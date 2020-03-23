package io.renren.modules.spider.utils.common;

import com.bestdata.nlp.seg.maxword.WordMaxSegment;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlaceRec extends WordMaxSegment {
	 /**词典**/
    public static Set<String> dict;
    public PlaceRec(int max_len) {
		super(max_len);
	}
	static {
    	 dict=initDict(root+"\\data\\term_place.txt","UTF-8");
    }
	@Override
	public void reloadDict(String dictPath, String charset) {
		dict=initDict(dictPath,charset);
	}

	@Override
	public void reloadDict(Collection<String> dicts) {
		dict = new HashSet<String>(dicts);
	}
	@Override
	public List<String> keyword(String text) {
		return keyword(dict, text);
	}
	public static void main(String[] args){
    	PlaceRec bimax=new PlaceRec(5);
        String text="当他们返回南方在自己的家乡产卵时，西北太平洋的鲑鱼穿过数种不同的虎鲸种群的觅食地，这些种群似乎与大型奇努克族有着密切的亲和力。 这些繁华的虎鲸实际上可能是从南部常住的逆戟鲸种群中偷走了一顿饭，而该种群正努力维持73个人。";
//        System.out.println(bimax.rmm_segment(text));
        System.out.println(bimax.keyword(text));
    }
}
