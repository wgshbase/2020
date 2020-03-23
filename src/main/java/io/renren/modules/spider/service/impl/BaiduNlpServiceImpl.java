package io.renren.modules.spider.service.impl;

import com.alibaba.fastjson.JSON;
import io.renren.modules.spider.service.INlpService;
import io.renren.modules.spider.utils.BaiduServceUtils;
import io.renren.modules.spider.utils.common.OrganRec;
import io.renren.modules.spider.utils.common.PlaceRec;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 参考百度api
 * http://ai.baidu.com/docs#/NLP-API/f6dc4440
 */
public class BaiduNlpServiceImpl implements INlpService {

    @Override
    public HashMap<String, Object> lexer(String text) {
        JSONObject res = BaiduServceUtils.baiduConnect().lexer(text, null);//词法分析接口
        HashMap jsonMap = JSON.parseObject(res.toString(), HashMap.class);
        return jsonMap;
    }

    @Override
    public HashMap<String, Object> autoSummary(String text) {
    	 JSONObject res = BaiduServceUtils.baiduConnect().newsSummary(text, 300, new HashMap<String, Object>());//自动摘要接口
         HashMap jsonMap = JSON.parseObject(res.toString(), HashMap.class);
         return jsonMap;
    }
 
    @Override
    public HashMap<String, Object> sentimentClassify(String text) {
        JSONObject sentimentClassify = BaiduServceUtils.baiduConnect().sentimentClassify(text, null);
        //情感倾向分析接口
        HashMap jsonMap = JSON.parseObject(sentimentClassify.toString(), HashMap.class);
        return jsonMap;
    }
    @Override
    public HashMap<String, Object> keyword(String title, String content) {
        JSONObject keyword = BaiduServceUtils.baiduConnect().keyword(title, content, null);//文章标签接口
        HashMap jsonMap = JSON.parseObject(keyword.toString(), HashMap.class);
        return jsonMap;
    }

    public HashMap<String, Object> topic(String title, String content) {
        JSONObject topic = BaiduServceUtils.baiduConnect().topic(title, content, null);//文章分类接口
        HashMap jsonMap = JSON.parseObject(topic.toString(), HashMap.class);
        return jsonMap;
    }
    
	@Override
	public boolean personReload(List<String> list) {
		return false;
	}

	@Override
	public boolean placeReload(List<String> list) {
		return false;
	}

	@Override
	public boolean organReload(List<String> list) {
		return false;
	}
	@Override
	public boolean kewordReload(List<String> list) {
			return false;
	}
    @Override
    public Map<String, Set<String>>  recognizeAndMaxword(String  text) {
    	
    	Map<String, Set<String>> result   = recognize(text);
    	List<String> listPlace = new PlaceRec(20).keyword(text);
    	List<String> listOrgan= new OrganRec(20).keyword(text);
    	result.put("place",new HashSet<String>(listPlace));
    	result.put("organ",new HashSet<String>(listOrgan));
    	return result;
    }
	@Override
	public Map<String, Set<String>> recognize(String text) {
		Map<String, Set<String>> result = new HashMap<String, Set<String>>();
		JSONObject topic = BaiduServceUtils.baiduConnect().lexer(text, new HashMap<String, Object>());//文章分类接口
        HashMap jsonMap = JSON.parseObject(topic.toString(), HashMap.class);
        if(null!=jsonMap&&jsonMap.containsKey("items")) {
            List<Map<String,Object>> list = (List<Map<String, Object>>) jsonMap.get("items");
            	for(Map<String,Object> o: list) {
            		String word = (String) o.get("item");
            		String nature = (String) o.get("ne");
            		if(nature.equals("ORG")) {
            			addword(result, word, "organ");
            		}else if(nature.equals("LOC")){
            			addword(result, word, "place");
            		}else if(nature.equals("PER")) {
            			addword(result, word, "person");
            		}else if(nature.equals("TIME")) {
            			addword(result, word, "time");
            		}
            	}
        }
        return result;
	}

	@Override
	public HashMap<String, Object> keyword(String content) {
		JSONObject topic = BaiduServceUtils.baiduConnect().keyword("", content, new HashMap<String, Object>());//文章分类接口
        HashMap jsonMap = JSON.parseObject(topic.toString(), HashMap.class);
        return jsonMap;
	}

	@Override
	public Map<String, Object> classify(String title, String content) {
		JSONObject topic = BaiduServceUtils.baiduConnect().topic(title, content, null);//文章分类接口
        HashMap jsonMap = JSON.parseObject(topic.toString(), HashMap.class);
        return jsonMap;
	}
    /**
     * 
     * @param result
     * @param word
     * @param type
     */
    private void addword(Map<String,Set<String>> result,String word,String type){
    	if(result.containsKey(type)){
			result.get(type).add(word);
		}else{
			Set<String> ss= new HashSet<String>();
			ss.add(word);
			result.put(type,ss);
		}
    }
    public static void main(String[] args) {
        String text = "研究人员周三表示，尽管煤炭消费量下降以及许多国家宣布出现气候紧急情况，但天然气使用量猛增所推动的全球碳排放量将在2019年创下历史新高。勒奎尔说，近几十年来大气中的二氧化碳水平呈指数增长，预计今年将平均达到410ppm。联合国上周表示，到2030年，全球排放量每年必须每年下降7.6％，才能将温度升高限制在1.5C。自工业时代以来，全球气温仅升高了1C，2019年出现了一系列致命的超级风暴，干旱，野火和洪水，气候变化加剧了这种情况。她强调指出，随着电力行业继续从煤炭行业转移，美国和欧洲的排放预计将下降1.7％。报告说，污染最严重的化石燃料今年在这两个地区的使用量下降了多达10％。";
        
        Map<String, Set<String>> stringObjectHashMap  = new BaiduNlpServiceImpl().recognize(text);
//        HashMap<String, Object> stringObjectHashMap = new BaiduNlpServiceImpl().sentimentClassify(text);
//        for(Map.Entry entry : stringObjectHashMap.entrySet()) {
//            System.out.println(entry.getKey());
//        }
        JSONObject topic = BaiduServceUtils.baiduConnect().lexer(text, new HashMap<String, Object>());//文章分类接口
        HashMap jsonMap = JSON.parseObject(topic.toString(), HashMap.class);
        if(null!=jsonMap&&jsonMap.containsKey("items")) {
        	List<Map<String,Object>> list = (List<Map<String, Object>>) jsonMap.get("items");
        	for(Map<String,Object> o: list) {
        		String word = (String) o.get("item");
        		String nature = (String) o.get("ne");
        		System.out.println(word+"//"+nature);
        	}
        }
 
        /**
         * items=[{"positive_prob":0.98057,"sentiment":2,"confidence":0.956822,"negative_prob":0.01943}]
         * 0 负向
         * 1 中性
         * 2 正向
         */

    }




}
