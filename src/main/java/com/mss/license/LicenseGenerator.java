package com.mss.license;

import java.io.File;
import java.util.HashMap;
import java.util.Map;  

/** 
 * 生成license 
 * @author happyqing 
 * 2014.6.15 
 */  
public class LicenseGenerator {  
      
    /** 
     * serial：由客户提供 
     * timeEnd：过期时间 
     */  
    private static String licensestatic = "serial=568b8fa5cdfd8a2623bda1d8ab7b7b34;" +  
                                          "timeEnd=1404057600000";  
      
 
      
    /** 
     * RSA算法 
     * 公钥和私钥是一对，此处只用私钥加密 
     */  
    public static final String privateKey = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJprzL2D6KhHY5KzyeNPm4IZabRi\n"  
            + "Y4DV6VK78/kPddg8IexQ9pvvhXk6D40Rl5OT38u0b6M9Kkp6RNabruBLaVBhajKBPCehayvjChqo\n"  
            + "1eZbDsvxPLQtAVOiNmDLKe+7ZFIgmnhzvrPpJX+SGI/+Kt4a23y7Nc+zA87m5o9Tfy1BAgMBAAEC\n"  
            + "gYAVnlfohEoTHQN0q1TtTNzRhutEhK23gLsMiSGr0Z1G64w4QFF2HT9LbHR25GqbD426QAWNDegY\n"  
            + "yytN/DesUQJqNXx8vuEuqs7+MQDgKgJqpAx+Fg3Iwsk/SVjq7meaSVGCgPKhtWHJk5oXoRMpsrlT\n"  
            + "AwUjpdpAZXIIKW3mrqkW0QJBANq4INw6lZlqRFtxT4uzYQYtzmB/nxMnCbL2SQ4ZQ/4CWlQpOnR/\n"  
            + "mH2JxIBCVtTADFlPM0DWF4aoqykYs9tu2X0CQQC0vgEk8DpkQbh1kgIyBGWCqYSKISTSXia0rbYo\n"  
            + "FPnzdldgtZVirNGNmiJGL8RPz0YKpZNOg9FLHq/oYXSNFI4VAkAJ4OcbC0pWc4ZC2wtMs/1d2hPI\n"  
            + "J/t3UfwOKTGDgYCgqFqMEpChUmIAyYgmgtiJI2NrZThbZVAKtPOGF6eH8anBAkAbxkL4wS3H8E1/\n"  
            + "S7OoqgJLZO9oJpW4+hzqkPM4D5klb58Xzm+pXTNKllAEBx0cwpZZ1n3fh+Qmrg2MIUW+1FTNAkBt\n"  
            + "WECowLUqW014M96WsFpiof7kjteOBNOjFyxhIbx2eT7//bnrADfq2Xu1/mSedUKrjGr/O+FRi7PO\n"  
            + "u7WhF6C9";  
      
    public static void generator(Map<String,String> license,String privateKey) throws Exception {          
        StringBuilder lsb = new StringBuilder();
        for (Map.Entry<String, String> entry : license.entrySet()) {  
        	lsb.append(entry.getKey());
        	lsb.append("=");
        	lsb.append(entry.getValue());
        	lsb.append(";");
        } 
        System.out.println("原文字：\r\n" + lsb);   
        byte[] data = lsb.toString().getBytes();  
        byte[] encodedData = RSAUtils.encryptByPrivateKey(data, privateKey);  
        System.out.println("加密后：\r\n" + new String(encodedData)); //加密后乱码是正常的 
        Base64Utils.byteArrayToFile(encodedData, FileUtil.getBasePath()+File.separator+"license.dat");        
    }  
      
    public static void main(String[] args) throws Exception { 
    	Map<String,String> license = new HashMap<String,String>();
    	license.put("serial", "568b8fa5cdfd8a2623bda1d8ab7b7b34");
    	license.put("timeEnd", "1404057600000");
        generator(license,privateKey);  
    }  
}  