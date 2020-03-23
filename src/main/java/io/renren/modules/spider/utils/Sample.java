package io.renren.modules.spider.utils;

import com.baidu.aip.nlp.AipNlp;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class Sample {
    //设置APPID/AK/SK
    public static final String APP_ID = "16028791";
    public static final String API_KEY = "n78x5IQsYAOFhwS1NgORhMkY";
    public static final String SECRET_KEY = "kN0gsW8GMI8YtxvtHpw0bXXb7fLG93aK";
    
    public static AipNlp CLIENT;

    static {
        // 初始化一个AipNlp
        CLIENT = new AipNlp(APP_ID, API_KEY, SECRET_KEY);
        // 可选：设置网络连接参数
        CLIENT.setConnectionTimeoutInMillis(2000);
        CLIENT.setSocketTimeoutInMillis(60000);
    }
    
    /**
     * 抽取文章的关键词
     * @param title 新闻的标题
     * @param content 新闻的正文
     * @param options 可选参数
     */
    public static List<String> keywords(String title, String content, HashMap<String, Object> options) {
    	JSONObject keyword = CLIENT.keyword(title, content, options);
    	List<String> keywords = new ArrayList<>();
    	JSONArray items = (JSONArray) keyword.get("items");
    	for(int i = 0; i < items.length(); i++) {
    		JSONObject obj = (JSONObject) items.get(i);
    		String tag = obj.getString("tag");
    		keywords.add(tag);
    	}
    	return keywords;
    }
    
    /**
     * 抽取新闻的摘要
     * @param content
     * @return
     */
    /*public static String newsSummary(String content) {
    	HashMap<String, Object> options = new HashMap<>();
    	int maxSummaryLen = 100;
    	JSONObject res = CLIENT.newsSummary(content, maxSummaryLen, options);
    	return res.toString(2);
    }*/
    
    /**
     * 实体抽取...
     */
    public static Map<String, Set<String>> extractEntities(String content) {
    	JSONObject res = CLIENT.lexer(content, null);
    	Map<String,Set<String>> map = new HashMap<>();
    	if(res.has("items")) {
    		JSONArray arraies = (JSONArray) res.get("items");
    		Set<String> nrList = new HashSet<>();
    		Set<String> nsList = new HashSet<>();
    		Set<String> ntList = new HashSet<>();
    		for(int i = 0; i < arraies.length(); i++) {
    			JSONObject obj = (JSONObject) arraies.get(i);
    			String item = (String) obj.get("item");
    			if("nr".equals(obj.get("pos")) || "PER".equals(obj.get("ne"))) {
    				if(!nrList.contains(item)) {
    					nrList.add(item);
    				}
    			} else if("ns".equals(obj.get("pos")) || "LOC".equals(obj.get("ne"))) {
    				if(!nsList.contains(item)) {
    					nsList.add(item);
    				}
    			} else if("nt".equals(obj.get("pos")) || "ORG".equals(obj.get("ne"))) {
    				if(!ntList.contains(item)) {
    					ntList.add(item);
    				}
    			}
    		}
    		map.put("nr", nrList);
    		map.put("ns", nsList);
    		map.put("nt", ntList);
    		return map;
    	} else {
    		return map;
    	}
    }
    
    /**
     * 抽取新闻的摘要
     * @param content 正文
     * @param sentenceNum 抽取的数量
     * @return
     */
    public static String extractNewsSummary(String content, int sentenceNum) {
    	return SimpleSummariserAlgorithm.summarise(content, sentenceNum);
    }

    public static void main(String[] args) {
        // 初始化一个AipNlp
        AipNlp client = new AipNlp(APP_ID, API_KEY, SECRET_KEY);

        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);

        // 可选：设置代理服务器地址, http和socket二选一，或者均不设置
//        client.setHttpProxy("proxy_host", proxy_port);  // 设置http代理
//        client.setSocketProxy("proxy_host", proxy_port);  // 设置socket代理

        // 调用接口
        String text = "参议院军事委员会战略部队小组委员会参议员Martin Heinrich（DN.M）在2019年3月14日的国防部预算委员会听证会上发言。 立法会议员在给代理国防部长帕特里克·沙纳汉的一封信中指出，新墨西哥州是数十个以空间为重点的政府和私营部门组织的所在地。 华盛顿 - 来自新墨西哥州的国会代表团成员要求代理国防部长帕特里克·沙纳汉在该州找到新的空间发展机构的成员，以便它可以与那里的大型空间研究组织合作。 “当你站在SDA时，我们敦促你和其他高级领导人将新墨西哥州视为共同定位机构总部的主机，并成为实施你所指导的空间研发政策的主要实体，”立法者在3月18日写给Shanahan的信中写道。 这封信由参议院军事委员会战略部队小组委员会的参议员马丁·海因里希以及参议员汤姆·乌达尔，众议员多奇拉·托兰斯和众议员本·雷·卢汉参议员签署。 该信的副本还被送交国防部副部长，研究和工程部迈克尔格里芬，空军部长希瑟威尔逊，太空发展局局长弗雷德肯尼迪和空军太空司令部司令约翰雷蒙德。 在上周参议院武装部队关于2020财政年度国防预算的听证会上，海因里希建议他支持空间发展局，但要求沙纳汉解释 SDA如何不重复其他人已经在做的事情。 “我非常同意我们的竞争对手正迅速在太空领域发挥新的能力，我们需要更加紧迫的行动，”海因里希说。 “我欢迎该部门在该领域提出的优先顺序。 ...我想确保我们不做的一件事就是重新组织现有的部件或重新发明轮子。“ 信中指出，新墨西哥州拥有两个国家核安全管理局实验室，空间和导弹系统中心的先进系统和发展理事会，空军研究实验室的太空飞行器管理局，太空测试计划，白沙导弹靶场，太空港美国，Starfire光学范围和太空快速能力办公室。 “特别是太空RCO为该部门提供了一个巨大的机会，利用该办事处独特的采办当局，迅速开发和过渡太空系统，转向更加分散的空间架构，”这封信说。 巧合的是，代表新墨西哥州代表新墨西哥州的空军部长希瑟·威尔逊在9月份给Shanahan的一份备忘录中建议将空间发展局与Kirtland空军基地的太空RCO放在一起。 Shanahan和Griffin拒绝了她的建议，他认为SDA应该在五角大楼，而不应该像传统的军事采购组织。 在听证会期间，海因里希提到太空RCO，空军研究实验室的太空飞行器管理局和先进系统理事会作为组织，在研究发展和部署我们国家的太空系统方面起着“关键作用。”他告诉Shanahan国防部应“充分利用现有的研发资产，因为你支持SDA，所以我们不会失去几年的重组，以使整个系统更好地运作。“ Shanahan推翻了空间发展局只是一个重组的想法。 “我们需要利用的空间发展机构的首要任务是大规模系统工程，”他说。 “空军拥有令人难以置信的技术。 我们不缺乏人才，这不是我们的问题。 我们并不缺钱。“但问题是，今天许多重叠项目正在国防部进行，他解释说，这会推高成本并减缓创新步伐。 空间发展机构将寻求巩固重复努力。";
        System.out.println(text.replaceAll("信用(.*?)(?<=>)", "").replaceAll("<.*?>", "").replaceAll("\\s+", " ").trim());
        System.out.println("实体抽取"+client.lexer(text, null));
        System.out.println("dsdsdsd " + extractEntities(text));
        //("立法者敦促国防部在新墨西哥州设立空间发展局", text.replaceAll("<.*?>", ""), null));
        String title = "立法者敦促国防部在新墨西哥州设立空间发展局";
        String content = "参议院军事委员会战略部队小组委员会参议员Martin Heinrich（DN.M）在2019年3月14日的国防部预算委员会听证会上发言。 立法会议员在给代理国防部长帕特里克·沙纳汉的一封信中指出，新墨西哥州是数十个以空间为重点的政府和私营部门组织的所在地。 华盛顿 - 来自新墨西哥州的国会代表团成员要求代理国防部长帕特里克·沙纳汉在该州找到新的空间发展机构的成员，以便它可以与那里的大型空间研究组织合作。 “当你站在SDA时，我们敦促你和其他高级领导人将新墨西哥州视为共同定位机构总部的主机，并成为实施你所指导的空间研发政策的主要实体，”立法者在3月18日写给Shanahan的信中写道。 这封信由参议院军事委员会战略部队小组委员会的参议员马丁·海因里希以及参议员汤姆·乌达尔，众议员多奇拉·托兰斯和众议员本·雷·卢汉参议员签署。 该信的副本还被送交国防部副部长，研究和工程部迈克尔格里芬，空军部长希瑟威尔逊，太空发展局局长弗雷德肯尼迪和空军太空司令部司令约翰雷蒙德。 在上周参议院武装部队关于2020财政年度国防预算的听证会上，海因里希建议他支持空间发展局，但要求沙纳汉解释 SDA如何不重复其他人已经在做的事情。 “我非常同意我们的竞争对手正迅速在太空领域发挥新的能力，我们需要更加紧迫的行动，”海因里希说。 “我欢迎该部门在该领域提出的优先顺序。 ...我想确保我们不做的一件事就是重新组织现有的部件或重新发明轮子。“ 信中指出，新墨西哥州拥有两个国家核安全管理局实验室，空间和导弹系统中心的先进系统和发展理事会，空军研究实验室的太空飞行器管理局，太空测试计划，白沙导弹靶场，太空港美国，Starfire光学范围和太空快速能力办公室。 “特别是太空RCO为该部门提供了一个巨大的机会，利用该办事处独特的采办当局，迅速开发和过渡太空系统，转向更加分散的空间架构，”这封信说。 巧合的是，代表新墨西哥州代表新墨西哥州的空军部长希瑟·威尔逊在9月份给Shanahan的一份备忘录中建议将空间发展局与Kirtland空军基地的太空RCO放在一起。 Shanahan和Griffin拒绝了她的建议，他认为SDA应该在五角大楼，而不应该像传统的军事采购组织。 在听证会期间，海因里希提到太空RCO，空军研究实验室的太空飞行器管理局和先进系统理事会作为组织，在研究发展和部署我们国家的太空系统方面起着“关键作用。”他告诉Shanahan国防部应“充分利用现有的研发资产，因为你支持SDA，所以我们不会失去几年的重组，以使整个系统更好地运作。“ Shanahan推翻了空间发展局只是一个重组的想法。 “我们需要利用的空间发展机构的首要任务是大规模系统工程，”他说。 “空军拥有令人难以置信的技术。 我们不缺乏人才，这不是我们的问题。 我们并不缺钱。“但问题是，今天许多重叠项目正在国防部进行，他解释说，这会推高成本并减缓创新步伐。 空间发展机构将寻求巩固重复努力。";
        HashMap<String, Object> options = new HashMap<>();
        System.out.println(keywords(title, content, options));
        System.out.println(client.keyword(title, content, options));
//        System.out.println(newsSummary(content));
        System.out.println(extractNewsSummary(content, 2));
        
    }
}
