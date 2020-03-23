package com.mss.license;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;



public class CustomerSerial {
	
	public static String getCupSerial(){
		 try {  
		        //long start = System.currentTimeMillis();  
		        Process process = Runtime.getRuntime().exec(  
		        new String[] { "wmic", "cpu", "get", "ProcessorId" });  
		        process.getOutputStream().close();  
		        Scanner sc = new Scanner(process.getInputStream());  
		        String property = sc.next();  
		        String serial = sc.next();  
		        System.out.println(property + ": " + serial);  		      
		        return serial;
		    } catch (IOException e) {  
		        // TODO Auto-generated catch block  
		        e.printStackTrace();  
		        return "";
		    }  
	}
	
	private static String getLocalMac() throws SocketException, UnknownHostException {
		
		InetAddress ia = InetAddress.getLocalHost();
		//获取网卡，获取地址
		byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
		System.out.println("mac数组长度："+mac.length);
		StringBuffer sb = new StringBuffer("");
		for(int i=0; i<mac.length; i++) {
			if(i!=0) {
				sb.append("-");
			}
			//字节转换为整数
			int temp = mac[i]&0xff;
			String str = Integer.toHexString(temp);
			System.out.println("每8位:"+str);
			if(str.length()==1) {
				sb.append("0"+str);
			}else {
				sb.append(str);
			}
		}
		System.out.println("本机MAC地址:"+sb.toString().toUpperCase());
		return sb.toString().toUpperCase();
		
	}

	
	public static void main(String[] args) throws SocketException, UnknownHostException {  
		getCupSerial();
		getLocalMac();
	  
	}

}
