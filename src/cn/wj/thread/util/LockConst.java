package cn.wj.thread.util;

import java.util.concurrent.CountDownLatch;

/**
* @author jwu
* @date 2017-5-17 ����1:55:40
* @Description 
**/
public class LockConst {

    //�����߳���
    public static int THREAD_NUMBER = 20000;
    
    //�߳̽�������
    public static CountDownLatch endLatch = new CountDownLatch(THREAD_NUMBER);
    
    //�߳̿�ʼ����
    public static CountDownLatch startLatch = new CountDownLatch(1);
    
    //Ĭ���˻���ʼ���
    public static int ACCOUNT_BALANCE = 100000000;
    
    //ģ�����ÿ�οۼ����
    public static int TRANS_AMT = 100;
    
    
}
