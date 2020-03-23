package com.mss.crawler.spiderjson.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

/**
 * 视频文件工具类, 主要提供视频文件的合并的功能
 *
 * @Author wgsh
 * @Date wgshb on 2019/4/28 13:33
 */
public class FileUtils {

	public static  void CombineFile(String path,String tar) throws Exception {
		try {
			File dirFile = new File(path);
			FileInputStream fis;
			FileOutputStream fos = new FileOutputStream(tar);
			byte buffer[] = new byte[1024 * 1024 * 2];//一次读取2M数据，将读取到的数据保存到byte字节数组中
			int len;
			if(dirFile.isDirectory()) { //判断file是否为目录
				String[] fileNames = dirFile.list();
				Arrays.sort(fileNames, new StringComparator());//实现目录自定义排序
				for (int i = 0;i<fileNames.length ;i++){
					System.out.println("E:\\temp\\"+fileNames[i]);
					fis = new FileInputStream("E:\\temp\\"+fileNames[i]);
					len = 0;
					while ((len = fis.read(buffer)) != -1) {
						fos.write(buffer, 0, len);//buffer从指定字节数组写入。buffer:数据中的起始偏移量,len:写入的字数。
					}
					fis.close();
				}
			}
			fos.flush();
			fos.close();
		}catch (IOException e){
			e.printStackTrace();
		} finally {
			System.out.println("合并完成！");
		}
	}

	// 字符串的比较器...
	static class StringComparator implements Comparator<String> {
		@Override
		public int compare(String s1, String s2) {
			if (returnDouble(s1) < returnDouble(s2))
				return -1;
			else if (returnDouble(s1) > returnDouble(s2))
				return 1;
			else
				return 0;
		}
	}

	public static double returnDouble(String str){
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<str.length();i++){
			if(Character.isDigit(str.charAt(i)))
				sb.append(str.charAt(i));
			else if(str.charAt(i)=='.'&&i<str.length()-1&&Character.isDigit(str.charAt(i+1)))
				sb.append(str.charAt(i));
			else break;
		}
		if(sb.toString().isEmpty())
			return 0;
		else
			return Double.parseDouble(sb.toString());
	}

	public static void main(String[] args) throws Exception {
		CombineFile("E:\\temp\\", "E:\\1.mp4");

	}

}
