package cn.wj.thread.aqs;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import cn.wj.thread.util.LockConst;
import cn.wj.thread.util.LockRun;
import cn.wj.thread.util.MyLock;

/**
* @author jwu
* @date 2017-5-17 ����11:21:09
* @Description ѧϰAQS
* AQS�ǻ���һ����CLH lock queue����ʵ���߳���������
* �ο�:   http://zhanjindong.com/2015/03/10/java-concurrent-package-aqs-overview AQS��ܸ���
*       http://ifeve.com/java-special-troops-aqs/   AQSԭ�����
*       http://www.cnblogs.com/zhanjindong/p/java-concurrent-package-ThreadPoolExecutor.html executor����
**/
public class SimpleLock extends AbstractQueuedSynchronizer implements MyLock{

    private static final long serialVersionUID = 6080411292750037659L;
    
    /**
     * @Description ����AQS��
     * @version 1.0
     * @author jwu
     * @since 2017-5-17 ����1:44:43
     * @history
     */
    public static void main(String[] args) throws Exception{
        long start = System.currentTimeMillis();
        SimpleLock lock = new SimpleLock();
        LockRun trans = new LockRun(lock);
        for(int i=0; i<LockConst.THREAD_NUMBER; i++){
            new Thread(trans, "t"+i).start();
        }
        System.out.println("AQS-----��ʼȫ���ۿ�����!");
        LockConst.endLatch.await();
        System.out.println("AQS-----����:"+(System.currentTimeMillis()-start)/1000+", ���:"+LockConst.ACCOUNT_BALANCE);
    }
    
    public SimpleLock(){
    }
    
    protected boolean tryAcquire(int unused) {
        if (compareAndSetState(0, 1)) {
            setExclusiveOwnerThread(Thread.currentThread());
            return true;
        }
        return false;
    }

    protected boolean tryRelease(int unused) {
        setExclusiveOwnerThread(null);
        setState(0);
        return true;
    }

    @Override
    public void lock() {
        acquire(1);
    }

    public boolean tryLock() {
        return tryAcquire(1);
    }

    @Override
    public void unlock() {
        release(1);
    }

    public boolean isLocked() {
        return isHeldExclusively();
    }

}

