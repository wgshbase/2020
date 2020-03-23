package com.mss.crawler.spiderjson.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.UUID;

/**
 * 验证码工具类
 * @Author wgsh
 * @Date wgshb on 2019/3/27 15:52
 */
public class ImageUtils {
	private static final Logger log = LogManager.getLogger(ImageUtils.class);


	public static String cutImage(String srcImg, String destImg, int x, int y, int width, int height){
		return cutImage(new File(srcImg), destImg, new java.awt.Rectangle(x, y, width, height));
	}

	public static String cutImage(File srcImg, String destImgPath, java.awt.Rectangle rect){
		String cutImgPath ="";
		File destImg = new File(destImgPath);
		if(destImg.exists()){
			String p = destImg.getPath();
			try {
				if(!destImg.isDirectory()) p = destImg.getParent();
				if(!p.endsWith(File.separator)) p = p + File.separator;
				long timestamp = System.currentTimeMillis();
				cutImage(srcImg, new java.io.FileOutputStream(p + "cut" + "_" + timestamp + "_" + srcImg.getName()), rect);
				cutImgPath = p + "cut" + "_" + timestamp + "_" + srcImg.getName();
			} catch (FileNotFoundException e) {
				log.warn("the dest image is not exist.");
			}
		}else log.warn("the dest image folder is not exist.");
		return cutImgPath;
	}

	/**
	 * <p>Title: cutImage</p>
	 * <p>Description:  根据原图与裁切size截取局部图片</p>
	 * @param srcImg    源图片
	 * @param output    图片输出流
	 * @param rect        需要截取部分的坐标和大小
	 */
	public static void cutImage(File srcImg, OutputStream output, java.awt.Rectangle rect){
		if(srcImg.exists()){
			java.io.FileInputStream fis = null;
			ImageInputStream iis = null;
			try {
				fis = new FileInputStream(srcImg);
				// ImageIO 支持的图片类型 : [BMP, bmp, jpg, JPG, wbmp, jpeg, png, PNG, JPEG, WBMP, GIF, gif]
				String types = Arrays.toString(ImageIO.getReaderFormatNames()).replace("]", ",");
				String suffix = null;
				// 获取图片后缀
				if(srcImg.getName().indexOf(".") > -1) {
					suffix = srcImg.getName().substring(srcImg.getName().lastIndexOf(".") + 1);
				}// 类型和图片后缀全部小写，然后判断后缀是否合法
				if(suffix == null || types.toLowerCase().indexOf(suffix.toLowerCase()+",") < 0){
					log.error("Sorry, the image suffix is illegal. the standard image suffix is {}." + types);
					return ;
				}
				// 将FileInputStream 转换为ImageInputStream
				iis = ImageIO.createImageInputStream(fis);
				// 根据图片类型获取该种类型的ImageReader
				ImageReader reader = ImageIO.getImageReadersBySuffix(suffix).next();
				reader.setInput(iis,true);
				ImageReadParam param = reader.getDefaultReadParam();
				param.setSourceRegion(rect);
				BufferedImage bi = reader.read(0, param);
				ImageIO.write(bi, suffix, output);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if(fis != null) fis.close();
					if(iis != null) iis.close();
					if(output!= null) {
						output.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}else {
			log.warn("the src image is not exist.");
		}
	}

	/**
	 * 返回随机的文件名称
	 */
	public static String getRandomFilename() {
		return UUID.randomUUID().toString().replace("-", "").substring(0,18);
	}

}
