package com.mss.translate;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransApi {
	private static final String TRANS_API_HOST = "http://api.fanyi.baidu.com/api/trans/vip/translate";
	Logger logger = LoggerFactory.getLogger(TransApi.class);
	private String appid;
	private String securityKey;

	public TransApi(String appid, String securityKey) {
		this.appid = appid;
		this.securityKey = securityKey;
	}

	/**
	 * 这边的主要的问题就是 百度翻译仅仅支持翻译 6000 字符(中文约 2000 个字符) , 所以对于大批量的数据需要进行拆分;
	 * 对于英文文档比较好的一点就是每个段落的结束位置都有换行符 \n, 这个为拆分提供了极大的便利...
	 * @param query
	 * @param from
	 * @param to
	 * @return
	 */
	public String getTransResult(String query, String from, String to) {
		int limit = 2000;

		// 声明一个翻译中的变量
		boolean translating = false;

		/*if(!from.equals("zh") && query.length() > limit && !translating) {
			StringBuffer finalResult = new StringBuffer();
			List<String> queryArray = parseLongString2StringArray(query,limit);
			for(String q : queryArray) {
				translating = true;
				finalResult.append(getTransResult(q,from,to));
			}
			return finalResult.toString();

		}*/
		Map<String, String> params = buildParams(query, from, to);
		// 返回的是总的 json 字符串
		String result = HttpGet.get(TRANS_API_HOST, params);
		// 解析 json 字符串中的目标字符
		JSONObject json = JSONObject.parseObject(result);
		JSONArray trans_result = json.getJSONArray("trans_result");
		for (int i = 0; i < trans_result.size(); i++) {
			JSONObject obj = trans_result.getJSONObject(i);
			if (obj.containsKey("dst")) {
				return obj.getString("dst");
			}
		}
		logger.error("===============> Something bad happens, there is no translate result...");
		return "";
	}

	/**
	 * 按照指定的字数限制将长字符串拆分为指定的字数限制的字符串数组
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

	/**
	 * 翻译的方法
	 * @param query     需要的请求参数
	 * @param from      翻译的原文的语言(允许为 auto, 即自动检测)
	 * @param to        翻译的目标语言(不允许为 auto), 常用的由 en 英文, zh 中文
	 * @return
	 */
	private Map<String, String> buildParams(String query, String from, String to) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("q", query);
		params.put("from", from);
		params.put("to", to);

		params.put("appid", appid);

		// 随机数
		String salt = String.valueOf(System.currentTimeMillis());
		params.put("salt", salt);

		// 签名
		String src = appid + query + salt + securityKey; // 加密前的原文
		params.put("sign", MD5.md5(src));

		return params;
	}

	public static void main(String[] args) {
		TransApi api = new TransApi("20190306000274362", "afuWSpDkOiJZ5CvmJQ7u");
		//String query = "You're somebody to me";

		//System.out.println(api.getTransResult(query, "auto", "zh"));
		String query =
				"     United Launch Alliance is developing a new vehicle to replace the Atlas 5 in future national security launch competitions. \n" +
				"   </div> \n" ;
		System.out.println(query.length());

		/*List<String> strings = parseLongString2StringArray(query, 1000);

		for(String str : strings) {
			System.out.println(str.length());

		}*/
		String transResult = api.getTransResult(query, "auto", "zh");
		System.out.println(transResult);
	}

}
