package com.mss.crawler.spiderjson.util;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PingIPUtils {

    private static Logger logger = Logger.getLogger(PingIPUtils.class);

    public static Boolean pingIp(String ipadd)throws Exception{
//获得当前进程对象
        Runtime runtime=Runtime.getRuntime();
        Process process=null;//声明处理类对象
        String line=null; //返回行信息
        InputStream is=null; //输入流
        InputStreamReader isr=null;//字节流
        BufferedReader br=null; //字符流 
        Boolean res=false;  //结果
        try{
            process=runtime.exec("ping "+ipadd);  //ping 命令
            is=process.getInputStream();//实例化流
            isr=new InputStreamReader(is); //输入流转化成为字节流
            br=new BufferedReader(isr);//从字节中读取文本
            while((line=br.readLine())!=null){
                if(line.contains("TTL")){
                    res=true;
                    break;
                }
            }
            is.close();
            br.close();
            isr.close();
        }catch(Exception e){
            logger.error("ping门店ip失败！原因："+e.getMessage());
            throw new RuntimeException(e);
        }
        return res;

    }

    public static void main(String[] args) throws Exception {
        Boolean aBoolean = pingIp("www.baidu.com");
        System.out.println(aBoolean);
    }

}
