package io.renren.modules.spider.utils;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.mss.pdf.HtmlToPdf;
import com.mss.translate.GoogleTranslate;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pdf2HtmlUtils {

    /**
     * 调用pdf2htmlEX将pdf文件转换为html文件
     *
     * @param exeFilePath
     *            pdf2htmlEX.exe文件路径
     * @param pdfFile
     *            pdf文件绝对路径
     * @param destDir 生成的html文件存放路径
     * @param htmlFileName
     *            生成的html文件名称
     * @return
     */
    public static String pdf2htmlWindows(String exeFilePath, String pdfFile,
                                   String destDir, String htmlFileName) {
        if (!(exeFilePath != null && !"".equals(exeFilePath) && pdfFile != null
                && !"".equals(pdfFile) && htmlFileName != null && !""
                .equals(htmlFileName))) {
            System.out.println("传递的参数有误！");
            return "";
        }
        Runtime rt = Runtime.getRuntime();
        StringBuilder command = new StringBuilder();
        command.append(exeFilePath).append(" ");
        if (destDir != null && !"".equals(destDir.trim()))// 生成文件存放位置,需要替换文件路径中的空格
            command.append("--dest-dir ").append(destDir.replace(" ", "\" \""))
                    .append(" ");
        command.append("--optimize-text 0 ");// 尽量减少用于文本的HTML元素的数目 (default: 0)
        command.append("--zoom 1.4 ");
        command.append("--process-outline 0 ");// html中显示链接：0——false，1——true
        command.append("--font-format woff ");// 嵌入html中的字体后缀(default ttf)
        // ttf,otf,woff,svg
        command.append(pdfFile.replace(" ", "\" \"")).append(" ");// 需要替换文件路径中的空格
        if (htmlFileName != null && !"".equals(htmlFileName.trim())) {
            command.append(htmlFileName);
            if (htmlFileName.indexOf(".html") == -1)
                command.append(".html");
        }
        try {
            System.out.println("Command：" + command.toString());
            Process p = rt.exec(command.toString());
            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(),
                    "ERROR");
            // 开启屏幕标准错误流
            errorGobbler.start();
            StreamGobbler outGobbler = new StreamGobbler(p.getInputStream(),
                    "STDOUT");
            // 开启屏幕标准输出流
            outGobbler.start();
            int w = p.waitFor();
            int v = p.exitValue();
            if (w == 0 && v == 0) {
                return (destDir.endsWith(File.separator)?destDir:destDir+File.separator) + htmlFileName;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean pdf2htmlLinux(String pdfFile, String destDir,
                                         String htmlFileName) {
        if (!(pdfFile != null && !"".equals(pdfFile) && htmlFileName != null && !""
                .equals(htmlFileName))) {
            System.out.println("传递的参数有误！");
            return false;
        }
        Runtime rt = Runtime.getRuntime();
        StringBuilder command = new StringBuilder();
        command.append("pdf2htmlEX").append(" ");
        if (destDir != null && !"".equals(destDir.trim()))// 生成文件存放位置,需要替换文件路径中的空格
            command.append("--dest-dir ").append(destDir.replace(" ", "\" \""))
                    .append(" ");
        command.append("--optimize-text 0 ");// 尽量减少用于文本的HTML元素的数目 (default: 0)
        command.append("--process-outline 0 ");// html中显示链接：0——false，1——true
        command.append("--font-format woff ");// 嵌入html中的字体后缀(default ttf)
        // ttf,otf,woff,svg
        command.append(pdfFile.replace(" ", "\" \"")).append(" ");// 需要替换文件路径中的空格
        if (htmlFileName != null && !"".equals(htmlFileName.trim())) {
            command.append(htmlFileName);
            if (htmlFileName.indexOf(".html") == -1)
                command.append(".html");
        }
        try {
            System.out.println("Command：" + command.toString());
            Process p = rt.exec(command.toString());
            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(),
                    "ERROR");
            // 开启屏幕标准错误流
            errorGobbler.start();
            StreamGobbler outGobbler = new StreamGobbler(p.getInputStream(),
                    "STDOUT");
            // 开启屏幕标准输出流
            outGobbler.start();
            int w = p.waitFor();
            int v = p.exitValue();
            if (w == 0 && v == 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
//          pdf2htmlWindows("F:\\111\\pdf2htmlEX-v1.0\\pdf2htmlEX.exe","E:/fy2020_agency_fact_sheet.pdf","E:\\temp","fy2020_agency_fact_sheet_zh-CHS.html");


        File file = new File("E:\\temp\\ztbg\\111");
        File[] files = file.listFiles();
        for(File f : files) {
            if(f.isFile() && f.getName().endsWith("pdf")) {
                translateEngPdf2Chinese(f.getAbsolutePath().replace(" ", "_"), "E:\\temp\\ztbg\\111");
            }
        }


//        translateEngPdf2Chinese("E:\\Air_Force_Space_Command.pdf", "E:\\temp");

    }

    /**
     * 将 html 转化为 pdf
     * @param fin
     * @return
     */
    public static String parseHtml2String(File fin) {
//        try (RandomAccessFile accessFile = new RandomAccessFile(fin, "r");
//             FileChannel fcin = accessFile.getChannel();
//        ){
//            Charset charset = Charset.forName("UTF-8");
//            int bufSize = 100000;
//            ByteBuffer rBuffer = ByteBuffer.allocate(bufSize);
//            String enterStr = "\n";
//            byte[] bs = new byte[bufSize];
//            StringBuilder strline = new StringBuilder("");
//            StringBuilder strBuf = new StringBuilder("");
//            while (fcin.read(rBuffer) != -1) {
//                int rSize = rBuffer.position();
//                rBuffer.rewind();
//                rBuffer.get(bs);
//                rBuffer.clear();
//                String tempString = new String(bs, 0, rSize,charset);
////                tempString = tempString.replaceAll("\r", "");
//
//                int fromIndex = 0;
//                int endIndex = 0;
//                while ((endIndex = tempString.indexOf(enterStr, fromIndex)) != -1) {
//                    String line = tempString.substring(fromIndex, endIndex);
//                    line = strBuf.toString() + line;
//                    strline.append(line.trim() + enterStr);
//
//                    strBuf.delete(0, strBuf.length());
//                    fromIndex = endIndex + 1;
//                }
//                if (rSize > tempString.length()) {
//                    strline.append(tempString.substring(fromIndex, tempString.length()));
//                    strBuf.append(tempString.substring(fromIndex, tempString.length()));
//                } else {
//                    strline.append(tempString.substring(fromIndex, rSize));
//                    strBuf.append(tempString.substring(fromIndex, rSize));
//                }
//            }
//            return strline.toString();
//        } catch (Exception e) {
//
//        }
//        return "";


        String fileContent = "";
        try {
            if(fin.isFile()&&fin.exists()){
                InputStreamReader read = new InputStreamReader(new FileInputStream(fin),"UTF-8");
                BufferedReader reader=new BufferedReader(read);
                String line;
                while ((line = reader.readLine()) != null) {
                    //将读取到的字符拼接
                    fileContent += line + "\n";
                }
                read.close();
            }
        } catch (Exception e) {
            System.out.println("读取文件内容操作出错");
            e.printStackTrace();
        }
//        System.out.println("fileContent:"+fileContent);
        return fileContent;

    }

    /**
     * pdf 翻译
     */
    public static void translateEngPdf2Chinese(String pdfPath, String targetDir) throws IOException {
        String filename = pdfPath.substring(pdfPath.lastIndexOf(File.separator) + 1, pdfPath.lastIndexOf("."));
        // 1. 将 pdf 抽取到  html
        String htmlName = pdf2htmlWindows("F:\\111\\pdf2htmlEX-v1.0\\pdf2htmlEX.exe",pdfPath,targetDir,filename + ".html");
        // 2. 抽取 html 的内容到 字符串
        String s = parseHtml2String(new File(htmlName));

        // 3. 翻译解析字符串
        if(!StringUtils.isEmpty(s)) {
            String trS = null;
            try {
                trS = translateString(s);
                String targetHtml = targetDir.endsWith(File.separator)?(targetDir + filename + "_cn-ZHS.html"):(targetDir+File.separator+filename + "_cn-ZHS.html");

                // 4. 字符串拼装 html
                transStr2LocalHtml(trS, targetHtml);

                // 5. html 转化为 pdf
                HtmlToPdf.convert(targetHtml, targetDir.endsWith(File.separator)?(targetDir + filename + "_cn-ZHS.pdf"):(targetDir+File.separator+filename + "_cn-ZHS.pdf"));
            } catch (Exception e) {
                e.printStackTrace();
            }


        }

//        transHtml2Pdf(targetHtml, targetDir.endsWith(File.separator)?(targetDir + filename + "_cn-ZHS.pdf"):(targetDir+File.separator+filename + "_cn-ZHS.pdf"));

    }

    /**
     * html 转换为 pdf
     */
    public static void transHtml2Pdf(String html, String pdf) {
        Document document = new Document();
        PdfWriter writer = null;
        try {
            writer = PdfWriter.getInstance(document, new FileOutputStream(pdf));
            document.open();
            XMLWorkerHelper.getInstance().parseXHtml(writer, document, new FileInputStream(html), Charset.forName("UTF-8"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        document.close();

    }

    /**
     * 输出文本到本地的文件
     */
    public static void transStr2LocalHtml(String s, String path) throws IOException {
        System.out.println(path);
        File file = new File(path);
//如果文件不存在，则自动生成文件；
        if(!file.exists()){
            file.createNewFile();
        }
        try {
            OutputStreamWriter osw=new OutputStreamWriter(new FileOutputStream(path),"UTF-8");
            osw.write(s);
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("write file error");
        }
    }

    /**
     * 翻译解析到的字符串
     * @param s
     * @return
     */
    public static String translateString(String s) {
        GoogleTranslate tr = new GoogleTranslate();
        /*if(!StringUtils.isEmpty(s)) {
            s = s.replaceAll("<span class=\"[^>]*\">","").replace("</span>", "");
            Matcher m = Pattern.compile("(<div class=\"[^>]*\">)([^>]*)</div>").matcher(s);
            StringBuilder sb = new StringBuilder();
            while(m.find()) {
//            System.out.println("===================");
//            System.out.println(m.group(2));
                if(!StringUtils.isEmpty(m.group())) {
//                System.out.println(tr.translate(m.group()));
                    s = s.replace(m.group(), tr.translate(m.group()));
                }
            }
            return s;
        }
        return "";*/
       String translateResult = "";
        // 1. 去掉词之间的分割标签
        s = s.replaceAll("<span class=\"[^>]*\">","").replace("</span>", "");
        // 2. 过长的文本进行分割处理
        if(!StringUtils.isEmpty(s) && s.length() > 5000) {
            // 按照转换的 html 的class属性值进行拆分依次翻译
            /*Html h = new Html(s);
            List<Selectable> selectables = h.xpath("//div[@class='pf']").nodes();
            for(Selectable sel : selectables) {
                String oldStr = sel.toString().replaceAll("\\n", "").replaceAll(">[\\s]+<", "><");
                String trans = tr.translate(oldStr);
                System.out.println(s.contains(oldStr) + "<-----");
                s = s.replace(oldStr, trans);
            }
            translateResult = s;*/
            org.jsoup.nodes.Document document = Jsoup.parse(s);
            Elements els = document.getElementsByClass("pf");
            for(Element e : els) {
                if(!StringUtils.isEmpty(e.toString()) && e.hasText()) {
                    String trans = tr.translate(e.toString());
                    if(trans.length() > 0) {
                        Element newEle = new Element(trans);
                        e.replaceWith(newEle);
                    }
                }

            }
            translateResult = document.toString().replace("<<", "<").replace(">>", ">").replace("</<", "<");
        } else {
            translateResult = tr.translate(s.replaceAll("<span class=\"[^>]*\">", "").replace("</span>", ""));
        }
        return translateResult;
    }

    /**
     * 过长的字符的拆分
     * @param query
     * @param limit
     * @return
     */
    private static List<String> parseLongString2StringArray(String query, int limit) {
        List<String> queryList = new ArrayList<>();
        String[] split = query.split("\n");

        int sum = 0;
        StringBuffer sb = new StringBuffer();
        for(String str : split) {
            sum += str.length();
            if(sum > limit) {
                queryList.add(sb.toString());
                // 计数器的重置
                sum = 0;
                sb = new StringBuffer();
            } else {
                sb.append(str + "\n");
            }
        }
        int listStrLength = 0;
        for(String q : queryList) {
            System.out.println("--------------->" + q.length());
            listStrLength += q.length();
        }
        if(listStrLength < query.length()) {
            queryList.add(query.substring(listStrLength, query.length()));
        }
        return queryList;
    }
}
