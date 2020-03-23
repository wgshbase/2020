package com.mss.license;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.mss.crawler.common.FileUtils;

public class LicenseChecker {
	
    private static final String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCaa8y9g+ioR2OSs8njT5uCGWm0YmOA1elSu/P5\n"  
            + "D3XYPCHsUPab74V5Og+NEZeTk9/LtG+jPSpKekTWm67gS2lQYWoygTwnoWsr4woaqNXmWw7L8Ty0\n"  
            + "LQFTojZgyynvu2RSIJp4c76z6SV/khiP/ireGtt8uzXPswPO5uaPU38tQQIDAQAB"; 
    
     private static Map<String,String> getMapFromStr(String data){
    	 Map<String,String> map = new HashMap<String,String>();
    	 String[] rexs = data.split(";");
    	 for(int i=0;i<rexs.length;i++){
    		 String[] obj = rexs[i].split("=");
    		 if(obj[0]!=null&&obj[0]!=""&&obj[1]!=null&&obj[1]!=""){
    			 map.put(obj[0], obj[1]);
    		 }
    	 }
    	 
    	 return map;
     }
	
	 public static boolean checker(String filePath,String publicKey) throws Exception {     
	      
		    byte[] encodedData  = FileUtil.getContent(filePath);
	        //解密  
	        byte[] decodedData = RSAUtils.decryptByPublicKey(encodedData, publicKey);  
	        String target = new String(decodedData);  	
	        
	        Map<String,String> map = getMapFromStr(target);
	        String serial = map.get("serial");
	        String timeEnd = map.get("timeEnd");
	        
	        if(Long.parseLong(timeEnd)-System.currentTimeMillis()>0&&serial.equals(CustomerSerial.getCupSerial())){
	        	return true ;
	        }
	        return false;
	    } 
	 
	 public static boolean checker(String publicKey) throws Exception {     
	      
		 byte[] encodedData  = FileUtils.fileToString(new File(FileUtil.getBasePath()+File.separator+"license.dat")).getBytes("ISO-8859-1");
	    //byte[] encodedData  = FileUtil.getContent(FileUtil.getBasePath()+File.separator+"license.dat");
        //解密  
        byte[] decodedData = RSAUtils.decryptByPublicKey(encodedData, publicKey);  
        String target = new String(decodedData); 
        System.out.println(target);
        return true;
	}  

	 /**
	  * 校验
	  * @param license
	  * @return
	  * @throws Exception
	  */
	 public static boolean checkLicense(String license) throws Exception {     
	    byte[] encodedData  = license.getBytes("ISO-8859-1");
        //解密  
        byte[] decodedData = RSAUtils.decryptByPublicKey(encodedData, publicKey);  
        String target = new String(decodedData);  
        Map<String,String> map = getMapFromStr(target);
        String serial = map.get("serial");
        String timeEnd = map.get("timeEnd");
        if(Long.parseLong(timeEnd)-System.currentTimeMillis()>0&&serial.equals(CustomerSerial.getCupSerial())){
        	return true ;
        }
        return false;
	}
	 
	 public static void main(String[] args) throws Exception{
		 checker(publicKey);
		 //checkLicense("�I���5q6�@ȅfw���8�x4CT��q���\"<��3�u�Ǟ��5��*��:�m���$:v�c�B����]<{8m��-�agef#���\bQd9��;�1$Z�����}�3�����Z8Ts�'");
	 }

}
