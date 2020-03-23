package io.renren.modules.spider.service.impl;

import com.bestdata.nlp.Datanlp;
import com.bestdata.nlp.classify.main.Classify4Web;
import com.bestdata.nlp.model.crf.CRFLexicalAnalyzer;
import com.bestdata.nlp.seg.common.Term;
import com.bestdata.nlp.summary.TextRankKeyword;
import com.bestdata.nlp.util.TermUtil;
import com.bestdata.util.NlpLog;
import com.google.common.collect.Maps;
import io.renren.modules.spider.dao.NewsDao;
import io.renren.modules.spider.service.INlpService;
import io.renren.modules.spider.utils.common.KewordRec;
import io.renren.modules.spider.utils.common.OrganRec;
import io.renren.modules.spider.utils.common.PlaceRec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class NlpServiceImpl implements INlpService {
 

	@Value("${data.upload.file}")
	private String uploadDir;
	
	@Value("${data.corpus.train}")
	private String trainDir;
	
	@Value("${data.corpus.newsModel}")
	private String newsModel;
	
	@Value("${data.corpus.sentimentModel}")
	private String sentimentModel;

	@Override
	public boolean personReload(List<String> list) {
		try {
			TermUtil.init().write(list,"\\data\\term_person.txt","nr");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	@Override
	public boolean placeReload(List<String> list) {
		try {
			TermUtil.init().write(list,"\\data\\term_place.txt","ns");
			new PlaceRec(20).reloadDict(list);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	@Override
	public boolean organReload(List<String> list) {
		try {
			TermUtil.init().write(list,"\\data\\term_organ.txt","nt");
			new OrganRec(20).reloadDict(list);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	@Override
	public boolean kewordReload(List<String> list) {
		try {
			TermUtil.init().write(list,"\\data\\term.txt","");
			new KewordRec(15).reloadDict(list);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/** 
	 * 词法分析
	 * 
	 */
    @Override
    public HashMap<String, Object> lexer(String text) {
        HashMap<String, Object> map = Maps.newHashMap();
        try {
            CRFLexicalAnalyzer analyzer = new CRFLexicalAnalyzer();
            List<Term> segList = analyzer.seg(text);
            map.put("items", segList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 自动摘要
     * 
     */
    @Override
    public Map<String, Object> autoSummary(String text) {
        // List<String> strings = HanLP.extractSummary(text, 5);
        // StringBuilder builder = new StringBuilder();
        // for (String string : strings) {
        //     builder.append(string).append('，');
        // }
        // builder.append('。');
        // return builder.toString();
//        return HanLP.getSummary(text, 50);
//    	keyword("", text);
    	Map<String, Object>  result =  new HashMap<String, Object>();
    	List<String> strings   =  Datanlp.extractSummary(text, 1);
    	if(null != strings && strings.size() > 0) {
    		result.put("summary", strings.get(0));
		} else {
    		result.put("summary", "");
		}
    	return result;
    }

 
    @Override
    public Map<String, Object> sentimentClassify(String text) {
    	String modelPath =  sentimentModel;
		Classify4Web classify4Web = new Classify4Web(modelPath);
		classify4Web.loadModel();
		Map<String, Object>  result =  new HashMap<String, Object>();
		try {
			result = classify4Web.proResult(text);
			result.put("status","OK");
		} catch (IOException e) {
			result.put("status","ERROR");
			result.put("msg","内容检测异常");
			e.printStackTrace();
		}
		return result;
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
    public Map<String, Set<String>>  recognize(String  text) {
    	Map<String, Set<String>> result = new HashMap<String, Set<String>>();
    	try {
//			segment.enableAllNamedEntityRecognize(true);
//			List<Term> tms =Datanlp.newSegment().seg(text);
//    		List<Term> tms  =  Datanlp.newSegment().enableNameRecognize(true).seg(text);
			CRFLexicalAnalyzer analyzer = new CRFLexicalAnalyzer();
            List<Term> tms = analyzer.seg(text);
//    		List<Term> tms = NLPTokenizer.segment(text);
//    		List<Term> tms = StandardTokenizer.segment(text);
			String time = "";
			String wd = "";
			for (Term tm : tms) {
				System.out.print(""+tm.word+"/"+tm.nature);
				if (tm.nature.startsWith("t")) {
					time += tm.word;
				} else if (!time.equals("")&&time.matches("[0-9一二三四五六七八九]+")) {
					addword(result, time, "time");
					time = "";
				}
				wd = tm.word.trim();
				if (wd.length() > 1) {
					if (tm.nature.startsWith("nt")&&tm.nature.equals("ntc")) {
						addword(result, wd, "organ");
					} else if (tm.nature.startsWith("nr")) {
						addword(result, wd, "person");
					} else if (tm.nature.startsWith("ns")) {
						addword(result, wd, "place");
					}
				}
			}
			if (!time.equals("")&&time.matches("[0-9一二三四五六七八九]+")) {
				addword(result, time, "time");
			}
    	} catch (Exception e) {
    	 	NlpLog.info(e.getMessage());
    	}
		return result;
    }
    
	@Override
	public HashMap<String, Object>  keyword(String content) {
		 List<String>  list = new KewordRec(15).keyword(content);
		 HashMap<String, Object> result = new LinkedHashMap<String, Object>();
		 
		 for(String s: list) {
			 result.put(s,0);
		 }
		 return result;
	}

    @Override
    public HashMap<String, Object> keyword(String title, String content) {
//    	String content = "程序员(英文Programmer)是从事程序开发、维护的专业人员。一般将程序员分为程序设计人员和程序编码人员，但两者的界限并不非常清楚，特别是在中国。软件从业人员分为初级程序员、高级程序员、系统分析员和项目经理四大类。";
    	TextRankKeyword textRankKeyword = new TextRankKeyword();
    	Map<String, Float> mm = textRankKeyword.getTermAndRank(content);
    	mm = mm.entrySet().stream().sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

    	HashMap<String, Object> result = new LinkedHashMap<String, Object>();
    	for(Entry<String, Float> en:mm.entrySet()){
    		if(en.getKey().length()>1){
    			result.put(en.getKey(),en.getValue());
    			if(result.size()>5){
    				break;
    			}
    		}
    	}
    	System.out.println("mm:"+mm);
    	System.out.println("result:"+result);
        return result;
    }
	/** 自动分类
	 *
	 */
    @Override
    public Map<String, Object> classify(String title, String content) {
    	String modelPath =  newsModel;
		Classify4Web classify4Web = new Classify4Web(modelPath);
		classify4Web.loadModel();
		Map<String, Object>  result =  new HashMap<String, Object>();
		try {
			result = classify4Web.proResult(title+"。"+content);
			result.put("status","OK");
		} catch (IOException e) {
			result.put("status","ERROR");
			result.put("msg","内容检测异常");
			e.printStackTrace();
		}
		return result;
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
//    	String title = "中国科普作家协会海洋科普专业委员会2019年年会";
		String text = "该研究结果新华社反映了北太平洋生态系统的流动性和相互联系，并且不承认州和国界或其相关的管理实践。";
		Map<String, Set<String>> stringObjectMap = new NlpServiceImpl().recognizeAndMaxword(text);
		for(Entry entry : stringObjectMap.entrySet()) {
			System.out.println(entry.getKey() + " " + entry.getValue());
		}
		System.out.println(stringObjectMap);
		System.out.println("过去二".matches("[0-9一二三四五六七八九]+"));

	}


}
