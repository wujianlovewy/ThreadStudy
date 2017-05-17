package cn.wj.thread.util;

import java.util.concurrent.CountDownLatch;

/**
* @author jwu
* @date 2017-5-17 下午1:55:40
* @Description 
**/
public class LockConst {

    //测试线程数
    public static int THREAD_NUMBER = 20000;
    
    //线程结束计数
    public static CountDownLatch endLatch = new CountDownLatch(THREAD_NUMBER);
    
    //线程开始计数
    public static CountDownLatch startLatch = new CountDownLatch(1);
    
    //默认账户初始余额
    public static int ACCOUNT_BALANCE = 100000000;
    
    //模拟测试每次扣减金额
    public static int TRANS_AMT = 100;
    
    
}
