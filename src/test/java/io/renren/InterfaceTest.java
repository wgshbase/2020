package io.renren;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

/**
 * JAVA 接口测试样例代码
 * @author wgshb
 *
 */
public class InterfaceTest {
	public static void main(String[] args) throws Exception {
		// 请求地址
		String surl = "http://localhost:8081/extractKeyword";
		// 请求参数集合
		Map<String, String> map = new HashMap<>();
		map.put("content", "泰国在线网站消息，当地时间1月7日16时，由韩国大宇造船与海洋工程为泰国皇家海军建造的DW-3000F型护卫舰首舰“普密蓬·阿杜德”号（HTMS Bhumibol Adulyadej，F-471）缓缓驶入泰国海军梭桃邑基地，并同时举行服役仪式，泰国海军总司令列差·如迪特上将（Luechai Ruddit）主持。在“普密蓬·阿杜德”号进入泰国海域时，泰国皇家海军派出了三艘舰艇迎接。");
		map.put("num", "2");
		// 拼接请求地址
		if(map.size() == 1) {
			for(Map.Entry<String, String> entry : map.entrySet()) {
				surl = surl + "?" + entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "utf-8");
			}
		} else if(map.size() > 1) {
			List<String> tempList = new ArrayList<>();
			for(Map.Entry<String, String> entry : map.entrySet()) {
				tempList.add(entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "utf-8"));
			}
			for(int i = 0; i < tempList.size(); i++) {
				if(i == 0) {
					surl = surl + "?" + tempList.get(i); 
				} else {
					surl = surl + "&" + tempList.get(i);
				}
			}
		}
		// 返回结果
		String result = execute(surl, JSONObject.toJSONString(map));
		System.out.println(result);
	}
	/**
	 * HTTP请求
	 * @param surl 接口请求url
	 * @param json 接口请求body-json字符串
	 *  
	 * @return 接口返回结果
	 */
    public static String execute(String surl, String json) throws Exception
    {
        URL url = new URL(surl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        conn.setRequestMethod("POST");// 提交模式
        conn.setRequestProperty("Content-Length", json.getBytes().length + "");
        conn.setConnectTimeout(100000);// 连接超时单位毫秒 //
        conn.setReadTimeout(200000);// 读取超时 单位毫秒
        conn.setDoOutput(true);// 是否输入参数
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.connect();
        DataOutputStream out = new DataOutputStream(conn.getOutputStream());
        out.write(json.getBytes());
        out.flush();
        out.close();
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null)
        {
            sb.append(line);
        }
        reader.close();
        conn.disconnect();

        return sb.toString();
    }
		
}
