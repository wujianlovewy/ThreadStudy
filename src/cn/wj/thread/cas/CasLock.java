package cn.wj.thread.cas;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
* @author jwu
* @date 2017-5-9 
* @Description 
* 参考 http://ifeve.com/java_lock_see4/
*      http://ifeve.com/java_lock_see2/
*      http://www.cnblogs.com/langtianya/p/4520373.html  ExecutorService的十个使用技巧
*      http://www.thinksaas.cn/topics/0/80/80708.html jdt编译
**/
public class CasLock {

    public static int threadNo = 200000;
    
    //线程结束计数
    public static CountDownLatch endLatch = new CountDownLatch(threadNo);
    
    //线程开始计数
    public static CountDownLatch startLatch = new CountDownLatch(1);
    
    private static final AtomicBoolean lock = new AtomicBoolean(false);
    public static int balance = 100000000;
    
    //重入锁记录次数
    //private LockCntThreadLocal count = new LockCntThreadLocal();
    
    public void lock(){
        /*Integer localCnt = count.get();
        if(!lock.get()){
            if(localCnt==null){
                count.set(1);
            }else{
                localCnt++;
                count.set(localCnt);
            }
            System.out.println(Thread.currentThread().getName()+", 当前锁重入:"+count.get());
            return;
        }
        while(lock.getAndSet(true)){
            LockSupport.parkNanos(1);
        }
        localCnt++;
        count.set(localCnt);*/
        while(lock.getAndSet(true)){
            LockSupport.parkNanos(1);
        }
    }
    
    public void unlock(){
        /*Integer localCnt = count.get();
        if(!lock.get()){
            if(localCnt!=0){
                localCnt --;
                count.set(localCnt);
                System.out.println(Thread.currentThread().getName()+", 当前锁重入释放:"+count.get());
            }else{
                lock.set(false);
            }
        }*/
        lock.set(false);
    }
    
    public static void main(String[] args) throws Exception {
        poolTrans();
        //noPoolTrans();
    }
    
    public static void  poolTrans() throws Exception{
        ExecutorService taskExecutorService = new ThreadPoolExecutor(
            30, 30, 0L, TimeUnit.MILLISECONDS, 
            new LinkedBlockingQueue<Runnable>(1000000), new TaskThreadFactory());
            
        long start = System.currentTimeMillis();
        MyPoolTrans poolTrans = new MyPoolTrans();
        System.out.println("pool:-----开始全部扣款任务!");
        for(int i=0; i<threadNo; i++){
            taskExecutorService.submit(poolTrans);
        }
        
        taskExecutorService.shutdown();
        while(!taskExecutorService.isTerminated()){
            LockSupport.parkNanos(2);
        }
        System.out.println("pool:-----结束:"+(System.currentTimeMillis()-start)/1000+", 余额:"+balance);
    }
    
    public static void  noPoolTrans() throws Exception{
        long start = System.currentTimeMillis();
        MyTrans trans = new MyTrans(startLatch, endLatch);
        for(int i=0; i<threadNo; i++){
            
            new Thread(trans, "t"+i).start();
            //taskExecutorService.submit(trans);
        }
        System.out.println("noPool-----开始全部扣款任务!");
        startLatch.countDown();
        endLatch.await();
        System.out.println("noPool-----结束:"+(System.currentTimeMillis()-start)/1000+", 余额:"+balance);
    }
    
}

 class TaskThreadFactory implements ThreadFactory {
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    TaskThreadFactory() {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = "TaskPool-";
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
}

class LockCntThreadLocal extends ThreadLocal<Integer>{

    @Override
    protected Integer initialValue() {
        return 0;
    }
}

class MyPoolTrans implements Runnable{
    
    private CasLock casLock = new CasLock();

    @Override
    public void run() {
        this.casLock.lock();
        CasLock.balance -= 100;
        System.out.println("thread:"+Thread.currentThread()+", time:"+System.currentTimeMillis()+", money:"+CasLock.balance);
        this.casLock.unlock();
    }
}

class MyTrans implements Runnable{
    private CasLock casLock = new CasLock();
    
    private CountDownLatch startLatch;
    private CountDownLatch endLatch;
    
    public MyTrans(CountDownLatch startLatch, CountDownLatch endLatch) {
        super();
        this.startLatch = startLatch;
        this.endLatch = endLatch;
    }

    @Override
    public void run() {
        try {
            this.startLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.casLock.lock();
        CasLock.balance -= 100;
        System.out.println("thread:"+Thread.currentThread()+", time:"+System.currentTimeMillis()+", money:"+CasLock.balance);
        this.casLock.unlock();
        this.endLatch.countDown();
    }
    
}
