package cn.wj.thread.unsafe;

import java.lang.reflect.Field;

import cn.wj.thread.aqs.UnsafeUtil;

import sun.misc.Unsafe;

public class UnsafeTest {
	
	public volatile int state;
	
	private static final Unsafe unsafe = UnsafeUtil.getUnsafe();
    private static final long stateOffset;
    /**
     * Unsafe 有一public 方法：getUnsafe() , 返回值是Unsafe的实例。如果直接调用会抛： 
     * Exception in thread "main" java.lang.SecurityException: Unsafe。 有这样一段说明： Although the class and all methods are public, 
     * use of this class is limited because only trusted code can obtain instances of it.  所以只有java认为是安全的代码才可以获取Unsafe实例。
     * 方法调用在java中是通过栈帧的出栈入栈实现的， 栈帧是方法执行时的数据结构， 包括局部变量表， 操作数栈等。getCallerClass(int n) 就是返回 跳过n 个栈帧后，定义这个捏的方法的类。 
     *  getCallerClass(2) 返回的永远是我们的代码中调用getUnsafe 的那个类（n = 0,返回的是 sun.reflect.Reflection. n=1 , 返回的是 Unsafe）。
     *  而我们所写的类的加载器是 AppClassLoader 。java加载器使用的双亲委派模型，保证了只有rt.jar 等class 是使用的启动类加载器，即getClassLoader返回null。
     * public static Unsafe getUnsafe() {  
       Class cc = sun.reflect.Reflection.getCallerClass(2);  
       if (cc.getClassLoader() != null)  
           throw new SecurityException("Unsafe");  
       return theUnsafe;  
   	  } 
     */
    
    static{
    	try {
            stateOffset = unsafe.objectFieldOffset
                (UnsafeTest.class.getDeclaredField("state"));
        } catch (Exception ex) { throw new Error(ex); }
    }
    
    public final boolean compareAndSetState(int expect, int update){
    	System.out.println(sun.reflect.Reflection.getCallerClass(0)); 
    	System.out.println(sun.reflect.Reflection.getCallerClass(1)); 
    	return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
    }
    
    public int getState() {
		return state;
	}

	public static void main(String[] args) {
		UnsafeTest test = new UnsafeTest();
		System.out.println(test.compareAndSetState(0, 1)+" : "+test.getState());
		System.out.println(test.compareAndSetState(0, 2)+" : "+test.getState());
		
	}
	
}
