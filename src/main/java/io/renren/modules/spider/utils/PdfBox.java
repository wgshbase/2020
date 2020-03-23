package io.renren.modules.spider.utils;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.*;

public class PdfBox {

     public static void main(String[] args) throws Exception {
          String mergePath = "C:/Users/44660/Desktop/testSplit/result/testSplit/";

          /*String path = "C:/Users/44660/Desktop/testSplit/";
          String fileClass = ".pdf";

          //获取文件夹下所有PDF文件
          List<String> filePathList = new ArrayList<String>();
          List<String> fileNameList = new ArrayList<String>();
          File file = new File(path);
          File[] tempList = file.listFiles();  // 获取该路径下所有的File对象
          for (int i = 0; i < tempList.length; i++) {
              if (tempList[i].isFile()) {
                  System.out.println("文件：" + tempList[i]);
                  if(tempList[i].getName().endsWith(fileClass)) {
                      filePathList.add(tempList[i].toString());  // 获取绝对路径
                      int index = tempList[i].getName().indexOf(".");
                      String name = tempList[i].getName().substring(0,index);
                      fileNameList.add(name); // 只获取名字
                  }
              }
          }
          //拆分PDF
          String outPath = "C:/Users/44660/Desktop/testSplit/result/";
          for(int k = 0;k<filePathList.size();k++){
              String newOutPath = outPath+fileNameList.get(k)+"/";
              //获取PDF总页数进行拆分

          }*/

         //拆分PDF
        splitPDF("E:\\temp\\ztbg\\pdf/RAND_RR2124_cn-ZHS.pdf","E:\\temp\\ztbg\\pdf/result/");
     }

    /**
     * 拆分PDF
     *
     */
    public static void splitPDF(String path,String outPath) throws IOException, InterruptedException {
        File file = new File(path);
        int index = file.getName().indexOf(".");
        String name = file.getName().substring(0,index);
        int pageNum = getNumberOfPages(path);
        if(pageNum >= 5){
            //拆分PDF
            for(int i = 1;i<pageNum;i=i+5){
                String newName = "";//文件拆分后的文件名
                if(i+4 >= pageNum){
                    newName = name+"_"+i+"-"+pageNum+".pdf";//文件名称
                    splitPDF(file,outPath+newName,i,pageNum);
                }else{
                    newName = name+"_"+i+"-"+(i+4)+".pdf";
                    splitPDF(file,outPath+newName,i,i+4);
                }
                System.out.println(outPath+newName);
            }
        }
    }

    /**
     * 合并PDF
     */
    public static void mergePDF(List<File> files,String outPath) throws IOException, InterruptedException {
        PDFMergerUtility mergePdf = new PDFMergerUtility();
        for (int i = files.size()-1; i >= 0;i--) {
            if(files.get(i).exists() && files.get(i).isFile()){
                // 循环添加要合并的pdf
                mergePdf.addSource(files.get(i));
            }
        }
        //设置合并生成pdf文件名称
        mergePdf.setDestinationFileName(outPath+"/merge.pdf");
        //合并pdf
        mergePdf.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
        System.out.println("合并完成!");
    }

    /**
     * 获取文件夹下所有PDF
     *
     */
    public static List<File> getFiles(String path,String fileClass){
        File file = new File(path);
        List<File> fileList = new ArrayList<File>();
        if(file.isDirectory()){
            File[] temp = file.listFiles();
            for (int i = 0; i < temp.length; i++) {
                if (temp[i].isFile()) {
                    if(temp[i].getName().endsWith(fileClass)) {
                        fileList.add(new File(temp[i].toString()));
                    }
                }
            }
        }else{
            return null;
        }
        return fileList;
    }

    /**
     * 按照创建时间排序文件
     *
     */
    public static List<File> getFileSort(String path) {

        List<File> list = getFiles(path,".pdf");

        if (list != null && list.size() > 0) {
            Collections.sort(list, new Comparator<File>() {
                public int compare(File file, File newFile) {
                    if (file.lastModified() < newFile.lastModified()) {
                        return 1;
                    } else if (file.lastModified() == newFile.lastModified()) {
                        return 0;
                    } else {
                        return -1;
                    }

                }
            });
        }

        return list;
    }


    /** 获取PDF文档元数据 **/
     public static void getPDFInformation(String file) {
          try {
              // 打开pdf文件流
              FileInputStream fis = new FileInputStream(file);
              // 加载 pdf 文档,获取PDDocument文档对象
              PDDocument document = PDDocument.load(fis);
              /** 文档属性信息 **/
              PDDocumentInformation info = document.getDocumentInformation();
              System.out.println("页数:" + document.getNumberOfPages());
              System.out.println("标题:" + info.getTitle());
              System.out.println("主题:" + info.getSubject());
              System.out.println("作者:" + info.getAuthor());
              System.out.println("关键字:" + info.getKeywords());
              System.out.println("应用程序:" + info.getCreator());
              System.out.println("pdf 制作程序:" + info.getProducer());
              System.out.println("Trapped:" + info.getTrapped());
              System.out.println("创建时间:" + dateFormat(info.getCreationDate()));
              System.out.println("修改时间:" + dateFormat(info.getModificationDate()));
              // 关闭输入流
              document.close();
              fis.close();
          } catch (Exception e) {
              e.printStackTrace();
          }
     }
     /** 打印纲要 **/
     public static void getPDFOutline(String file) {
          try {
              // 打开pdf文件流
              FileInputStream fis = new FileInputStream(file);
              // 加载 pdf 文档,获取PDDocument文档对象
              PDDocument document = PDDocument.load(fis);
              // 获取PDDocumentCatalog文档目录对象
              PDDocumentCatalog catalog = document.getDocumentCatalog();
              // 获取PDDocumentOutline文档纲要对象
              PDDocumentOutline outline = catalog.getDocumentOutline();
              // 获取第一个纲要条目（标题1）
              PDOutlineItem item = outline.getFirstChild();
              if (outline != null) {
                   // 遍历每一个标题1
                   while (item != null) {
                        // 打印标题1的文本
                        System.out.println("Item:" + item.getTitle());
                        // 获取标题1下的第一个子标题（标题2）
                        PDOutlineItem child = item.getFirstChild();
                        // 遍历每一个标题2
                        while (child != null) {
                             // 打印标题2的文本
                             System.out.println("    Child:" + child.getTitle());
                             // 指向下一个标题2
                             child = child.getNextSibling();
                        }
                        // 指向下一个标题1
                        item = item.getNextSibling();
                   }
              }
              // 关闭输入流
              document.close();
              fis.close();
          } catch (Exception e) {
              e.printStackTrace();
          }
     }
     /** 打印一级目录 **/
     public static void getPDFCatalog(String file) {
          try {
              // 打开pdf文件流
              FileInputStream fis = new FileInputStream(file);
              // 加载 pdf 文档,获取PDDocument文档对象
              PDDocument document = PDDocument.load(fis);
              // 获取PDDocumentCatalog文档目录对象
              PDDocumentCatalog catalog = document.getDocumentCatalog();
              // 获取PDDocumentOutline文档纲要对象
              PDDocumentOutline outline = catalog.getDocumentOutline();
              // 获取第一个纲要条目（标题1）
              if (outline != null) {
                   PDOutlineItem item = outline.getFirstChild();
                   // 遍历每一个标题1
                   while (item != null) {
                        // 打印标题1的文本
                        System.out.println("Item:" + item.getTitle());
                        // 指向下一个标题1
                        item = item.getNextSibling();
                   }
              }
              // 关闭输入流
              document.close();
              fis.close();
          } catch (Exception e) {
              e.printStackTrace();
          }
     }
     /**
      * 通过PDFbox获取文章总页数
      *
      * @param filePath:文件路径
      * @return
      * @throws IOException
      */
     public static int getNumberOfPages(String filePath) throws IOException, InterruptedException {
          PDDocument pdDocument = PDDocument.load(new File(filePath));
          int pages = pdDocument.getNumberOfPages();
          pdDocument.close();
          return pages;
     }
     /**
      * 通过PDFbox获取文章内容
      *
      * @param filePath
      * @return
      */
     public static String getContent(String filePath) throws IOException {
          PDFParser pdfParser = new PDFParser(new org.apache.pdfbox.io.RandomAccessFile(new File(filePath), "rw"));
          pdfParser.parse();
          PDDocument pdDocument = pdfParser.getPDDocument();
          PDFTextStripper pdfTextStripper = new PDFTextStripper();
          String text = pdfTextStripper.getText(pdDocument);
          pdDocument.close();
          return text;
     }
     /**
      * 提取部分页面文本
      *
      * @param filePath
      *            pdf文档路径
      * @param startPage
      *            开始页数
      * @param endPage
      *            结束页数
      */
     public static String getContent(String filePath, int startPage, int endPage) throws IOException {
          PDFParser pdfParser = new PDFParser(new org.apache.pdfbox.io.RandomAccessFile(new File(filePath), "rw"));
          pdfParser.parse();
          PDDocument pdDocument = pdfParser.getPDDocument();
          PDFTextStripper pdfTextStripper = new PDFTextStripper();
          pdfTextStripper.setStartPage(startPage);
          pdfTextStripper.setEndPage(endPage);
          String text = pdfTextStripper.getText(pdDocument);
          pdDocument.close();
          return text;
     }
     /**
      * 坐标提取部分页面文本
      *
      * @param filePath
      *            pdf文档路径
      * @param startPage
      *            开始页数
      * @param endPage
      *            结束页数
      */
     public static void getContentByArea(String filePath, int startPage, int endPage) throws IOException {
        PDFParser pdfParser = new PDFParser(new org.apache.pdfbox.io.RandomAccessFile(new File(filePath), "rw"));
        pdfParser.parse();
        PDDocument pdDocument = pdfParser.getPDDocument();
		PDFTextStripperByArea stripper = new PDFTextStripperByArea();
		stripper.setSortByPosition(true);
		stripper.addRegion("title", new Rectangle(30, 60, 600, 20));//(x坐标，y坐标，长，宽)
		stripper.addRegion("content", new Rectangle(90, 487, 100, 200));
		
		PDPage firstPage = pdDocument.getPage(0);
		stripper.extractRegions(firstPage);
		System.out.println(stripper.getTextForRegion("title"));
		System.out.println(stripper.getTextForRegion("content"));
		pdDocument.close();
     }
     /**
      * 通过PDFbox获取文章内容并保存
      *
      * @param filePath
      *            pdf文档路径
      * @param outPath
      *            文本保存路径
      * @return
      */
     public static void getContent(String filePath, String outPath) throws IOException {
          PDFParser pdfParser = new PDFParser(new org.apache.pdfbox.io.RandomAccessFile(new File(filePath), "rw"));
          pdfParser.parse();
          File dir = new File(outPath);
          File fileParent = dir.getParentFile();
          if (!fileParent.exists()){
              fileParent.mkdirs();
          }
          PDDocument pdDocument = pdfParser.getPDDocument();
          PDFTextStripper pdfTextStripper = new PDFTextStripper();
          Writer writer = new OutputStreamWriter(new FileOutputStream(outPath));
          pdfTextStripper.writeText(pdDocument, writer);
          writer.close();
          pdDocument.close();
     }
     /**
      * 提取部分页面文本并保存
      *
      * @param filePath
      *            pdf文档路径
      * @param startPage
      *            开始页数
      * @param endPage
      *            结束页数
      * @param outPath
      *            文本保存路径
      */
     public static void getContent(String filePath, String outPath, int startPage, int endPage) throws IOException {
          PDFParser pdfParser = new PDFParser(new org.apache.pdfbox.io.RandomAccessFile(new File(filePath), "rw"));
          pdfParser.parse();
          File dir = new File(outPath);
          File fileParent = dir.getParentFile();
          if (!fileParent.exists()){
              fileParent.mkdirs();
          }
          PDDocument pdDocument = pdfParser.getPDDocument();
          PDFTextStripper pdfTextStripper = new PDFTextStripper();
          pdfTextStripper.setStartPage(startPage);
          pdfTextStripper.setEndPage(endPage);
          Writer writer = new OutputStreamWriter(new FileOutputStream(outPath));
          pdfTextStripper.writeText(pdDocument, writer);
          writer.close();
          pdDocument.close();
     }
     /**
      * 通过PDFbox生成文件的缩略图
      *
      * @param filePath：文件路径
      * @param outPath：输出图片目录
      * @throws IOException
      */
     public static void getThumbnails(String filePath, String outPath) throws IOException {
          // 利用PdfBox生成图像
          PDDocument pdDocument = PDDocument.load(new File(filePath));
          File dir = new File(outPath);
          if (!dir.exists()){
              dir.mkdirs();
          }
          PDFRenderer renderer = new PDFRenderer(pdDocument);
          // 构造图片
          BufferedImage img_temp = renderer.renderImageWithDPI(0, 30, ImageType.RGB);
          // 设置图片格式
          Iterator<ImageWriter> it = ImageIO.getImageWritersBySuffix("png");
          // 将文件写出
          ImageWriter writer = (ImageWriter) it.next();
          ImageOutputStream imageout = ImageIO.createImageOutputStream(new FileOutputStream(outPath + "thumbnail.png"));
          writer.setOutput(imageout);
          writer.write(new IIOImage(img_temp, null, null));
          img_temp.flush();
          imageout.flush();
          imageout.close();
          // Warning: You did not close a PDF Document
          pdDocument.close();
     }
     /**
      * 提取图片并分别另存为图片。
      *
      * @param filePath
      *            文件路径
      * @param outPath
      *            输出图片目录
      * @param startPage
      *            开始页数
      * @param endPage
      *            结束页数
      * @throws IOException
      */
     public static void getImages(String filePath, String outPath, int startPage, int endPage) throws IOException {
          // 利用PdfBox生成图像
          PDDocument pdDocument = PDDocument.load(new File(filePath));
          File dir = new File(outPath);
          if (!dir.exists()){
              dir.mkdirs();
          }
          int j = 1;
          startPage = startPage > 0 ? startPage - 1 : startPage;
          for (int i = startPage; i < endPage; i++) {
              // 取得第i页
              PDPage page = pdDocument.getPage(i);
              PDResources resources = page.getResources();
              Iterable xobjects = resources.getXObjectNames();
              if (xobjects != null) {
                   Iterator imageIter = xobjects.iterator();
                   while (imageIter.hasNext()) {
                        COSName key = (COSName) imageIter.next();
                        PDImageXObject image = (PDImageXObject) resources.getXObject(key);
                        BufferedImage img_temp = image.getImage();
                        // 将PDF文档中的图片 分别另存为图片。
                        Iterator<ImageWriter> it = ImageIO.getImageWritersBySuffix("png");
                        // 将文件写出
                        ImageWriter writer = (ImageWriter) it.next();
                        ImageOutputStream imageout = ImageIO
                                  .createImageOutputStream(new FileOutputStream(outPath + String.format("%04d", j) + ".png"));
                        writer.setOutput(imageout);
                        writer.write(new IIOImage(img_temp, null, null));
                        img_temp.flush();
                        imageout.flush();
                        imageout.close();
                        j++;
                   }
              }
          }
          pdDocument.close();
     }
     /**
      * 提取部分页图片并分别另存为图片。
      *
      * @param filePath
      *            文件路径
      * @param outPath
      *            输出图片目录
      * @throws IOException
      */
     public static void getImages(String filePath, String outPath) throws IOException {
          // 利用PdfBox生成图像
          PDDocument pdDocument = PDDocument.load(new File(filePath));
          File dir = new File(outPath);
          if (!dir.exists()){
              dir.mkdirs();
          }
          int j = 1;
          int pageNum = pdDocument.getNumberOfPages();
          for (int i = 0; i < pageNum; i++) {
              // 取得第i页
              PDPage page = pdDocument.getPage(i);
              PDResources resources = page.getResources();
              Iterable xobjects = resources.getXObjectNames();
              if (xobjects != null) {
                   Iterator imageIter = xobjects.iterator();
                   while (imageIter.hasNext()) {
                        COSName key = (COSName) imageIter.next();
                        PDImageXObject image = (PDImageXObject) resources.getXObject(key);
                        BufferedImage img_temp = image.getImage();
                        // 将PDF文档中的图片 分别另存为图片。
                        Iterator<ImageWriter> it = ImageIO.getImageWritersBySuffix("png");
                        // 将文件写出
                        ImageWriter writer = (ImageWriter) it.next();
                        ImageOutputStream imageout = ImageIO
                                  .createImageOutputStream(new FileOutputStream(outPath + String.format("%04d", j) + ".png"));
                        writer.setOutput(imageout);
                        writer.write(new IIOImage(img_temp, null, null));
                        img_temp.flush();
                        imageout.flush();
                        imageout.close();
                        j++;
                   }
              }
          }
          pdDocument.close();
     }
     /**
      * 提取图片并保存PDF
      *
      * @param filePath
      *            文件路径
      * @param outPath
      *            输出PDF目录
      * @throws IOException
      */
     public static void getImagesPDF(String filePath, String outPath) throws IOException {
          // 利用PdfBox生成图像
          PDDocument pdDocument = PDDocument.load(new File(filePath));
          PDDocument pdDocumentOut = new PDDocument();
          pdDocumentOut.addPage(new PDPage());
          File dir = new File(outPath);
          if (!dir.exists()){
              dir.mkdirs();
          }
          int j = 1;
          int pageNum = pdDocument.getNumberOfPages();
          for (int i = 0; i < pageNum; i++) {
              // 取得第i页
              PDPage page = pdDocument.getPage(i);
              PDPage page1 = pdDocumentOut.getPage(0);
              PDResources resources = page.getResources();
              Iterable xobjects = resources.getXObjectNames();
              if (xobjects != null) {
                   Iterator imageIter = xobjects.iterator();
                   while (imageIter.hasNext()) {
                        COSName key = (COSName) imageIter.next();
                        PDImageXObject image = (PDImageXObject) resources.getXObject(key);
                        PDPageContentStream contentStream = new PDPageContentStream(pdDocumentOut, page1, AppendMode.APPEND,
                                  true);
                        float scale = 1f;
                        contentStream.drawImage(image, 20, 20, image.getWidth() * scale, image.getHeight() * scale);
                        contentStream.close();
                        pdDocumentOut.save(outPath + String.format("%04d", j) + ".pdf");
                        j++;
                   }
              }
          }
          pdDocument.close();
     }
     /**
      * 拆分每页并分别另存为图片。
      *
      * @param filePath
      *            文件路径
      * @param outPath
      *            输出图片目录
      * @throws IOException
      */
     public static void splitImages(String filePath, String outPath) throws IOException {
          // 利用PdfBox生成图像
          PDDocument pdDocument = PDDocument.load(new File(filePath));
          PDFRenderer renderer = new PDFRenderer(pdDocument);
          File dir = new File(outPath);
          if (!dir.exists()){
              dir.mkdirs();
          }
          int j = 1;
          int pageNum = pdDocument.getNumberOfPages();
          System.out.println("总页数："+pageNum);
          // 遍历每一页
          for (int i = 0; i < pageNum; i++) {
              // 取得第i页 dpi越大越清晰，文件越大
              BufferedImage img_temp = renderer.renderImageWithDPI(i, 300, ImageType.RGB);
              // 将PDF文档中的图片 分别另存为图片。
              Iterator<ImageWriter> it = ImageIO.getImageWritersBySuffix("png");
              // 将文件写出
              ImageWriter writer = (ImageWriter) it.next();
              ImageOutputStream imageout = ImageIO.createImageOutputStream(new FileOutputStream(outPath + String.format("%04d", j) + ".png"));
              writer.setOutput(imageout);
              writer.write(new IIOImage(img_temp, null, null));
              img_temp.flush();
              imageout.flush();
              imageout.close();
              j++;
          }
          pdDocument.close();
     }
     /**
      * 拆分部分页并分别另存为图片。
      *
      * @param filePath
      *            文件路径
      * @param outPath
      *            输出图片目录
      * @param startPage
      *            开始页数
      * @param endPage
      *            结束页数
      * @throws IOException
      */
     public static void splitImages(String filePath, String outPath, int startPage, int endPage) throws IOException {
          // 利用PdfBox生成图像
          PDDocument pdDocument = PDDocument.load(new File(filePath));
          PDFRenderer renderer = new PDFRenderer(pdDocument);
          File dir = new File(outPath);
          if (!dir.exists()){
              dir.mkdirs();
          }
          int j = startPage;
          startPage = startPage > 0 ? startPage - 1 : startPage;
          for (int i = startPage; i < endPage; i++) {
              // 取得第i页
              BufferedImage img_temp = renderer.renderImageWithDPI(i, 300, ImageType.RGB);
              // 将PDF文档中的图片 分别另存为图片。
              Iterator<ImageWriter> it = ImageIO.getImageWritersBySuffix("png");
              // 将文件写出
              ImageWriter writer = (ImageWriter) it.next();
              ImageOutputStream imageout = ImageIO.createImageOutputStream(new FileOutputStream(outPath + String.format("%04d", j) + ".png"));
              writer.setOutput(imageout);
              writer.write(new IIOImage(img_temp, null, null));
              img_temp.flush();
              imageout.flush();
              imageout.close();
              j++;
          }
          pdDocument.close();
     }
     /**
      * 拆分部分PDF
      *
      * @param
      * @param outPath
      *            输出文件路径
      * @param startPage
      *            开始页数
      * @param endPage
      *            结束页数
      * @throws IOException
      */
     public static void splitPDF(File file, String outPath, int startPage, int endPage) throws IOException {
          // 利用PdfBox生成图像
          PDDocument pdDocument = PDDocument.load(file);
          PDDocument pdDocumentOut = new PDDocument();
          File dir = new File(outPath);
          File fileParent = dir.getParentFile();
          if (!fileParent.exists()){
              fileParent.mkdirs();
          }
          startPage = startPage > 0 ? startPage - 1 : startPage;
          for (int i = startPage; i < endPage; i++) {
              pdDocumentOut.addPage(pdDocument.getPage(i));
          }
          pdDocumentOut.save(outPath);
          pdDocument.close();
          pdDocumentOut.close();
     }
     public static String dateFormat(Calendar calendar) {
          if (null == calendar)
              return null;
          String date = null;
          String pattern = "yyyy-MM-dd HH:mm:ss";
          SimpleDateFormat format = new SimpleDateFormat(pattern);
          date = format.format(calendar.getTime());
          return date == null ? "" : date;
     }
}






