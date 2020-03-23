package com.mss.pdf;

import java.io.*;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.mss.crawler.common.FileUtils;
import com.mss.crawler.common.HttpClientPoolUtil;
import com.mss.crawler.common.UUIDGenerator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jodconverter.office.LocalOfficeManager;

import javax.xml.parsers.ParserConfigurationException;

/** 
 * @ClassName: HtmlToPdf 
 * @Description: TODO() 
 * @author xsw
 * @date 2016-12-8 上午10:14:54 
 *  
 */

public class HtmlToPdf {
    //wkhtmltopdf在系统中的路径 C:\Program Files\wkhtmltopdf\bin
    private static final String toPdfTool = " C:\\Program Files\\wkhtmltopdf\\bin\\wkhtmltopdf.exe";
    private static final String rootPath = "E:/data/html/";
    /**
     * html转pdf
     * @param  html路径，可以是硬盘上的路径，也可以是网络路径
     * @param destPath pdf保存路径
     * @return 转换成功返回true
     */
    public static boolean convertWeiXin(String url, String destPath){
    	
    	String text = HttpClientPoolUtil.httpGetRequest(url,null,null);
    	text = StringUtils.replace(text, "data-src", "src");
    	String fileName = UUIDGenerator.generate()+".html";
    	FileUtils.writeFile(text, rootPath+fileName);
    	
        File file = new File(destPath);
        File parent = file.getParentFile();
        //如果pdf保存路径不存在，则创建路径
        if(!parent.exists()){
            parent.mkdirs();
        }
        
        StringBuilder cmd = new StringBuilder();
        cmd.append(toPdfTool);
        cmd.append(" ");
        cmd.append("  --header-line");//页眉下面的线
        cmd.append("  --header-center 这里是页眉这里是页眉这里是页眉这里是页眉 ");//页眉中间内容
        //cmd.append("  --margin-top 30mm ");//设置页面上边距 (default 10mm) 
        cmd.append(" --header-spacing 10 ");//    (设置页眉和内容的距离,默认0)
        cmd.append(rootPath+fileName);
        cmd.append(" ");
        cmd.append(destPath);
        
        boolean result = true;
        try{
            Process proc = Runtime.getRuntime().exec(cmd.toString());
            HtmlToPdfInterceptor error = new HtmlToPdfInterceptor(proc.getErrorStream());
            HtmlToPdfInterceptor output = new HtmlToPdfInterceptor(proc.getInputStream());
            error.start();
            output.start();
            proc.waitFor();
        }catch(Exception e){
            result = false;
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * html转pdf
     * @param srcPath html路径，可以是硬盘上的路径，也可以是网络路径
     * @param destPath pdf保存路径
     * @return 转换成功返回true
     */
    public static boolean convert(String srcPath, String destPath){
        File file = new File(destPath);
        File parent = file.getParentFile();
        //如果pdf保存路径不存在，则创建路径
        if(!parent.exists()){
            parent.mkdirs();
        }
        
        StringBuilder cmd = new StringBuilder();
        cmd.append(toPdfTool);
        cmd.append(" ");
        cmd.append("  --header-line");//页眉下面的线
        cmd.append("  --header-center 这里是页眉这里是页眉这里是页眉这里是页眉 ");//页眉中间内容
        //cmd.append("  --margin-top 30mm ");//设置页面上边距 (default 10mm) 
        cmd.append(" --header-spacing 10 ");//    (设置页眉和内容的距离,默认0)
        cmd.append(" --debug-javascript ");
//        cmd.append(" --ignore-load-errors "); // 忽略错误加载项
        cmd.append(srcPath);
        cmd.append(" ");
        cmd.append(destPath);
        
        boolean result = true;
        try{
            Process proc = Runtime.getRuntime().exec(cmd.toString());
            HtmlToPdfInterceptor error = new HtmlToPdfInterceptor(proc.getErrorStream());
            HtmlToPdfInterceptor output = new HtmlToPdfInterceptor(proc.getInputStream());
            error.start();
            output.start();
            proc.waitFor();
        }catch(Exception e){
            result = false;
            e.printStackTrace();
        }
        
        return result;
    }
    
    
    public static void main(String[] args) throws FileNotFoundException, IOException, ParserConfigurationException {
    	/*List<File> list = FileUtils.getFileList1("D:/home",".html");
    	for(File file:list){
    		String srcFile = file.getAbsolutePath();
    		System.out.println(srcFile);
    		String destFile = StringUtils.replace(srcFile, ".html", ".pdf");
    		HtmlToPdf.convert(file.getAbsolutePath(), destFile);
    	}*/
    	HtmlToPdf.convert("E:\\temp\\123.html","E:\\temp\\3_cn-Z1231312.pdf");
//
//        LocalOfficeManager manager = LocalOfficeManager.builder().officeHome()

        /*PDDocument pdf = PDDocument.load(new File("E:/fy2020_agency_fact_sheet.pdf"));
        Writer output = new PrintWriter("E:/fy2020_agency_fact_sheet.html", "utf-8");
        new PDFDomTree().writeText(pdf, output);

        output.close();*/
//        PdfWriter pdf = PdfWriter.load

    }
}