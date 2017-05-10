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

    public static int threadNo = 100;
    public static CountDownLatch cdl = new CountDownLatch(threadNo);
    private static final AtomicBoolean lock = new AtomicBoolean(false);
    public static int balance = 100000000;
    
    //重入锁记录次数
    private ThreadLocal<Integer> count = new ThreadLocal<Integer>();
    
    public void setLockCnt(int cnt){
        this.count.set(0);
    }
    
    public void lock(){
        Integer localCnt = count.get();
        if(lock.get()){
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
        count.set(localCnt);
    }
    
    public void unlock(){
        Integer localCnt = count.get();
        if(lock.get()){
            if(localCnt!=0){
                localCnt --;
                count.set(localCnt);
                System.out.println(Thread.currentThread().getName()+", 当前锁重入释放:"+count.get());
            }else{
                lock.set(false);
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        
        long start = System.currentTimeMillis();
        MyTrans trans = new MyTrans(cdl);
        for(int i=0; i<threadNo; i++){
            new Thread(trans, "t"+i).start();
        }
        cdl.await();
        System.out.println("-----结束:"+(System.currentTimeMillis()-start)/1000+", 余额:"+balance);
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
        this.casLock.setLockCnt(0);
        this.countDownLatch.countDown();
        this.casLock.lock();
        this.casLock.lock();
        CasLock.balance -= 100;
        System.out.println("thread:"+Thread.currentThread()+", money:"+CasLock.balance);
        this.casLock.unlock();
        this.casLock.unlock();
    }
    
}
