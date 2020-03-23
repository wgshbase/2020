package io.renren.modules.spider.utils;

import com.baidu.aip.nlp.AipNlp;
import com.baidu.aip.ocr.AipOcr;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class BaiduServceUtils {
	 //设置APPID/AK/SK
//    public static final String APP_ID = "10998482";
//    public static final String API_KEY = "48Gh3mIy7zxbTd9E62UAFSuQ";
//    public static final String SECRET_KEY = "hFqriWezUQWKTdoTRFhxMnOUqF3BujcG";
    public static final String APP_ID = "18033519";
    public static final String API_KEY = "79Su7LmHPQ9GFcqhiswNw8mi";
    public static final String SECRET_KEY = "KSnHfoRkurN5QbW2kfzNe2XsYLkXrH4v";
    private static String client_id = "s7PYZhdA0Ra9GMUwNvqPrQFp";
    private static String client_secret = "pr2NVb5cv7XFQWxcmRrG7xIyj8PxGMgK";
    public static AipNlp baiduConnect(){
        // 初始化一个AipNlp
        AipNlp client = new AipNlp(APP_ID, API_KEY, SECRET_KEY);

        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);

        // 可选：设置代理服务器地址, http和socket二选一，或者均不设置
        // client.setHttpProxy("proxy_host", "https://aip.baidubce.com/rpc/2.0/nlp/v1/lexer?access_token=24.86643a9269407a6e413dc695e54660cc.2592000.1524648141.282335-10998482");  // 设置http代理
        // client.setSocketProxy("proxy_host", proxy_port);  // 设置socket代理

        // 可选：设置log4j日志输出格式，若不设置，则使用默认配置
        // 也可以直接通过jvm启动参数设置此环境变量
        System.setProperty("aip.log4j.conf", "path/to/your/log4j.properties");
        return client;
    }
    
    public static AipOcr aipOcrConnect(){
    	// 初始化一个AipNlp
    	AipOcr  client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);
    	
    	// 可选：设置网络连接参数
    	client.setConnectionTimeoutInMillis(2000);
    	client.setSocketTimeoutInMillis(60000);
    	
    	// 可选：设置代理服务器地址, http和socket二选一，或者均不设置
    	// client.setHttpProxy("proxy_host", "https://aip.baidubce.com/rpc/2.0/nlp/v1/lexer?access_token=24.86643a9269407a6e413dc695e54660cc.2592000.1524648141.282335-10998482");  // 设置http代理
    	// client.setSocketProxy("proxy_host", proxy_port);  // 设置socket代理
    	
    	// 可选：设置log4j日志输出格式，若不设置，则使用默认配置
    	// 也可以直接通过jvm启动参数设置此环境变量
    	System.setProperty("aip.log4j.conf", "path/to/your/log4j.properties");
    	return client;
    }
    public static void main(String[] args) throws JSONException {
    	JSONObject json = BaiduServceUtils.aipOcrConnect()
    			.basicGeneral("F:\\项目管理\\23所\\20191211\\ceshi\\第6页.jpg", new HashMap<String, String>());
    	System.out.println(json);
    	String text2 = "大吉大利晚上吃鸡"; JSONObject depparser =
    	BaiduServceUtils.baiduConnect().depParser(text2, null);//依存句法分析接口
    	System.out.println("依存句法分析接口-------------"+depparser.toString(2));
    	
		/*
		 * // 调用接口 String text = "锋哥哥是好人"; JSONObject res =
		 * BaiduServceUtils.baiduConnect().lexer(text, null);//词法分析接口
		 * System.out.println("词法分析接口-----------"+res.toString(2)); HashMap jsonMap =
		 * JSON.parseObject(res.toString(), HashMap.class); for (Object map :
		 * jsonMap.entrySet()){ System.out.println(((Map.Entry)map).getKey()+"     " +
		 * ((Map.Entry)map).getValue()); }
		 * 
		 * String text2 = "大吉大利晚上吃鸡"; JSONObject depparser =
		 * BaiduServceUtils.baiduConnect().depParser(text2, null);//依存句法分析接口
		 * System.out.println("依存句法分析接口-------------"+depparser.toString(2));
		 * 
		 * String text3 = "安会锋"; JSONObject word_emb_vec =
		 * BaiduServceUtils.baiduConnect().wordEmbedding(text3, null);//词向量表示接口
		 * System.out.println("词向量表示接口----------"+word_emb_vec.toString());
		 * 
		 * String text4 = "床前明月光"; JSONObject dnnlmCn =
		 * BaiduServceUtils.baiduConnect().dnnlmCn(text4, null);//DNN语言模型接口
		 * System.out.println("DNN语言模型接口---------------"+dnnlmCn.toString(2));
		 * 
		 * String text5 = "北京"; String text6 = "天津"; JSONObject wordSimEmbedding =
		 * BaiduServceUtils.baiduConnect().wordSimEmbedding(text5, text6,
		 * null);//词义相似度接口
		 * System.out.println("词义相似度接口------------"+wordSimEmbedding.toString(2));
		 * 
		 * String text7 = "学挖掘机到南翔"; String text8 = "学厨师就到新东方"; JSONObject simnet =
		 * BaiduServceUtils.baiduConnect().simnet(text7,text8, null);//短文本相似度接口
		 * System.out.println("短文本相似度接口-------------"+simnet.toString(2));
		 * 
		 * 
		 * String text9 = "三星电脑电池不给力"; JSONObject commentTag =
		 * BaiduServceUtils.baiduConnect().commentTag(text9, ESimnetType._3C,
		 * null);//评论观点抽取接口
		 * System.out.println("评论观点抽取接口-------------"+commentTag.toString(2));
		 * 
		 * String text10 = "学挖掘机就到到南翔"; JSONObject sentimentClassify =
		 * BaiduServceUtils.baiduConnect().sentimentClassify(text10, null);//情感倾向分析接口
		 * System.out.println("情感倾向分析接口-------------"+sentimentClassify.toString(2));
		 * 
		 * 
		 * String text11 = "学厨师就到新东方"; String content = "新东方，三百个床位不锈钢"; JSONObject
		 * keyword = BaiduServceUtils.baiduConnect().keyword(text11, content,
		 * null);//文章标签接口 System.out.println("文章标签接口-------------"+simnet.toString(2));
		 * 
		 * 
		 * String text12 = "欧洲冠军联赛"; String content1 =
		 * "欧洲冠军联赛是欧洲足球协会联盟主办的年度足球比赛，代表欧洲俱乐部足球最高荣誉和水平，被认为是全世界最高素质、最具影响力以及最高水平的俱乐部赛事，亦是世界上奖金最高的足球赛事和体育赛事之一";
		 * JSONObject topic = BaiduServceUtils.baiduConnect().topic(text12,content1,
		 * null);//文章分类接口 System.out.println("文章分类接口-------------"+simnet.toString(2));
		 */
    }
}
