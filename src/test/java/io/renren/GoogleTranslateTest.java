package io.renren;

import com.alibaba.fastjson.JSONArray;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @Author wgsh
 * @Date wgshb on 2019/4/19 9:40
 */
public class GoogleTranslateTest {
	public static void main(String[] args) throws Exception {
		System.out.println("-----test-----");
		String txt="永和九年，岁在癸丑，暮春之初，会于会稽山阴之兰亭，修禊事也。群贤毕至，少长咸集。此地有崇山峻岭，茂林修竹；又有清流激湍，映带左右，引以为流觞曲水，列坐其次。虽无丝竹管弦之盛，一觞一咏，亦足以畅叙幽情。是日也，天朗气清，惠风和畅，仰观宇宙之大，俯察品类之盛，所以游目骋怀，足以极视听之娱，信可乐也。\n" +
				"夫人之相与，俯仰一世，或取诸怀抱，悟言一室之内；或因寄所托，放浪形骸之外。虽趣舍万殊，静躁不同，当其欣于所遇，暂得于己，快然自足，不知老之将至。及其所之既倦，情随事迁，感慨系之矣。向之所欣，俯仰之间，已为陈迹，犹不能不以之兴怀。况修短随化，终期于尽。古人云：“死生亦大矣。”岂不痛哉！(不知老之将至 一作：曾不知老之将至)\n" +
				"每览昔人兴感之由，若合一契，未尝不临文嗟悼，不能喻之于怀。固知一死生为虚诞，齐彭殇为妄作。后之视今，亦犹今之视昔。悲夫！故列叙时人，录其所述，虽世殊事异，所以兴怀，其致一也。后之览者，亦将有感于斯文。";
		GT g = GT.getInstance();
		System.out.println( g.translateText(txt,"auto","en"));
		System.out.println( g.translateText("谁能说支持不支持","auto","en"));
		String content = "<div itemprop=\"articleBody\" class=\"article-body\"> \n" +
				" <div class=\"hidden-lg meta\"> \n" +
				"  <ul> \n" +
				"   <li><time pubdate datetime=\"April 11, 2019\">April 11, 2019</time></li> \n" +
				"   <li>News</li> \n" +
				"   <li>By C. Todd Lopez <a href=\"\" target=\"_blank\"><i class=\"fa fa-twitter\"></i></a> &nbsp;|&nbsp; <a href=\"https://dod.defense.gov/Resources/Contact-DoD/Contact-Author/?articleurl=https://dod.defense.gov/News/Article/Article/1812069/reserve-components-are-focused-on-readiness-leaders-say/\">Contact Author</a> </li> \n" +
				"   <li>Defense.gov</li> \n" +
				"  </ul> \n" +
				" </div> \n" +
				" <hr> \n" +
				" <span >WASHINGTON -- </span> \n" +
				" <p>Readiness was a top priority for both the National Guard and the services’ Reserve components when planning budgets for fiscal year 2020, their leaders told Congress yesterday.</p> \n" +
				" <p>During a hearing of the Senate Appropriations Committee’s defense subcommittee, the chiefs of all four service reserve components and the National Guard Bureau briefed lawmakers on the status of the components they lead, and some of the priorities that went into developing their budget request for fiscal 2020.</p> \n" +
				" <p>Air Force Gen. Joseph L. Lengyel, chief of the National Guard Bureau, said the three priorities for the National Guard included preparing to fight overseas, defending the nation and building partnerships. Each of those missions supports the national defense strategy, he said.</p> \n" +
				" <p>Lengyel also told lawmakers that for the National Guard to be ready to do what it needs to do — to be deployable, sustainable and interoperable with the active component of the Army and Air Force -- “we require such things as appropriate levels of full-time support, and replacing and upgrading old, worn-out facilities. We require parity in equipping our force through concurrent and balanced modernization and recapitalization of our force along with our active components.”</p> \n" +
				" <p><strong>Service Plans</strong></p> \n" +
				" <p>Army Gen. Charles D. Luckey, chief of the Army Reserve, highlighted “Ready Force X” in his submitted testimony.</p> \n" +
				" <p>“This construct, Ready Force X, remains the way in which we focus energy, optimize our process, and prioritize our resources to deliver capabilities at the speed of relevance for a major war,” he said.</p> \n" +
				" <p>Ready Force X, or RFX, Luckey said, begins at the individual soldier level.</p> \n" +
				" <p>“While many aspects of collective readiness at the unit level can be tuned up quickly upon mobilization, the key individual soldier requirements of physical fitness, medical readiness, tactical discipline, professional education, and fieldcraft proficiency must be ‘baked in’ to the entire force,” he said. “Put simply, at a profound level, we are all in RFX.”</p> \n" +
				" <p>Air Force Lt. Gen. Richard W. Scobee, chief of the Air Force Reserve, said his focus is to “prepare to operate in tomorrow’s battlespace while providing excellent support to our airmen and their families.”</p> \n" +
				" <p>In the past year, Scobee said, the Air Force Reserve activated its first cyber wing and its first intelligence, surveillance and reconnaissance wing, which is expected to help the service further expand its support off those missions.</p> \n" +
				" <p>Vice Adm. Luke M. McCollum, chief of the Navy Reserve, said recapitalization of aging hardware “is critical to ensuring the highest levels of readiness and interoperability with the active component.”</p> \n" +
				" <p>A No. 1 priority there, he said, is recapitalization of the Navy Reserve's Maritime Patrol and Reconnaissance Force capability. Two Navy Reserve units, one at Whidbey Island, Washington, and the other at Jacksonville, Florida, currently operate P-3C maritime patrol aircraft. &nbsp;Plans are to replace those with the P-8A Poseidon aircraft.</p> \n" +
				" <p>“Recapitalization of the two RC squadrons currently operating the legacy P-3C’s, through FY 2022, with additional P-8A aircraft, aircrews and associated military construction, will buy down warfighting risk,” McCollum said in his written statement.</p> \n" +
				" <p>The budget request seeks funds to help accomplish that, McCollum said, as well as other Navy Reserve priorities such as avionics upgrades for the C-130T aircraft, 40-foot patrol boats and the Joint Reserve Intelligence Centers.</p> \n" +
				" <p>Maj. Gen. Bradley S. James, commander of Marine Corps Forces Reserve, told senators how they could contribute to a “more ready and lethal Marine Reserve force.”</p> \n" +
				" <p>First, he cited continued support of the National Guard and Reserve Equipment Appropriation, and he asked for “greater spending flexibility within this appropriation in order to procure critical shortfall items, and modernize equipment and systems.”</p> \n" +
				" <p>Second, he highlighted the timeliness of the fiscal 2019 &nbsp;budget and said the predictability has a positive impact in the training of Reserve Marines.</p> \n" +
				" <p>“I would like to thank you for this year’s appropriations,” he told the panel. “On average the Marine Corps Reserve [members] only have 38 training days a year, which places an increase in importance on adequate and timely appropriation.”</p> \n" +
				" <br> \n" +
				"</div>";

		System.out.println(g.translateText(content, "auto", "zh_cn"));


	}
}

/**
 * 谷歌翻译类
 */
class GT {

	private static final String PATH="https://translate.googleapis.com/translate_a/single"; //地址
	private static final String CLIENT="gtx";

	private static final String USER_AGENT="Mozilla/5.0";//"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36";

	private static final Map<String,String> LANGUAGE_MAP=new HashMap();

	private static GT _instance = null;

	/**
	 * 获取单例
	 * @return
	 */
	public static GT getInstance() {
		if( null == _instance){
			_instance = new GT();
			_instance.init();
		}
		return _instance;
	}

	/**
	 * 初始化语言类
	 */
	private void init(){
		LANGUAGE_MAP.put("auto","Automatic");
		LANGUAGE_MAP.put("af","Afrikaans");
		LANGUAGE_MAP.put("sq","Albanian");
		LANGUAGE_MAP.put("am","Amharic");
		LANGUAGE_MAP.put("ar","Arabic");
		LANGUAGE_MAP.put("hy","Armenian");
		LANGUAGE_MAP.put("az","Azerbaijani");
		LANGUAGE_MAP.put("eu","Basque");
		LANGUAGE_MAP.put("be","Belarusian");
		LANGUAGE_MAP.put("bn","Bengali");
		LANGUAGE_MAP.put("bs","Bosnian");
		LANGUAGE_MAP.put("bg","Bulgarian");
		LANGUAGE_MAP.put("ca","Catalan");
		LANGUAGE_MAP.put("ceb","Cebuano");
		LANGUAGE_MAP.put("ny","Chichewa");
		LANGUAGE_MAP.put("zh_cn","Chinese Simplified");
		LANGUAGE_MAP.put("zh_tw","Chinese Traditional");
		LANGUAGE_MAP.put("co","Corsican");
		LANGUAGE_MAP.put("hr","Croatian");
		LANGUAGE_MAP.put("cs","Czech");
		LANGUAGE_MAP.put("da","Danish");
		LANGUAGE_MAP.put("nl","Dutch");
		LANGUAGE_MAP.put("en","English");
		LANGUAGE_MAP.put("eo","Esperanto");
		LANGUAGE_MAP.put("et","Estonian");
		LANGUAGE_MAP.put("tl","Filipino");
		LANGUAGE_MAP.put("fi","Finnish");
		LANGUAGE_MAP.put("fr","French");
		LANGUAGE_MAP.put("fy","Frisian");
		LANGUAGE_MAP.put("gl","Galician");
		LANGUAGE_MAP.put("ka","Georgian");
		LANGUAGE_MAP.put("de","German");
		LANGUAGE_MAP.put("el","Greek");
		LANGUAGE_MAP.put("gu","Gujarati");
		LANGUAGE_MAP.put("ht","Haitian Creole");
		LANGUAGE_MAP.put("ha","Hausa");
		LANGUAGE_MAP.put("haw","Hawaiian");
		LANGUAGE_MAP.put("iw","Hebrew");
		LANGUAGE_MAP.put("hi","Hindi");
		LANGUAGE_MAP.put("hmn","Hmong");
		LANGUAGE_MAP.put("hu","Hungarian");
		LANGUAGE_MAP.put("is","Icelandic");
		LANGUAGE_MAP.put("ig","Igbo");
		LANGUAGE_MAP.put("id","Indonesian");
		LANGUAGE_MAP.put("ga","Irish");
		LANGUAGE_MAP.put("it","Italian");
		LANGUAGE_MAP.put("ja","Japanese");
		LANGUAGE_MAP.put("jw","Javanese");
		LANGUAGE_MAP.put("kn","Kannada");
		LANGUAGE_MAP.put("kk","Kazakh");
		LANGUAGE_MAP.put("km","Khmer");
		LANGUAGE_MAP.put("ko","Korean");
		LANGUAGE_MAP.put("ku","Kurdish (Kurmanji)");
		LANGUAGE_MAP.put("ky","Kyrgyz");
		LANGUAGE_MAP.put("lo","Lao");
		LANGUAGE_MAP.put("la","Latin");
		LANGUAGE_MAP.put("lv","Latvian");
		LANGUAGE_MAP.put("lt","Lithuanian");
		LANGUAGE_MAP.put("lb","Luxembourgish");
		LANGUAGE_MAP.put("mk","Macedonian");
		LANGUAGE_MAP.put("mg","Malagasy");
		LANGUAGE_MAP.put("ms","Malay");
		LANGUAGE_MAP.put("ml","Malayalam");
		LANGUAGE_MAP.put("mt","Maltese");
		LANGUAGE_MAP.put("mi","Maori");
		LANGUAGE_MAP.put("mr","Marathi");
		LANGUAGE_MAP.put("mn","Mongolian");
		LANGUAGE_MAP.put("my","Myanmar (Burmese)");
		LANGUAGE_MAP.put("ne","Nepali");
		LANGUAGE_MAP.put("no","Norwegian");
		LANGUAGE_MAP.put("ps","Pashto");
		LANGUAGE_MAP.put("fa","Persian");
		LANGUAGE_MAP.put("pl","Polish");
		LANGUAGE_MAP.put("pt","Portuguese");
		LANGUAGE_MAP.put("ma","Punjabi");
		LANGUAGE_MAP.put("ro","Romanian");
		LANGUAGE_MAP.put("ru","Russian");
		LANGUAGE_MAP.put("sm","Samoan");
		LANGUAGE_MAP.put("gd","Scots Gaelic");
		LANGUAGE_MAP.put("sr","Serbian");
		LANGUAGE_MAP.put("st","Sesotho");
		LANGUAGE_MAP.put("sn","Shona");
		LANGUAGE_MAP.put("sd","Sindhi");
		LANGUAGE_MAP.put("si","Sinhala");
		LANGUAGE_MAP.put("sk","Slovak");
		LANGUAGE_MAP.put("sl","Slovenian");
		LANGUAGE_MAP.put("so","Somali");
		LANGUAGE_MAP.put("es","Spanish");
		LANGUAGE_MAP.put("su","Sundanese");
		LANGUAGE_MAP.put("sw","Swahili");
		LANGUAGE_MAP.put("sv","Swedish");
		LANGUAGE_MAP.put("tg","Tajik");
		LANGUAGE_MAP.put("ta","Tamil");
		LANGUAGE_MAP.put("te","Telugu");
		LANGUAGE_MAP.put("th","Thai");
		LANGUAGE_MAP.put("tr","Turkish");
		LANGUAGE_MAP.put("uk","Ukrainian");
		LANGUAGE_MAP.put("ur","Urdu");
		LANGUAGE_MAP.put("uz","Uzbek");
		LANGUAGE_MAP.put("vi","Vietnamese");
		LANGUAGE_MAP.put("cy","Welsh");
		LANGUAGE_MAP.put("xh","Xhosa");
		LANGUAGE_MAP.put("yi","Yiddish");
		LANGUAGE_MAP.put("yo","Yoruba");
		LANGUAGE_MAP.put("zu","Zulu");
	}

	/**
	 * 判断语言是否支持
	 * @param language
	 * @return
	 */
	public boolean isSupport(String language){
		if( null == LANGUAGE_MAP.get( language )){
			return false;
		}
		return true;
	}

	/** 获取 语言代码
	 * ISO 639-1 code
	 * @param desiredLang 语言
	 * @return 如果返回null则标示不支持
	 */
	public String getCode(String desiredLang){
		if( null != LANGUAGE_MAP.get(desiredLang)){
			return desiredLang;
		}
		String tmp = desiredLang.toLowerCase();
		for(Map.Entry<String, String> enter : LANGUAGE_MAP.entrySet() ){
			if( enter.getValue().equals( tmp)){
				return enter.getKey();
			}
		}

		return null;
	}


	/**
	 * 翻译文本
	 * @param text  文本内容
	 * @param sourceLang  文本所属语言。如果不知道，可以使用auto
	 * @param targetLang  目标语言。必须是明确的有效的目标语言
	 * @return
	 * @throws Exception
	 */
	public String translateText(String text,String sourceLang, String targetLang) throws Exception{


		String retStr="";
		if( !( isSupport(sourceLang) || isSupport(targetLang) ) ){
			throw new Exception("不支持的语言类型");
		}

		List<NameValuePair> nvps = new ArrayList();
		nvps.add(new BasicNameValuePair("client", CLIENT));
		nvps.add(new BasicNameValuePair("sl", sourceLang));
		nvps.add(new BasicNameValuePair("tl", targetLang));
		nvps.add(new BasicNameValuePair("dt", "t"));
		nvps.add(new BasicNameValuePair("q", text));
//        String finalPath=PATH +"?client="+CLIENT+"&sl="+sourceLang+"&tl="+targetLang+"&dt=t&q="+ text ;

		String resp =  postHttp( PATH,nvps);
		if( null == resp ){
			throw  new Exception("网络异常");
		}

//        System.out.println( "==>返回内容：" + resp);

		JSONArray jsonObject = JSONArray.parseArray( resp );
		for (Iterator<Object> it = jsonObject.getJSONArray(0).iterator(); it.hasNext(); ) {
			JSONArray a = (JSONArray) it.next();
			retStr += a.getString(0);
		}

		return retStr;
	}


	/**
	 * post 请求
	 * @param url 请求地址
	 * @param nvps 参数列表
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private   String postHttp( String url ,List<NameValuePair> nvps){
		String responseStr = null;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost( url );
		//重要！！必须设置 http 头，否则返回为乱码
		httpPost.setHeader("User-Agent",USER_AGENT);
		CloseableHttpResponse response2 = null;
		try {
			// 重要！！ 指定编码，对中文进行编码
			httpPost.setEntity( new UrlEncodedFormEntity(nvps, Charset.forName("UTF-8"))  );
			response2 = httpclient.execute(httpPost);
			HttpEntity entity2 = response2.getEntity();
			responseStr = EntityUtils.toString(entity2);
			EntityUtils.consume(entity2);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != response2) {
				try {
					response2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if( null != httpclient ){
				try {
					httpclient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return responseStr;
	}


}
