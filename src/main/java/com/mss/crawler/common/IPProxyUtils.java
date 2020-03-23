package com.mss.crawler.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IP 代理工具类
 * @Author wgsh
 * @Date wgshb on 2019/4/9 15:12
 */
public class IPProxyUtils {

	/**
	 * 获取 IP  的列表
	 */
	public static IP getIP() {
		IP ip = new IP();
		JSONObject result = (JSONObject) JSON.parse(getJSON());

		JSONArray array = (JSONArray) result.get("data");
	        for(int i = 0; i < array.size() ; i ++) {
		        JSONObject obj = (JSONObject) JSON.parse(array.get(i).toString());
				ip.setIp(obj.getString("ip"));
				ip.setPort(obj.getString("port"));
		        try {
			        Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(obj.getString("expire_time"));
			        ip.setExpireTime(date.getTime());
			        // ip.setExpireTime(1554801900000l);
		        } catch (ParseException e) {
		        }
	        }
	        return ip;
	}

	/**
	 * 获取请求的 json
	 */
	public static String getJSON() {
		StringBuilder json = new StringBuilder();
		IP ip = new IP();
		try {
			URL oracle = new URL("http://d.jghttp.golangapi.com/getip?num=1&type=2&pro=&city=0&yys=0&port=1&time=2&ts=1&ys=0&cs=0&lb=1&sb=0&pb=4&mr=1&regions=");
			URLConnection yc = oracle.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					yc.getInputStream(),"utf-8"));//防止乱码
			String inputLine = null;
			while ((inputLine = in.readLine()) != null) {
				json.append(inputLine);
			}
			in.close();
		} catch (MalformedURLException e) {
		} catch (IOException e) { }
		JSONObject result = (JSONObject) JSON.parse(json.toString());
		// 出现 ip 需要审核, 此时我们需要将新的 ip 添加进白名单, 免验证
		if("113".equals(result.getString("code"))) {
			StringBuilder tempSB = new StringBuilder();
			System.out.println("============>" + result);
			String whiteIp = result.getString("msg");
			Matcher matcher = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+").matcher(whiteIp);
			if(matcher.find()) {
				whiteIp = matcher.group();
			}
			try {
				URL whitelist = new URL("http://webapi.jghttp.golangapi.com/index/index/save_white?neek=8056&appkey=1733f34c2ae7367406d6c99a36ae621f&white=" + whiteIp);
				// webapi.jghttp.golangapi.com/index/index/save_white?neek=8056&appkey=1733f34c2ae7367406d6c99a36ae621f&white=您的ip
				URLConnection yc = whitelist.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						yc.getInputStream(),"utf-8"));//防止乱码
				String inputLine = null;
				while ((inputLine = in.readLine()) != null) {
					tempSB.append(inputLine);
				}
				System.out.println(tempSB.toString());
				in.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			getJSON();
		}


		return json.toString();
	}

	public static void main(String[] args) {

		System.out.println(getIP());
	}

}
