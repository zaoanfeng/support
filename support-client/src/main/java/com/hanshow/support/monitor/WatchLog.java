package com.hanshow.support.monitor;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;

public class WatchLog {
	
	private RandomAccessFile raf;
	private static long DAY = 1000 * 60 * 60 * 24;
	

	
	public String watch(String watchFile) throws IOException {
		File file = new File(watchFile);
		if (file.exists()) {
			long lastModifyTime = Cache.get(file.getName() + ".modifyTime") == null ? new Date().getTime() - DAY: (long)Cache.get(file.getName() + ".modifyTime");
			if(lastModifyTime <= file.lastModified()) {
				raf = new RandomAccessFile(file, "r"); 
				long pos = Cache.get(file.getName() + ".length") == null ? 0L : (long)Cache.get(file.getName() + ".length");
				raf.seek(pos);
				byte[]  buff=new byte[1024];  
	            //用于保存实际读取的字节数  
	            int hasRead=0;  
	            //循环读取 
	            StringBuilder result = new StringBuilder();
	            int i=0;
	            while((hasRead=raf.read(buff))>0){  
	                //打印读取的内容,并将字节转为字符串输入  
	                result.append(new String(buff,0,hasRead));  
	               i++;
	               if (i >= 500) {
	            	   break;
	               }
	            } 
	            Cache.set(file.getName() + ".length", file.length());
	            Cache.set(file.getName() + ".modifyTime", file.lastModified());
	            return result.toString();
			}
			return null;
		}
		return null;
	}
	
}
