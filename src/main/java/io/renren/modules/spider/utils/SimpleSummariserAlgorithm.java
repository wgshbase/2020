package io.renren.modules.spider.utils;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

/**
 *
 * <p>Title:</p>
 * <p>Description: SimpleSummariser
 * </p>
 * @createDate：2013-8-26
 * @author xq
 * @version 1.0
 */
public class SimpleSummariserAlgorithm {

	/**
	 *
	 * @Title: summarise
	 * @Description: 文章摘要实现
	 * @param @param input   新闻的正文
	 * @param @param numSentences  抽取的句子的数量
	 * @param @return
	 * @return String
	 * @throws
	 */
	public static String summarise(String input, int numSentences) {
		// get the frequency of each word in the input
		Map<String,Integer> wordFrequencies = segStr(input);

		// now create a set of the X most frequent words
		Set<String> mostFrequentWords = getMostFrequentWords(100, wordFrequencies).keySet();

		// break the input up into sentences
		// workingSentences is used for the analysis, but
		// actualSentences is used in the results so that the
		// capitalisation will be correct.
		String[] workingSentences = getSentences(input.toLowerCase());
		String[] actualSentences = getSentences(input);

		// iterate over the most frequent words, and add the first sentence
		// that includes each word to the result
		Set<String> outputSentences = new LinkedHashSet<String>();
		Iterator<String> it = mostFrequentWords.iterator();
		while (it.hasNext()) {
			String word = (String) it.next();
			for (int i = 0; i < workingSentences.length; i++) {
				if (workingSentences[i].indexOf(word) >= 0) {
					outputSentences.add(actualSentences[i]);
					break;
				}
				if (outputSentences.size() >= numSentences) {
					break;
				}
			}
			if (outputSentences.size() >= numSentences) {
				break;
			}

		}

		List<String> reorderedOutputSentences = reorderSentences(outputSentences, input);

		StringBuffer result = new StringBuffer("");
		it = reorderedOutputSentences.iterator();
		while (it.hasNext()) {
			String sentence = (String) it.next();
			result.append(sentence);
			result.append("。"); // This isn't always correct - perhaps it should be whatever symbol the sentence finished with
			if (it.hasNext()) {
				result.append("");
			}
		}

		return result.toString();
	}

	/**
	 *
	 * @Title: reorderSentences
	 * @Description: 将句子按顺序输出
	 * @param @param outputSentences
	 * @param @param input
	 * @param @return
	 * @return List<String>
	 * @throws
	 */
	private static List<String> reorderSentences(Set<String> outputSentences, final String input) {
		// reorder the sentences to the order they were in the
		// original text
		ArrayList<String> result = new ArrayList<String>(outputSentences);

		Collections.sort(result, new Comparator<String>() {
			public int compare(String arg0, String arg1) {
				String sentence1 = (String) arg0;
				String sentence2 = (String) arg1;

				int indexOfSentence1 = input.indexOf(sentence1.trim());
				int indexOfSentence2 = input.indexOf(sentence2.trim());
				int result = indexOfSentence1 - indexOfSentence2;

				return result;
			}

		});
		return result;
	}

	/**
	 *
	 * @Title: getMostFrequentWords
	 * @Description: 对分词进行按数量排序,取出前num个
	 * @param @param num
	 * @param @param words
	 * @param @return
	 * @return Map<String,Integer>
	 * @throws
	 */
	public static Map<String, Integer> getMostFrequentWords(int num,Map<String, Integer> words){

		Map<String, Integer> keywords = new LinkedHashMap<String, Integer>();
		int count=0;
		// 词频统计
		List<Map.Entry<String, Integer>> info = new ArrayList<Map.Entry<String, Integer>>(words.entrySet());
		Collections.sort(info, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> obj1, Map.Entry<String, Integer> obj2) {
				return obj2.getValue() - obj1.getValue();
			}
		});

		// 高频词输出
		for (int j = 0; j < info.size(); j++) {
			// 词-->频
			if(info.get(j).getKey().length()>1){
				if(num>count){
					keywords.put(info.get(j).getKey(), info.get(j).getValue());
					count++;
				}else{
					break;
				}
			}
		}
		return keywords;
	}


	/**
	 *
	 * @Title: segStr
	 * @Description: 返回LinkedHashMap的分词
	 * @param @param content
	 * @param @return
	 * @return Map<String,Integer>
	 * @throws
	 */
	public static Map<String, Integer> segStr(String content){
		// 分词
		Reader input = new StringReader(content);
		// 智能分词关闭（对分词的精度影响很大）
		IKSegmenter iks = new IKSegmenter(input, true);
		Lexeme lexeme = null;
		Map<String, Integer> words = new LinkedHashMap<String, Integer>();
		try {
			while ((lexeme = iks.next()) != null) {
				if (words.containsKey(lexeme.getLexemeText())) {
					words.put(lexeme.getLexemeText(), words.get(lexeme.getLexemeText()) + 1);
				} else {
					words.put(lexeme.getLexemeText(), 1);
				}
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
		return words;
	}

	/**
	 *
	 * @Title: getSentences
	 * @Description: 把段落按. ! ?分隔成句组
	 * @param @param input
	 * @param @return
	 * @return String[]
	 * @throws
	 */
	public static String[] getSentences(String input) {
		if (input == null) {
			return new String[0];
		} else {
			// split on a ".", a "!", a "?" followed by a space or EOL
			//"(\\.|!|\\?)+(\\s|\\z)"
			return input.split("(\\.|!|\\?|。)");
		}

	}

	public static void main(String[] args){
		String s="11月23日，中国科普作家协会海洋科普专业委员会2019年年会在位于山东青岛的中国海洋大学举行。本届年会由中国科普作家协会海洋科普专业委员会、山东自然辩证法研究会、山东省科普创作协会联合主办，主题为“海洋文化与海洋科普”。\n" +
				"\n" +
				"中国科普作家协会海洋科普专业委员会2019年年会在中国海洋大学举行 （摄影 赵海磊）\n" +
				"\n" +
				"中国科普作家协会海洋科普专业委员会主任委员、中国海洋大学原校长吴德星在致辞中首先对参会人员表示感谢，他认为三个协会首次联合举办学术年会对推动山东海洋科普和海洋教育将产生积极影响，并预祝会议圆满成功。\n" +
				"\n" +
				"山东省科普创作协会理事长马来平在致辞中表示，此次三个协会联合举办的“海洋文化与海洋科普研讨会”在配合山东海洋强国战略、探讨海洋科普与海洋文化方面意义深刻、影响深远。\n" +
				"\n" +
				"在工作总结环节，海洋科普专业委员会副秘书长、中国海洋大学出版社副总编辑李夕聪代表海洋科普专业委员会做了2019年度工作报告，山东自然辩证法研究会马佰莲做了山东自然辩证法研究会2019年度工作报告，山东省科普创作协会肖鹏代秘书长做了山东省科普创作协会2019年度工作报告。\n" +
				"\n" +
				"2019年以来，海洋科普专业委员会共组织策划精品海洋科普图书20余部，其中《珊瑚礁里的秘密科普丛书》入选2019年度国家出版基金项目，《我们的海洋》《中国海洋符号丛书》入选2019自然资源部优秀科普图书，《中国海洋神话故事读本》等15本图书入选2019年农家书屋重点图书推荐目录。\n" +
				"\n" +
				"海洋科普专业委员会还在推动海洋教育方面做出了积极探索，承办了中国海洋发展基金会2019年1月、8月两次有100位教师参加的“海洋知识教材教师培训班”培训任务，并承办了“海洋欢乐谷”夏令营活动。\n" +
				"\n" +
				"在学术报告环节，山东省政府参事、山东自然辩证法研究会、山东省科普创作协会理事长马来平做了《关于繁荣山东海洋科普的若干建议》讲座，海洋科普专业委员会副主任委员、原国家海洋局第一海洋研究所所长马德毅做了《中国文明演进中海洋文化的历史兴背》讲座，中国海洋大学于广利教授做了《国内外海洋药物研究开发进展》讲座，中国海洋大学马树华教授做了《走向大众：海洋文化的书写、记忆与传承》讲座，国家一级作家霞子做了《关于青少年海洋教育的一点思考》讲座。\n" +
				"\n" +
				"当天下午分组讨论中，与会人员分别就强化海洋意识、深化海洋普教育、传播海洋文化进行了热烈探讨。\n" +
				"\n" +
				"中国科普作家协会海洋科普专业委员会秘书长杨立敏在总结讲话中表示，海洋科普专业委员会将一如既往，团结和凝聚社会各界力量，为普及海洋知识，提升全民海洋意识而继续努力。（中国日报青岛记者站）\n" +
				"\n";
		System.out.println(summarise(s, 5));
	}
}
