package io.renren;

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
	 * @param @param input
	 * @param @param numSentences
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
		String s="这篇文章最初出现在2019年3月11日的SpaceNews杂志上，于3月18日更新。1月27日，当一架俄罗斯火箭从南美洲起飞，将六颗法国制造的卫星送入低地球轨道时，OneWeb的梦想是以实惠，丰富的宽带覆盖全球。OneWeb面临着11月的最后一个截止日期，要求它放弃其第一批卫星，否则几年前国际电信联盟将为这个雄心勃勃的宽带巨型星号分配频谱而失去频谱。如果所有这些都继续按计划进行，OneWeb的前六个航天器将在今年春天完成在轨测试，为648颗卫星的初始系统 - 600个运行卫星和48个备用卫星 - 扫清道路，并为最终可能编号的更大系统奠定基础900颗以上的卫星。围绕最初的648卫星星座进行轨道运动将成为历史上规模最大的发射活动。 在初秋的夏末，OneWeb预计将每隔三到四周就联盟号火箭发射大约30颗左右的卫星。 除了通过欧洲发射服务提供商Arianespace预订的这20个联盟号任务之外，OneWeb还指望一个或多个阿丽亚娜6号发射加上一个确定数量的Virgin Orbit LauncherOne任务，到2021年完成其星座。OneWeb及其卫星制造合作伙伴空中客车防务与航天公司已将每秒10千兆位的容量塞进洗碗机大小的航天器中。 空中客车公司即将卸任的首席执行官汤姆恩德斯2月14日表示，OneWeb卫星的生产成本为100万美元，这些公司每年可以通过其合资企业OneWeb Satellite在4月份开设的价值8500万美元的佛罗里达工厂完成350到400颗卫星。 空中客车发言人Guilhem Boltz表示，第一批佛罗里达制造的卫星将在第三季度末交付给OneWeb。OneWeb的卫星是一项技术奇迹。 传统的地球静止通信卫星与卡车一样大，需要数年时间才能建造，通常耗资1亿美元或更多，并且仍然可能产生比一些OneWeb卫星更少的吞吐量。但卫星行业已经看到其他技术奇迹进入轨道，而其背后的商业计划则落到了地面。最臭名昭着的失败是Teledesic。 比尔盖茨支持的合资企业在20世纪90年代筹集了10亿美元用于建造840颗卫星“天空中的互联网”，但在推出一颗演示卫星后几年就被烧毁了。虽然Iridium和Globalstar在2000年成功部署了相对适度的星座，这使得手持式卫星电话得以出现，但两家企业都在此过程中破产。 然而，Iridium和Globalstar最终摆脱了破产，继续推出第二代星座。每个OneWeb太空船重约150公斤 - 远远超过立方体，但远低于平均通信卫星，通常重达数吨。图片来源：ARIANESPACE / CNES / ESA 每个OneWeb太空船重约150公斤 - 远远超过立方体，但远低于平均通信卫星，通常重达数吨。 图片来源：ARIANESPACE / CNES / ESA 根据前Moog首席技术专家和参与Teledesic和Iridium的CSA工程主管Eric Anderson的说法，OneWeb目前的风险主要是财务风险，而不是技术风险。 他现在是太空顾问和投资者。“如果他们的收入增长缓慢，我预计他们将会增长，那么他们就无法重新投入资本支出，例如建造新的卫星和地面站基础设施，”安德森说。OneWeb在其首次推出前夕表示，迄今已筹集并花费超过20亿美元，其中包括自2015年以来两次高调股权回合中的17亿美元，以及该公司去年筹集的未披露金额。 2015年首批5亿美元来自行业合作伙伴空客，休斯网络系统，Intelsat，高通和维珍集团，以及可口可乐，墨西哥电信公司Grupo Salinas和印度电信公司Bharti Airtel。 2016年，日本科技集团软银集团（SoftBank）在12亿美元的融资中提供了10亿美元，其中包括之前的投资者。 9月，OneWeb自2015年以来第三次取代其首席执行官，取代空中客车公司资深人士埃里克·贝朗格与OneWeb董事会成员Adrian Steckel取代，后者同时担任Grupo Salinas Telecom和加密货币创业公司Uphold的首席执行官。 Steckel在2月27日Soyuz发布OneWeb的前六颗卫星之前，在法属圭亚那Kourou对记者发表讲话，预测OneWeb将在2022年实现盈利，或者在OneWeb预计该星座实现全球服务后约六个月。 Steckel表示，OneWeb很快将从之前的投资者那里筹集更多资金。 事实证明，3月18日，该公司宣布进行12.5亿美元的融资 - 迄今为止最大的一轮 - 由SoftBank Group Corp.，Grupo Salinas，Qualcomm Technologies和卢旺达政府领导。 新一轮将OneWeb的总资本提高到34亿美元 - 这是一笔可观的数额，但如果没有OneWeb系统的总成本估算，那么这一重要意义就无法完全衡量。 虽然早期的估计从15亿美元到25亿美元不等，但OneWeb不再分享部署该星座的预测。 当Steckel去年秋天被聘用时，Uphold发布了一则新闻稿（很快撤回），称OneWeb是“全球宽带计划的价值为6B美元。”OneWeb创始人Greg Wyler在美国国会听证会上作证时，于2017年将其描述为40亿美元。关于卫星连接。 OneWeb首席执行官阿德里安·斯特克尔在2月27日OneWeb首次在阿丽亚娜空间联盟号上首次发射之前在圭亚那航天中心内的木星控制室内??发表讲话。图片来源：SpaceNews / Caleb Henry OneWeb首席执行官阿德里安·斯特克尔在2月27日OneWeb首次在阿丽亚娜空间联盟号上首次发射之前在圭亚那航天中心内的木星控制室内??发表讲话。图片来源：SpaceNews / Caleb Henry 卫星咨询公司TelAstra的总裁Roger Rusch认为，Wyler的40亿美元估值和Uphold可疑的60亿美元数据都太低了。 他估计OneWeb最终将需要75亿美元来完成其系统，如果它为用户终端提供资金，可能会更多。 “现实数字可能远远超出了他们现在所处的位置，”Rusch说，他帮助制造商设计了几个卫星系统。 “它可能比??它们的三倍多。” OneWeb的个人太空船虽然比传统卫星便宜得多，但仍然至少是OneWeb在计划开始时设想的价格低于50万美元的价格的两倍。 Rusch告诫说，不太明显的成本，如门户地面站，房地产和监管许可，可能会进一步超出早期估算的成本。 其他卫星运营商批评OneWeb的方法不健全 - 这将使发射提供商，卫星制造商及其供应商获利，同时让投资者保持高度干燥。 OneWeb领导层坚持认为情况并非如此。 从项目到业务 “我们知道我们会卖掉[容量]，”Steckel说。 “当你以前所未有的覆盖范围以及适用于他们的设备提供快速数据时，你就卖光了。” OneWeb还有很长的路要走，以销售预想的648颗卫星初始系统的容量。 发布当天，OneWeb宣布了其前两个客户：英国卫星传送和网络运营商Talia以及意大利电信公司Intermatica。 OneWeb拒绝提供合约价值，但Steckel表示，Talia的交易“几乎使我们的一年只与那份合约相提并论”。 携带OneWeb第一颗卫星的联盟号火箭也带有FIRST（用于启发和识别科学和技术）的标识，这是一个致力于灌输对科学和技术着迷的年轻人的非营利组织。图片来源：ESA / CNES / ARIANESPACE 携带OneWeb第一颗卫星的联盟号火箭也带有FIRST（用于启发和识别科学和技术）的标识，这是一个致力于灌输对科学和技术着迷的年轻人的非营利组织。 图片来源：ESA / CNES / ARIANESPACE 目前尚不清楚宣布的合同将在多长时间内为OneWeb带来可观的收入。 在新闻发布会上，OneWeb表示其服务“将于2021年开始实施Talia，几乎所有Talia的市场都将在2023年启动。”Talia发言人Elliot Banks拒绝对合同的财务条款发表评论。 OneWeb宣布Talia和Intermatic合同也显示其与最大投资者SoftBank的关系发生了重大变化。 SoftBank最初声称完全拥有OneWeb的容量，但Wyler在2月27日说两家公司已经放弃了这种方法。 “这是我们一直在努力的事情，他们拥有所有系统的所有能力，他们将转售它，但我们已经改变了这种模式，”他说。 威勒没有给出确切的更改日期，称它“逐渐发生了几个月”。 由于SoftBank不再是该星座的全部产能的保证购买者，OneWeb作为一个企业的成功更依赖于自己的肩膀。 债务融资被搁置 OneWeb的2019年3月的融资也标志着以前计划依靠债务融资来满足其余的资金需求。 “它发生了变化，”Steckel在解释OneWeb回归股东的计划时表示，而不是通过出口信贷机构安排债务融资，出口信贷机构通常会根据客户承诺证明其支持。 “我们不想做的就是依赖外部融资，”Steckel说。 “这将迫使我们提前签署商业交易，并给予客户太多折扣。” Steckel表示，早鸟折扣可能会使OneWeb产能价格的预期低于公司想要的价格，从而将销售价格变为事实上的常态。 “我们的问题不是'我们会卖光吗？' 我们的问题是确保我们签署正确的交易，以便我们不会以太快的价格和错误的价格出售，“他说。 OneWeb股??票的首次公开募股也可用于满足公司未来的资本需求。 Steckel表示OneWeb“绝对”计划上市 - 只是暂时没有。 “我们的任务是将连接带到各处......但要实现这一目标，我们需要建立一个可行的业务，”Steckel说。 “这是一种优质产品，它需要投入大量资金，我们需要能够将其货币化。” 达到盈利 OneWeb预测，它的初始系统将准备好在2021年达到全球覆盖时提供500毫秒/秒的连接，仅有30毫秒的延迟时间。 目前，OneWeb的前六颗卫星（可操作的航天器，而不是原型）可以一次共同提供18分钟的服务 - 对于OneWeb打算服务的大多数客户来说太少了。 但每次连续发射都会增加，极地地区成为第一个获得24小时覆盖的地区。 OneWeb预计今年将发射大约150颗卫星，使用四枚或五枚联盟号火箭和潜在的维珍轨道尚未发射的LauncherOne，它一次可携带一到两颗卫星。 怀勒说，到2020年，该公司应该能够达到300颗卫星 - 足以使加拿大和其他靠近北极和南极的地区有24小时的覆盖范围。 他说，随着星座扩大到648，24小时覆盖的树冠将到达赤道，提供全球覆盖。 Steckel说全面服务应该在2021年或2022年初开始。 Steckel表示，OneWeb将在2022年实现盈利，直到全球服务的前六个月实现盈利。 外界观察人士告诫说，在星座开始产生大量收入之前，OneWeb可能会烧掉更多现金。 “他们需要达到这种有意义的服务水平，无论是300颗卫星还是其他任何东西，以便其他金融家认为网络的完整构建是可行的，”安德森说。 “看起来非常接近，他们现在能够做到这一点，但如果收入只是涓涓细流，或者利润率大大低于传统卫星运营商可以获得的高利润率，那么我认为他们无法做到获得额外的数十亿美元。“ 空间破碎 OneWeb成立的目标是为全球各个角落提供快速，经济实惠的互联网。 “我们设想一个所有人都可以访问的世界，并拥有为自己和他人创造机会的能力，”OneWeb目前正在描述其使命。 但是，弥合世界数字鸿沟将需要OneWeb建立一个成功的业务，围绕连接比偏远村庄或典型学校更深的口袋的客户。 理查德·布兰森爵士离开，与OneWeb创始人格雷格·怀勒一起在法属圭亚那的圭亚那航天中心为OneWeb的2月27日首次发布。图片来源：SpaceNews / Caleb Henry 理查德·布兰森爵士离开，与OneWeb创始人格雷格·怀勒一起在法属圭亚那的圭亚那航天中心为OneWeb的2月27日首次发布。 图片来源：SpaceNews / Caleb Henry Steckel表示，使用OneWeb卫星回传蜂窝塔通信流量的移动网络运营商将占公司早期客户的很多用户。 他将航空，海事和政府客户列为其他预期的早期用户 - 与学校一起。 “有许多不同的方法或财务模型可以连接不同国家的不同支付能力的学校，”怀勒说。 威勒表示，他在OneWeb的主要工作是确保公司忠于其慈善目标，同时平衡金融投资并实现公司的“快速盈利”。 “我们今天的投资者有很好的平衡，他们正在关注双方，”他说。 维珍银河的亿万富翁创始人理查德布兰森就是这样一位投资者。 在发布会上，布兰森将OneWeb描述为“空间破碎”，因为它可以带来差异。 “这将是巨大的，”他说。 怀勒表示，耗资20亿美元的OneWeb已经花费了几十年的时间，几乎已经准备好开始发射数十颗卫星，这是“一次性”开支，用于确保发射，建立制造基础设施和建立供应链。 他说，从这一点来看，构建和扩展系统的边际成本很低。 但是，关于OneWeb实现梦想能力的问题依然存在。";
		System.out.println(summarise(s, 5));
	}
}
