package cn.wj.thread.aqs;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;

import sun.misc.Unsafe;

/**
 * 参考AbstractQueuedSynchronizer实现 CLH锁
 * 参考文章: https://www.cnblogs.com/micrari/p/6937995.html
 * http://www.cnblogs.com/davidwang456/category/805707.html
 * @author jwu
 */
@SuppressWarnings("restriction")
public class WQueuedSynchronizer {
	
	private static final long waitStatusOffset;
	private static final long tailOffset;
	private static final long headOffset;
	
	private static final Unsafe unsafe = UnsafeUtil.getUnsafe();
	
	static{
		try {
			waitStatusOffset = unsafe.objectFieldOffset(WQueuedSynchronizer.Node.class.getDeclaredField("waitStatus"));
			tailOffset = unsafe.objectFieldOffset(WQueuedSynchronizer.class.getDeclaredField("tail"));
			headOffset = unsafe.objectFieldOffset(WQueuedSynchronizer.class.getDeclaredField("head"));
		} catch (Exception e) {
			throw new Error(e);
		}
	}
	
	private static final boolean compareAndSetWaitStatus(Node node, int expect, int update){
		return unsafe.compareAndSwapInt(node, waitStatusOffset, expect, update);
	}
	
	private final boolean compareAndSetTail(Node expect, Node update){
		return unsafe.compareAndSwapObject(this, tailOffset, expect, update);
	}
	
	private final boolean compareAndSetHead(Node update){
		return unsafe.compareAndSwapObject(this, headOffset, null, update);
	}
	
	//头节点
	volatile Node head;
	
	//尾节点
	volatile Node tail;
	
	protected boolean tryAcquire(int arg){
		return false;
	}
	
	public final void acquire(int arg) {
		if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
			selfInterrupt();
	}
	
	private static void selfInterrupt(){
		Thread.currentThread().interrupt();
	}
	
	public static void main(String[] args) throws Exception{
		
		int n = 1;
		final CountDownLatch latch = new CountDownLatch(n);
		final WQueuedSynchronizer queued = new WQueuedSynchronizer();
		for(int i=0;i<n;i++){
			new Thread(){
				@Override
				public void run() {
					queued.acquire(1);
					latch.countDown();
				}
				
			}.start();
		}
		
		latch.await();
		
	}
	
	//队列中已经存在的节点获取锁,如果等待中中断线程,返回中断标识
	final boolean acquireQueued(Node node, int arg){
		//标记线程是否中断
		boolean interrupted = false;
		for(;;){
			Node prev = node.predecessor();
			//如果前继节点是head节点并且获取锁成功,将当前节点设置成head节点
			if(prev == head && this.tryAcquire(arg)){
				System.out.println("节点-"+node+" 获取锁成功,将自己设置成Head节点");
				this.setHead(node);
				prev.next = null; //让GC回收prev节点
				return interrupted;
			}
			
			if(shouldParkAfterFailedAcquire(prev, node)
					&& this.parkAndCheckInterrupt()){
				interrupted = true;
			}
		}
	}
	
	//等待节点加入队列
	private Node addWaiter(Node mode) {
		Node node = new Node(Thread.currentThread(), Node.EXCLUSIVE);
		//先执行快速入队的方式,失败执行循环重试入队的方式
		Node pred = tail;
		if(pred!=null){
			node.prev = pred;
			if(this.compareAndSetTail(pred, node)){
				System.out.println("直接将"+node+"放入队尾");
				//当前节点入队成功,将之前的尾节点置为当前节点的前置节点
				pred.next = node;
				return node;
			}
		}
		
		//快速入队失败,执行循环入队方式
		enq(node);
		
		return node;
	}
	
	private Node enq(Node node){
		for(;;){
			Node pred = this.tail;
			if(pred==null){
				Node head = new Node();
				node.prev = head;
				head.next = node;
				if(this.compareAndSetHead(head)){
					System.out.println("head节点为空,设置head节点并将"+node+"放入队尾");
					this.tail = node;
					return node;
				}
			}else{
				node.prev = pred;
				if(this.compareAndSetTail(pred, node)){
					System.out.println("将"+node+"放入队尾");
					pred.next = node;
					return node;
				}
			}
		}
		
	}
	
	//根据前继节点的waitStatus来判断是否需要阻塞当前节点
	private static boolean shouldParkAfterFailedAcquire(Node pred, Node node){
		//前继节点的状态是带唤醒状态 当前节点需要阻塞
		int ws = pred.waitStatus;
		if(ws == Node.SIGNAL){
			System.out.println("节点-"+node+"需要阻塞");
			return true;
		}
		
		if(ws>0){
			//如果前继节点已经取消 ,向前找到未取消的节点并且将该节点置为前继节点
			do{
				node.prev = pred = pred.prev;
				ws = pred.waitStatus;
			}while(ws>0);
			pred.next = node;
		}else{
			//将前继节点置为唤醒状态 在线程阻塞前未成功获取锁保证线程可以重试
			compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
			System.out.println("通知前继节点-"+pred+"唤醒");
		}
		
		return false;
	}
	
	//阻塞当前线程,并且返回中断状态
	private boolean parkAndCheckInterrupt(){
		LockSupport.park(this);
		return Thread.interrupted();
	}

	private void setHead(Node node){
		head = node;
		node.thread = null;
		node.prev = null;
	}
	

	static final class Node{
		//标记线程取消状态
		static final int CANCELLED =  1;
		
		/**标记后继线程需要唤醒 */
        static final int SIGNAL    = -1;
        
        /**标记节点上的锁是独占模式*/
        static final Node EXCLUSIVE = null;
        
        //节点状态
        volatile int waitStatus;
        
        //前继节点
        volatile Node prev;
        
        //后继节点
        volatile Node next;
        
        //等待队列中的节点模式 独占、共享
        Node nextWaiter;
        
        volatile Thread thread;
        
        final Node predecessor() throws NullPointerException{
        	Node p = prev;
        	if(p==null) throw new NullPointerException();
        	return p;
        }

        public Node(){
        }
        
		public Node(Thread thread,Node mode) {
			this.nextWaiter = mode;
			this.thread = thread;
		}

	}
	
}
