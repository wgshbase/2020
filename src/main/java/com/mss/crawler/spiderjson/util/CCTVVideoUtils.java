package com.mss.crawler.spiderjson.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.renren.modules.spider.utils.CSVUtils;
import io.renren.modules.spider.utils.ZipCompressor;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @Author wgsh
 * @Date wgshb on 2019/4/28 15:39
 */
public class CCTVVideoUtils {
	private static final Logger log = LogManager.getLogger(CCTVVideoUtils.class);
	/**
	 * 返回视频的列表请求页
	 * @param url
	 * @return
	 */
	public static String getVideolistRequest(String url) {

		StringBuilder sb = new StringBuilder();
		if(StringUtils.isEmpty(url)) {
			return sb.toString();
		}
		sb.append("http://api.cntv.cn/video/getVideoListByTopicIdInfo?videoid=");
		String videoId = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
		sb.append(videoId);
		sb.append("&topicid=TOPC1451526164984187&serviceId=tvcctv&type=0&t=jsonp&cb=setItem0=");

		return sb.toString();
	}

	/**
	 * 通过返回的结果结合 url 获取对应的视频的详情链接
	 * @param url
	 * @param result
	 * @return
	 */
	public static String getTargetVideoUrl(String url, String result) {
		//1. 获取 pid
		if(result.contains("setItem0=(") && result.contains(");")) {
			result = result.substring(result.indexOf("setItem0=(") + 10, result.indexOf(");"));
		}

		JSONObject resultJson = JSONObject.parseObject(result);
		JSONArray data = resultJson.getJSONArray("data");

		String pid = "";

		for(int i = 0; i < data.size(); i++) {
			JSONObject obj = (JSONObject) data.get(i);
			String videoUrl = obj.getString("video_url");
			if(url.equals(videoUrl)) {
				pid = obj.getString("guid");
				break;
			}
		}
		if("".equals(pid)) {
			return "";
		}
		// 2. 拼接请求地址
		StringBuilder sb = new StringBuilder();
		sb.append("http://vdn.apps.cntv.cn/api/getHttpVideoInfo.do?pid=").append(pid).append("&tz=-8&from=000news&url=").append(url).append("&idl=32&idlr=32&modifyed=false&tsp=").append(System.currentTimeMillis()/1000 + "").append("&vn=1540&uid=7E3F13CB5A3B17711BC1E66AB85433A4");

		return sb.toString();
	}

	/**
	 * 获取下载链接的集合
	 * @param videoResult
	 * @return
	 */
	public static Map<String, Object> getTargetVideoDownUrl(String videoResult) {
		Map<String, Object> downloadinfo = new HashMap<>();
		List<String> result = new ArrayList<>();

		videoResult = videoResult.substring(videoResult.indexOf("<body>") + 6, videoResult.indexOf("</body>"));

		JSONObject resultJson = JSONObject.parseObject(videoResult);
		JSONObject video = resultJson.getJSONObject("video");
		int validChapterNum = Integer.parseInt(video.getString("validChapterNum"));
		// 下载的分辨率设置, 值越大分辨率越高
		JSONArray chapters = video.getJSONArray("chapters" + (validChapterNum - 3));
		for(int i = 0; i < chapters.size(); i++) {
			JSONObject obj = chapters.getJSONObject(i);
			result.add(obj.getString("url"));
			if(!downloadinfo.containsKey("headimg")) {
				downloadinfo.put("headimg", obj.getString("image"));
			}
		}
		if(result.size() > 0) {
			downloadinfo.put("title", resultJson.getString("title"));
			downloadinfo.put("list", result);
		}
		return downloadinfo;
	}

	/**
	 * 下载视频文件到本地
	 * @param downloadInfo
	 * @param targetFile 目标文件夹
	 * @param zippath 将对应的文件夹打包到什么位置
	 */
	public static void downFile2Local(Map<String,Object> downloadInfo, String targetFile, String zippath) {
		String title = ((String)downloadInfo.get("title")).replace(" ", "");
		File file = new File(targetFile);
		targetFile = targetFile + title + File.separator;
		if(file.exists()) {
			List<String> filenames = Arrays.asList(file.list());

			if(filenames.contains(title)) {
				log.info("视频[ " + title + " ]已经存在!!!");
				return;
			}
		}
		String headimg = (String) downloadInfo.get("headimg");
		String suffix = headimg.substring(headimg.lastIndexOf("."), headimg.length());
		List<String> result = (List<String>) downloadInfo.get("list");
		String imgZipPath = zippath + title + File.separator + title + suffix;
		String imgPath = targetFile + title + suffix;
		try {
			HttpFileUtil.getInstance().getVideoFileTo(headimg, imgPath);
			// 文件复制
			CSVUtils.copySingleFile(imgPath, imgZipPath);

		} catch (IOException e) {
			log.info("下载封面失败...");
			e.printStackTrace();
		}

		String filename = UUID.randomUUID().toString().replace("-", "").substring(0, 18);
		List<String> videoNameList = new ArrayList<>();
		videoNameList.add(imgZipPath);
		for(String u : result) {
			try {
				String index = result.indexOf(u) + 1 + "";
				log.info("第 " + index + " 个视频开始下载...");
				String fullname = targetFile + filename + "_" + index + ".MP4";
				String videoZipName = zippath + title + File.separator + filename + "_" + index + ".MP4";
				HttpFileUtil.getInstance().getVideoFileTo(u, fullname);
				CSVUtils.copySingleFile(fullname, videoZipName);
				videoNameList.add(videoZipName);
				addInfo2Text(fullname, filename);
				log.info("下载完成...");
			} catch (IOException e) {
				log.info("下载视频[ " + title +" ]失败");
				e.printStackTrace();
			}
		}
		// 分别讲视频文件打包, 不进行视频的整合...
 		ZipCompressor zc = new ZipCompressor(zippath + title + ".zip");
		// zc.compress(zippath + title);
		zc.compress(videoNameList.toArray(new String[videoNameList.size()]));
		// 删除临时文件
		HttpFileUtil.deleteDir(new File(zippath + title + File.separator));

		/*if(result.size() > 1) {
			// 执行视频的合并操作
			log.info("开始视频合并");
			union(targetFile, targetFile + filename + ".MP4");
			log.info("视频合并完成");
			try {
				Thread.sleep(15000l);
				CSVUtils.copySingleFile(targetFile + filename + ".MP4", zippath + title + File.separator + filename + ".MP4");
				ZipCompressor zc = new ZipCompressor(zippath + title + ".zip");
				// zc.compress(zippath + title);
				zc.compress(imgZipPath, zippath + title + File.separator + filename + ".MP4");
				log.info("打包成功");
				// 删除临时文件
				HttpFileUtil.deleteDir(new File(zippath + title + File.separator));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}*/

	}

	/**
	 * 视频合并, 打包借助 ffmpeg 工具进行视频的合并
	 */
	public static void union(String filePath, String targetFile) {
		try {
			File file = new File(filePath + "temp.bat");
			if(file.exists()) {
				file.delete();
			}

			file.createNewFile();

			filePath = java.net.URLDecoder.decode(filePath,"utf-8");

			//0. 构建临时 bat 文件
			BufferedWriter bw = new BufferedWriter(
					new OutputStreamWriter(
							new FileOutputStream(file), Charset.forName("GBK")));

			// String cmd = "ffmpeg.exe -f concat -i filelist.txt -c copy " + targetFile;
			String cmd = "ffmpeg.exe -f concat -safe 0 -i filelist.txt -c copy " + targetFile;
			bw.write(cmd);
			bw.flush();
			bw.close();

			//1. 找到对应的文件的目录, 打开指定的目录 Runtime.getRuntime().exec("你的命令",null,new File("d:/test"));
			Runtime runtime = Runtime.getRuntime();
			// Process exec = runtime.exec("cmd /c start " + file.getAbsolutePath());
			// System.out.println("Videos union successfully...");

			// Runtime.getRuntime().exec("你的命令",null,new File("d:/test"));
			runtime.exec("cmd /c start " + file.getAbsolutePath(), null, new File(filePath));
			// 杀掉进程, 关闭掉启动的 cmd 窗口
			killProcess(runtime);
		} catch (IOException e) {
			log.error("Fail to union movies ...");
			e.printStackTrace();
		}

	}

	/**
	 * 杀死当前的进程
	 * @param rt
	 */
	public static void killProcess(Runtime rt){
		try {
			rt.exec("cmd.exe /C start wmic process where name='cmd.exe' call terminate");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 添加视频名称到文件
	 * @param fullname
	 */
	public static void addInfo2Text(String fullname, String filename) {
		File file = new File(fullname);
		File parentFile = file.getParentFile();
		File filelist = new File(parentFile + File.separator + "filelist.txt");
		FileWriter bw = null;
		try {
			if(!filelist.exists()) {
				filelist.createNewFile();
			}
			bw = new FileWriter(filelist, true);
			bw.write("file '" + fullname + "'\r\n");
			bw.flush();
			bw.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		union("D:\\data\\webmagic\\CNTV_防务新观察\\《防务新观察》20190419解放军绕台岛巡航民进党当局破坏两岸关系图谋不会得逞", "D:\\data\\webmagic\\CNTV_防务新观察\\《防务新观察》20190419解放军绕台岛巡航民进党当局破坏两岸关系图谋不会得逞\\1.mp4");

	}
}
