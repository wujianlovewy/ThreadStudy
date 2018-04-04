package cn.wj.thread.aqs;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class UnsafeUtil {
	
	public static Unsafe getUnsafe(){
		return UnsafeObj.getInstance();
	}

	private static final class UnsafeObj{
		
		public static Unsafe getInstance(){
			try {
				Field field = Unsafe.class.getDeclaredField("theUnsafe");  
				field.setAccessible(true);  
				Unsafe unsafe = (Unsafe)field.get(null);  
				
				return unsafe;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			} 
		}
	}
	
}
