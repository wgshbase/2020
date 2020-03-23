package com.mss.word;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.rtf.RtfWriter2;

import io.renren.modules.spider.utils.ImageUtil;

public class HtmlToWordByItext {
	Document document;
	String rootPath ;
	String pline="" ;//当前读取内容
	BaseFont bfChinese;// 设置中文字体
	Font contextFont;// 正文字体风格
	Font contextFont2;// 正文字体风格2
	
	public HtmlToWordByItext() {
		try {
			bfChinese = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
			contextFont = new Font(bfChinese, 12, Font.BOLD);
			contextFont2 = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 10, Font.NORMAL);
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
				
	}
	public void setRootPath(String rootPath){
		this.rootPath=rootPath;
	}
	public void toWord(String html,String outPath){
		try {
		// 设置纸张大小
		document = new Document(PageSize.A4);
		File file = new File(outPath);
		RtfWriter2.getInstance(document, new FileOutputStream(file));
		document.open();
		org.jsoup.nodes.Document  htmlRoot = Jsoup.parse(html);
		Elements  es = htmlRoot.children();
		parseNodes(es);
		if(StringUtils.isNotBlank(pline)){
			addContext(pline);
		}
		document.close();
		// 得到输入流
		// wordFile = new ByteArrayInputStream(baos.toByteArray());
		// baos.close();
		} catch (FileNotFoundException e) {
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
				}else{
					pline+=e.ownText();
				}
				if(e.childNodeSize()>0){
					parseNodes(e.children());
				}
			}
			
		}
	}
	public void addImg(String imgPath) throws Exception{
		// 添加图片 Image.getInstance即可以放路径又可以放二进制字节流
		//imgPath = "D:/hymax/root/www.thespacereview.com/image/1b34df8cb3fd49bbee9b9edee8fa98e6.jpg";
		Image img = Image.getInstance(imgPath);
		img.setAbsolutePosition(0, 0);
		img.setAlignment(Image.ALIGN_CENTER);// 设置图片显示位置
		int fixedSize = 500;
		if(img.getWidth()>fixedSize){
			int pc = ImageUtil.getPercent(fixedSize, img.getWidth());
			img.scalePercent(pc);// 表示显示的大小为原尺寸的50%
		}
		//img.scalePercent(img.getHeight(), img.getWidth());// 直接设定显示尺寸
		//img.scaleAbsolute(img.getHeight(), img.getWidth());// 直接设定显示尺寸
		// img.scalePercent(25, 12);//图像高宽的显示比例
		// img.setRotation(30);//图像旋转一定角度
		document.add(img);
	}
	
	//
	public void addBoldLine(String boldLine) throws Exception{
		Paragraph pg = new Paragraph(boldLine);
		// 正文格式左对齐
		pg.setAlignment(Element.ALIGN_LEFT);
		pg.setFont(contextFont);
		// 离上一段落空的行数
		//pg.setSpacingBefore(20);
		document.add(pg);
	} 
	public void addContext(String text) {
		if(StringUtils.isBlank(text)){
			return ;
		}
		Paragraph pg = new Paragraph(text);
		// 正文格式左对齐
		pg.setAlignment(Element.ALIGN_LEFT);
		pg.setFont(contextFont2);
		// 离上一段落空的行数
		//pg.setSpacingBefore(20);
		try {
			document.add(pg);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	} 
	
	public String exportDoc(String html,String rootPath , String outPath) {
		
		try {
			// 设置纸张大小
			Document document = new Document(PageSize.A4);
			File file = new File(outPath);
			RtfWriter2.getInstance(document, new FileOutputStream(file));
			document.open();
			// 设置中文字体
			BaseFont bfChinese = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
			// 正文字体风格
			Font contextFont = new Font(bfChinese, 12, Font.BOLD);
			// Font contextFont = new Font(bfChinese, 11, Font.NORMAL);
			List<String> list = new ArrayList<>();
			
			for (String string : list) {
				// 标题 
				String code = "";
				Paragraph codeStyle = new Paragraph(code);
				codeStyle.setAlignment(Element.ALIGN_LEFT);// 正文格式左对齐
				codeStyle.setFont(contextFont);
				codeStyle.setSpacingBefore(20);// 离上一段落（标题）空的行数
				// context.setFirstLineIndent(0);// 设置第一行空的列数
				document.add(codeStyle);
				
				// result
				String result = "result ：";
				Paragraph resultStyle = new Paragraph(result);
				// 正文格式左对齐
				resultStyle.setAlignment(Element.ALIGN_LEFT);
				resultStyle.setFont(contextFont);
				// 离上一段落空的行数
			//	resultStyle.setSpacingBefore(10);
				// 设置第一行空的列数
				// context.setFirstLineIndent(0);
				document.add(resultStyle);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (BadElementException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	public static void test(String outPath) {
		
		try {
			// 设置纸张大小
			Document document = new Document(PageSize.A4);
			File file = new File(outPath);
			RtfWriter2.getInstance(document, new FileOutputStream(file));
			document.open();
			// 设置中文字体
			BaseFont bfChinese = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
			// 标题字体风格
			// Font titleFont = new Font(bfChinese, 12, Font.BOLD);
			// Paragraph title = new Paragraph("测试结果");
			// 设置标题格式对齐方式
			// title.setAlignment(Element.ALIGN_CENTER);
			// title.setFont(titleFont);
			// document.add(title);
			// 正文字体风格
			Font contextFont = new Font(bfChinese, 12, Font.BOLD);
			// Font contextFont = new Font(bfChinese, 11, Font.NORMAL);
			List<String> list = new ArrayList<>();
			list.add("a");
			for (String s : list) {
				// code
				String code = "code ：  ";
				Paragraph codeStyle = new Paragraph(code);
				// 正文格式左对齐
				codeStyle.setAlignment(Element.ALIGN_LEFT);
				codeStyle.setFont(contextFont);
				// 离上一段落（标题）空的行数
				//codeStyle.setSpacingBefore(20);
				// 设置第一行空的列数
				// context.setFirstLineIndent(0);
				document.add(codeStyle);
				// 操作
				String codeContent = "点击";
				Paragraph codeContentStyle = new Paragraph(codeContent,
						FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 11, Font.UNDERLINE, new Color(0, 0, 255)));
				// 离上一段落（标题）空的行数
				codeContentStyle.setSpacingBefore(5);
				document.add(codeContentStyle);
				// result
				String result = "result ：";
				Paragraph resultStyle = new Paragraph(result);
				// 正文格式左对齐
				resultStyle.setAlignment(Element.ALIGN_LEFT);
				resultStyle.setFont(contextFont);
				// 离上一段落空的行数
				resultStyle.setSpacingBefore(10);
				// 设置第一行空的列数
				// context.setFirstLineIndent(0);
				document.add(resultStyle);
				// 利用类FontFactory结合Font和Color可以设置各种各样字体样式
				// 结果
				String resultContent = "成功";
				Paragraph resultContentStyle = null;
				if (resultContent.equals("成功")) {
					resultContentStyle = new Paragraph(resultContent,
							FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 11, Font.UNDERLINE, new Color(0, 255, 0)));
				} else {
					resultContentStyle = new Paragraph(resultContent,
							FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 11, Font.UNDERLINE, new Color(255, 0, 0)));
				}
				// 离上一段落空的行数
				resultContentStyle.setSpacingBefore(5);
				document.add(resultContentStyle);
				// 添加图片 Image.getInstance即可以放路径又可以放二进制字节流
				String imgPath = "D:/hymax/root/www.thespacereview.com/image/1b34df8cb3fd49bbee9b9edee8fa98e6.jpg";
				Image img = Image.getInstance(imgPath);
				img.setAbsolutePosition(0, 0);
				img.setAlignment(Image.ALIGN_CENTER);// 设置图片显示位置
				img.scalePercent(30);// 表示显示的大小为原尺寸的50%
				// img.scaleAbsolute(60, 60);// 直接设定显示尺寸
				// img.scalePercent(25, 12);//图像高宽的显示比例
				// img.setRotation(30);//图像旋转一定角度
				document.add(img);
				String log = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
				if (log != null && !"".equals(log)) {
					Paragraph exceptionStyle = new Paragraph("异常信息 ：");
					// 正文格式左对齐
					exceptionStyle.setAlignment(Element.ALIGN_LEFT);
					exceptionStyle.setFont(contextFont);
					// 离上一段落空的行数
					//exceptionStyle.setSpacingBefore(20);
					document.add(exceptionStyle);
					document.add(new Paragraph(log, FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 10, Font.NORMAL)));
				}
			}

			document.close();
			// 得到输入流
			// wordFile = new ByteArrayInputStream(baos.toByteArray());
			// baos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (BadElementException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		//test("d:/tmp/test.doc");
		HtmlToWordByItext hw = new HtmlToWordByItext();
		hw.setRootPath("D:/tmp/");
		String html = "<p style=\"text-align:center\"><span style=\"color:red\">————————————————————本文来源于国外网站,以下是翻译内容,向下阅读可查看原文————————————————————</span></p>    <div itemscope itemtype=\"http://schema.org/NewsArticle\">   <meta itemprop=\"datePublished\" content=\"2016-03-28T17:15:00.0000000\">   <div class=\"slider-wrap\">    <div class=\"slider slider-article-detail \">     <div>      <div>       <img src=\"${RootPath}/www.557weatherwing.af.mil/image/72130bb9a0039dd9054940c4044b6634.jpg\">      </div>      <div class=\"media-caption\">       <p>第16天气中队  </p>      </div>     </div>    </div>   </div>   <div></div>   <div></div>   <div>    <div></div>    <div itemprop=\"articleBody\">      <span>任务</span>       <span> </span>      <span></p><p></span>      <span>利用尖端技术，科学和创新技术，为作战人员和国家机构提供响应迅速，准确且相关的天气情报。</span>       <span></p><p></span>      <span>人事和资源</span>       <span> </span>      <span></p><p></span>      <span>第16天气中队配备由55名军事和民用技术专家组成，提供综合承包商支持，组成三个航班，开发，演示和部署创新的环境情报产品，用于全球气象业务。</span>       <span></p><p></span>      <span>操作</span>       <span></p><p></span>      <span>第16届WS，为全球战斗指挥作战服务，为空军天气武器系统量身定制，整合和验证陆地和太空天气情报产品和服务。</span>       <span>他们调整和推进国防部唯一的全球云预测和分析能力，支持一系列国家机构和国防部用户。</span>       <span>最近的成就归功于第16届WS创新者，其中包括20年来空军第一个全球气象模拟系统全球空地天气开发模型的运作。</span>       <span>发明国防部首个对流规模集合预报套件，为5,000多名用户提供远程驾驶飞机支持和决策级随机产品的模型数据快速更新;</span>       <span>为未来的建模和仿真用途建立第一个天气服务实时，虚拟，构建平台;</span>       <span>通过空军天气网络服务提供决策级信息和服务，并率先推出空军首个移动天气应用程序，以便与3,000个电子飞行包移动计算平台集成。</span>       <span></p><p></span>     </div>   </div>   <div>    <div></div>   </div>  </div> <p style=\"text-align: center;\"><span>————————————————————原文————————————————————</span></p><p style=\"text-align: center;\">16th Weather Squadron</p><div itemscope itemtype=\"http://schema.org/NewsArticle\">  <meta itemprop=\"datePublished\" content=\"2016-03-28T17:15:00.0000000\">    <div class=\"slider-wrap\">   <div class=\"slider slider-article-detail \">    <div >     <div >      <img src=\"${RootPath}/www.557weatherwing.af.mil/image/72130bb9a0039dd9054940c4044b6634.jpg\">     </div>      <div class=\"media-caption\">      <p>16th Weather Squadron</p>     </div>    </div>   </div>  </div>        <div ></div>   <div ></div>   <div>   <div >   </div>   <div itemprop=\"articleBody\">    <span >Mission</span>    <span > </span>    <span ></p><p> </span>    <span >Exploit cutting-edge technologies, science, and innovations to provide responsive, accurate, and relevant weather intelligence to the warfighter and national agencies.</span>    <span ></p><p> </span>    <span >Personnel and Resources</span>    <span > </span>    <span ></p><p> </span>    <span >16th Weather Squadron manning consists of 55 military, and civilian technical experts, with integrated contractor support, organized into three flights that develop, demonstrate, and deploy innovative environmental intelligence products into global weather operations. </span>    <span ></p><p> </span>    <span >Operations</span>    <span ></p><p> 16th WS, servicing worldwide combatant command operations, tailors, integrates, and validates terrestrial and space weather intelligence products and services for the Air Force Weather Weapons System. They tune and advance the DoD’s only worldwide cloud forecast and analysis capability, supporting an array of national agencies and DoD users. Recent accomplishments credited to 16th WS innovators are operationalization of the Global Air-Land Weather Exploitation Model, the Air Force’s first global weather modeling system in 20 years; inventing the DoD’s first convective scale ensemble forecasting suite, delivering rapid refresh of model data for Remotely Piloted Aircraft support and decision-grade stochastic products to over 5,000 users; establishing the first Weather Services Live, Virtual, Constructive platform for future modeling and simulation uses; delivering decision-grade information and services through the Air Force Weather Web Services and pioneering the Air Force’s first mobile weather application for integration with 3,000 Electronic Flight Bag mobile computing platforms.</p><p> </span>   </div>  </div>    <div >   <div >   </div>  </div> </div>";
		hw.toWord(html,"d:/tmp/htmlToWord_itext_2.doc");
	}
}