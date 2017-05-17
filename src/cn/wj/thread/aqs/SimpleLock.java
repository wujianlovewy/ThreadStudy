package cn.wj.thread.aqs;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import cn.wj.thread.util.LockConst;
import cn.wj.thread.util.LockRun;
import cn.wj.thread.util.MyLock;

/**
* @author jwu
* @date 2017-5-17 上午11:21:09
* @Description 学习AQS
* AQS是基于一个叫CLH lock queue的来实现线程阻塞队列
* 参考:   http://zhanjindong.com/2015/03/10/java-concurrent-package-aqs-overview AQS框架概述
*       http://ifeve.com/java-special-troops-aqs/   AQS原理解析
*       http://www.cnblogs.com/zhanjindong/p/java-concurrent-package-ThreadPoolExecutor.html executor解析
**/
public class SimpleLock extends AbstractQueuedSynchronizer implements MyLock{

    private static final long serialVersionUID = 6080411292750037659L;
    
    /**
     * @Description 测试AQS锁
     * @version 1.0
     * @author jwu
     * @since 2017-5-17 下午1:44:43
     * @history
     */
    public static void main(String[] args) throws Exception{
        long start = System.currentTimeMillis();
        SimpleLock lock = new SimpleLock();
        LockRun trans = new LockRun(lock);
        for(int i=0; i<LockConst.THREAD_NUMBER; i++){
            new Thread(trans, "t"+i).start();
        }
        System.out.println("AQS-----开始全部扣款任务!");
        LockConst.endLatch.await();
        System.out.println("AQS-----结束:"+(System.currentTimeMillis()-start)/1000+", 余额:"+LockConst.ACCOUNT_BALANCE);
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

