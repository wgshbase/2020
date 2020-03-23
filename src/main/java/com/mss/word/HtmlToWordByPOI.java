package com.mss.word;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import io.renren.modules.spider.utils.ImageUtil;

public class HtmlToWordByPOI {
	CustomXWPFDocument document;
//	XWPFDocument document;
	String rootPath ;
	String pline="" ;//当前读取内容
	public void setRootPath(String rootPath){
		this.rootPath=rootPath;
	}
	public void toWord(String html,String outPath){
		//创建一个word文档
//		document = new XWPFDocument();
		document =new CustomXWPFDocument();
		FileOutputStream outputStream;
		File file = new File(outPath);
		if(!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}

		/**
		 * 文件不存在，直接创建文件
		 */
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.out.println(file.getName());
				e.printStackTrace();
				return;
			}
		}

		try {
			outputStream = new FileOutputStream(outPath);
			org.jsoup.nodes.Document  htmlRoot = Jsoup.parse(html);
			Elements  es = htmlRoot.children();
			parseNodes(es);
			if(StringUtils.isNotBlank(pline)){
				addContext(pline);
			}
			document.write(outputStream);
			outputStream.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	} 
	public void parseNodes(Elements  es){
		if(null==es||es.size()==0){
			return;
		}
		for(  org.jsoup.nodes.Element e  :es){
			String tag= e.tagName();
			if(tag.equalsIgnoreCase("img")&&e.hasAttr("src")){
				String imagPath = e.attr("src").replace("${RootPath}", rootPath);
				try {
					addImg(imagPath);
					addContext(pline);
					pline="";
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}else{
				if(tag.equalsIgnoreCase("p")||tag.equalsIgnoreCase("div")){
					addContext(pline);
					pline="";
					addContext(e.ownText());

//					Elements b = e.children().tagName("strong");
//					if(b.size() == 1 && null != b.tagName("strong")) {
//						System.out.println(e.ownText());
//						System.out.println(b.text());
//					}
////					if(null != b && !StringUtils.isNotEmpty(b.text())) {
////
////					}

				}
//				else if(tag.equalsIgnoreCase("b")){
//					addBoldContext(pline);
//					pline="";
//					String tempText = e.ownText();
//					System.out.println("Bold message: " + tempText);
//					addBoldContext(e.ownText());
//				}
				else{
					pline+=e.ownText();
				}
				if(e.childNodeSize()>0){
					parseNodes(e.children());
				}
			}
			
		}
	}
	public void addImg(String imgPath) throws Exception{
		float fixedSize=500;
		String  filetype= imgPath.substring(imgPath.lastIndexOf(".")+1);
		int pictype = getPictureType(filetype);
		File  ff= new File(imgPath);
		BufferedImage bufImg = ImageIO.read(ff);
		double rate  = ImageUtil.getPercentDouble(fixedSize,bufImg.getWidth());
		String picId=document.addPictureData(ImageUtil.zoomToBytes(bufImg, rate,filetype),pictype);
		int pid = Integer.valueOf(picId.replaceAll("[^0-9]+", ""));
		
		document.createPicture( document.createParagraph(),picId, pid,(int)(bufImg.getWidth()*rate),(int)(bufImg.getHeight()*rate),"");  
	}
	//
	public void addContext(String boldLine){
		addContext(boldLine,false);
	}
	public void addBoldContext(String boldLine){
		addContext(boldLine,true);
	}
	public void addContext(String text,boolean isBold) {
		if(StringUtils.isBlank(text)){
			return ;
		}
		XWPFParagraph paragraph = document.createParagraph();
		XWPFRun run = paragraph.createRun();
		run.setText(text);
		run.setBold(isBold);
//		run.setColor("fff000");
	} 
	  /**
     * 根据图片类型，取得对应的图片类型代码
     */
    private static int getPictureType(String picType) {
        int res = XWPFDocument.PICTURE_TYPE_PICT;
        if (picType != null) {
            if (picType.equalsIgnoreCase("png")) {
                res = XWPFDocument.PICTURE_TYPE_PNG;
            } else if (picType.equalsIgnoreCase("dib")) {
                res = XWPFDocument.PICTURE_TYPE_DIB;
            } else if (picType.equalsIgnoreCase("emf")) {
                res = XWPFDocument.PICTURE_TYPE_EMF;
            } else if (picType.equalsIgnoreCase("jpg") || picType.equalsIgnoreCase("jpeg")) {
                res = XWPFDocument.PICTURE_TYPE_JPEG;
            } else if (picType.equalsIgnoreCase("wmf")) {
                res = XWPFDocument.PICTURE_TYPE_WMF;
            }
        }
        return res;
    }
	public static void main(String[] args) {
		HtmlToWordByPOI hw = new HtmlToWordByPOI();
		hw.setRootPath("D:/tmp/");
		String html = "<p style=\"text-align:center\"><span style=\"color:red\">———————本文来源于国外网站,以下是翻译内容,向下阅读可查看原文——————</span></p>    <div itemscope itemtype=\"http://schema.org/NewsArticle\">   <meta itemprop=\"datePublished\" content=\"2016-03-28T17:15:00.0000000\">   <div class=\"slider-wrap\">    <div class=\"slider slider-article-detail \">     <div>      <div>       <img src=\"${RootPath}/www.557weatherwing.af.mil/image/72130bb9a0039dd9054940c4044b6634.jpg\">      </div>      <div class=\"media-caption\">       <p>第16天气中队  </p>      </div>     </div>    </div>   </div>   <div></div>   <div></div>   <div>    <div></div>    <div itemprop=\"articleBody\">      <span>任务</span>       <span> </span>      <span></p><p></span>      <span>利用尖端技术，科学和创新技术，为作战人员和国家机构提供响应迅速，准确且相关的天气情报。</span>       <span></p><p></span>      <span>人事和资源</span>       <span> </span>      <span></p><p></span>      <span>第16天气中队配备由55名军事和民用技术专家组成，提供综合承包商支持，组成三个航班，开发，演示和部署创新的环境情报产品，用于全球气象业务。</span>       <span></p><p></span>      <span>操作</span>       <span></p><p></span>      <span>第16届WS，为全球战斗指挥作战服务，为空军天气武器系统量身定制，整合和验证陆地和太空天气情报产品和服务。</span>       <span>他们调整和推进国防部唯一的全球云预测和分析能力，支持一系列国家机构和国防部用户。</span>       <span>最近的成就归功于第16届WS创新者，其中包括20年来空军第一个全球气象模拟系统全球空地天气开发模型的运作。</span>       <span>发明国防部首个对流规模集合预报套件，为5,000多名用户提供远程驾驶飞机支持和决策级随机产品的模型数据快速更新;</span>       <span>为未来的建模和仿真用途建立第一个天气服务实时，虚拟，构建平台;</span>       <span>通过空军天气网络服务提供决策级信息和服务，并率先推出空军首个移动天气应用程序，以便与3,000个电子飞行包移动计算平台集成。</span>       <span></p><p></span>     </div>   </div>   <div>    <div></div>   </div>  </div> <p style=\"text-align: center;\"><span>——————————————————原文————————————————————</span></p><p style=\"text-align: center;\">16th Weather Squadron</p><div itemscope itemtype=\"http://schema.org/NewsArticle\">  <meta itemprop=\"datePublished\" content=\"2016-03-28T17:15:00.0000000\">    <div class=\"slider-wrap\">   <div class=\"slider slider-article-detail \">    <div >     <div >      <img src=\"${RootPath}/www.557weatherwing.af.mil/image/72130bb9a0039dd9054940c4044b6634.jpg\">     </div>      <div class=\"media-caption\">      <p>16th Weather Squadron</p>     </div>    </div>   </div>  </div>        <div ></div>   <div ></div>   <div>   <div >   </div>   <div itemprop=\"articleBody\">    <span >Mission</span>    <span > </span>    <span ></p><p> </span>    <span >Exploit cutting-edge technologies, science, and innovations to provide responsive, accurate, and relevant weather intelligence to the warfighter and national agencies.</span>    <span ></p><p> </span>    <span >Personnel and Resources</span>    <span > </span>    <span ></p><p> </span>    <span >16th Weather Squadron manning consists of 55 military, and civilian technical experts, with integrated contractor support, organized into three flights that develop, demonstrate, and deploy innovative environmental intelligence products into global weather operations. </span>    <span ></p><p> </span>    <span >Operations</span>    <span ></p><p> 16th WS, servicing worldwide combatant command operations, tailors, integrates, and validates terrestrial and space weather intelligence products and services for the Air Force Weather Weapons System. They tune and advance the DoD’s only worldwide cloud forecast and analysis capability, supporting an array of national agencies and DoD users. Recent accomplishments credited to 16th WS innovators are operationalization of the Global Air-Land Weather Exploitation Model, the Air Force’s first global weather modeling system in 20 years; inventing the DoD’s first convective scale ensemble forecasting suite, delivering rapid refresh of model data for Remotely Piloted Aircraft support and decision-grade stochastic products to over 5,000 users; establishing the first Weather Services Live, Virtual, Constructive platform for future modeling and simulation uses; delivering decision-grade information and services through the Air Force Weather Web Services and pioneering the Air Force’s first mobile weather application for integration with 3,000 Electronic Flight Bag mobile computing platforms.</p><p> </span>   </div>  </div>    <div >   <div >   </div>  </div> </div>";
		hw.toWord(html,"d:/tmp/htmlToWord_poi_7.doc");
	}
}