package cn.wj.thread.cas;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
* @author jwu
* @date 2017-5-9 
* @Description 
**/
public class CasLock {

    public static int threadNo = 1;
    public static CountDownLatch cdl = new CountDownLatch(threadNo);
    private static final AtomicBoolean lock = new AtomicBoolean(false);
    public static int balance = 100000000;
    
    public void lock(){
        while(lock.getAndSet(true)){
            LockSupport.parkNanos(1);
        }
    }
    
    public void unlock(){
        lock.set(false);
    }
    
    public static void main(String[] args) throws Exception {
        
        long start = System.currentTimeMillis();
        MyTrans trans = new MyTrans(cdl);
        for(int i=0; i<threadNo; i++){
            new Thread(trans, "t"+i).start();
        }
        cdl.await();
        System.out.println("-----½áÊø:"+(System.currentTimeMillis()-start)/1000+", Óà¶î:"+balance);
    }
    
}

class MyTrans implements Runnable{
    
    private CasLock casLock = new CasLock();
    
    private CountDownLatch countDownLatch;
    
    public MyTrans(CountDownLatch countDownLatch) {
        super();
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        this.countDownLatch.countDown();
        this.casLock.lock();
        CasLock.balance -= 100;
        System.out.println("thread:"+Thread.currentThread()+", money:"+CasLock.balance);
        this.casLock.unlock();
    }
    
}
