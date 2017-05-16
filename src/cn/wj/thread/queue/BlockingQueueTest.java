package cn.wj.thread.queue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
* @author jwu
* @date 2017-1-23 下午2:35:27
* @Description 测试blocking
**/
public class BlockingQueueTest {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);  
        System.out.print("Enter base directory (e.g. /usr/local/jdk5.0/src): ");  
        String directory = in.nextLine();  
        System.out.print("Enter keyword (e.g. volatile): ");  
        String keyword = in.nextLine();  
        in.close();
  
        final int FILE_QUEUE_SIZE = 50;// 阻塞队列大小  
        final int SEARCH_THREADS = 6;// 关键字搜索线程个数  
  
        // 基于ArrayBlockingQueue的阻塞队列  
        BlockingQueue<File> queue = new ArrayBlockingQueue<File>(  
                FILE_QUEUE_SIZE);  
  
        //只启动一个线程来搜索目录  
        FileEnumerationTask enumerator = new FileEnumerationTask(queue,  
                new File(directory));  
        new Thread(enumerator).start();  
          
        //启动100个线程用来在文件中搜索指定的关键字  
        for (int i = 1; i <= SEARCH_THREADS; i++)  
            new Thread(new SearchTask(queue, keyword)).start();  
    }
    
}

/**
 * 文件列表查询服务
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
               System.out.println("当前搜索文件目录为:"+workDir.getName());
               this.enumerate(this.workDir);
               this.queue.put(DUMMY);
           }
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
        
    }
    
    /**
     * @Description 获取所有文件
     * @version 1.0
     * @author jwu
     * @throws InterruptedException 
     * @since 2017-1-23 下午3:12:01
     * @history
     */
    public void enumerate(File dir) throws InterruptedException{
        File files[] = dir.listFiles();
        for(File file : files){
            if(file.isDirectory()){
                this.enumerate(file);
            }else{
                System.out.println("工作队列放入文件:"+file.getName());
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
 * 搜索服务
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
            System.out.println(Thread.currentThread().getName()+"开始执行关键字搜索任务...");
            Scanner scan = null;
            try {
                File file = this.queue.take();
                if(file == FileEnumerationTask.DUMMY){
                   //取出来后重新放入，好让其他线程读到它时也很快的结束  
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
                System.out.println("文件:"+file.getName()+
                    ",行"+linenum+"找到关键字【"+this.keyWord+"】【"+line+"】");
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
