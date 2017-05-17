package cn.wj.thread.util;
/**
* @author jwu
* @date 2017-5-17 обнГ2:01:39
* @Description 
**/
public class LockRun implements Runnable {

    private MyLock lock;
    
    public LockRun(MyLock lock){
        this.lock = lock;
    }
    
    @Override
    public void run() {
        this.lock.lock();
        LockConst.ACCOUNT_BALANCE -= LockConst.TRANS_AMT;
        System.out.println("thread:"+Thread.currentThread()+", time:"+System.currentTimeMillis()
            +", money:"+LockConst.ACCOUNT_BALANCE);
        
        this.lock.unlock();
        
        LockConst.endLatch.countDown();
    }

}
