package cn.wj.thread.queue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
* @author jwu
* @date 2017-1-23 ����2:35:27
* @Description ����blocking
**/
public class BlockingQueueTest {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);  
        System.out.print("Enter base directory (e.g. /usr/local/jdk5.0/src): ");  
        String directory = in.nextLine();  
        System.out.print("Enter keyword (e.g. volatile): ");  
        String keyword = in.nextLine();  
        in.close();
  
        final int FILE_QUEUE_SIZE = 50;// �������д�С  
        final int SEARCH_THREADS = 6;// �ؼ��������̸߳���  
  
        // ����ArrayBlockingQueue����������  
        BlockingQueue<File> queue = new ArrayBlockingQueue<File>(  
                FILE_QUEUE_SIZE);  
  
        //ֻ����һ���߳�������Ŀ¼  
        FileEnumerationTask enumerator = new FileEnumerationTask(queue,  
                new File(directory));  
        new Thread(enumerator).start();  
          
        //����100���߳��������ļ�������ָ���Ĺؼ���  
        for (int i = 1; i <= SEARCH_THREADS; i++)  
            new Thread(new SearchTask(queue, keyword)).start();  
    }
    
}

/**
 * �ļ��б��ѯ����
 * @author jwu
 *
 */
class FileEnumerationTask implements Runnable{
    
    public static final File DUMMY = new File("");
    
    private BlockingQueue<File> queue;
    
    private File workDir;
    
    FileEnumerationTask(BlockingQueue<File> queue, File workDir){
        this.queue = queue;
        this.workDir = workDir;
    }

    @Override
    public void run() {
        
       try {
        if(null != workDir && workDir.isDirectory()){
               System.out.println("��ǰ�����ļ�Ŀ¼Ϊ:"+workDir.getName());
               this.enumerate(this.workDir);
               this.queue.put(DUMMY);
           }
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
        
    }
    
    /**
     * @Description ��ȡ�����ļ�
     * @version 1.0
     * @author jwu
     * @throws InterruptedException 
     * @since 2017-1-23 ����3:12:01
     * @history
     */
    public void enumerate(File dir) throws InterruptedException{
        File files[] = dir.listFiles();
        for(File file : files){
            if(file.isDirectory()){
                this.enumerate(file);
            }else{
                System.out.println("�������з����ļ�:"+file.getName());
                this.queue.put(file);
            }
        }
    }

    public BlockingQueue<File> getQueue() {
        return queue;
    }

    public void setQueue(BlockingQueue<File> queue) {
        this.queue = queue;
    }

    public File getWorkDir() {
        return workDir;
    }

    public void setWorkDir(File workDir) {
        this.workDir = workDir;
    }
    
}

/**
 * ��������
 * @author jwu
 *
 */
class SearchTask implements Runnable{

    private BlockingQueue<File> queue;
    private String keyWord;
    
    public SearchTask(BlockingQueue<File> queue, String keyWord){
        this.queue = queue;
        this.keyWord = keyWord;
    }
    
    @Override
    public void run() {
        boolean done = false;
        while(!done){
            System.out.println(Thread.currentThread().getName()+"��ʼִ�йؼ�����������...");
            Scanner scan = null;
            try {
                File file = this.queue.take();
                if(file == FileEnumerationTask.DUMMY){
                   //ȡ���������·��룬���������̶߳�����ʱҲ�ܿ�Ľ���  
                    queue.put(file);  
                    done = true;  
                }else{
                    scan = search(file);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return ;
            } finally{
               if(null!=scan){
                   scan.close();
               }
            }
        }
    }

    private Scanner search(File file) throws FileNotFoundException {
        Scanner scan = new Scanner(file);
        int linenum = 0;
        while(scan.hasNext()){
            linenum ++;
            String line = scan.nextLine();
            if(line.contains(this.keyWord)){
                System.out.println("�ļ�:"+file.getName()+
                    ",��"+linenum+"�ҵ��ؼ��֡�"+this.keyWord+"����"+line+"��");
            }
        }
        return scan;
    }
    
    public BlockingQueue<File> getQueue() {
        return queue;
    }

    public void setQueue(BlockingQueue<File> queue) {
        this.queue = queue;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }
    
}
