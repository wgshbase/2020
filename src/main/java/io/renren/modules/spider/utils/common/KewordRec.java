package io.renren.modules.spider.utils.common;

import com.bestdata.nlp.seg.maxword.WordMaxSegment;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KewordRec extends WordMaxSegment {
	 /**词典**/
    public static Set<String> dict;
    public KewordRec(int max_len) {
		super(max_len);
	}
	static {
    	 dict=initDict(root+"\\data\\term.txt","UTF-8");
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
    	KewordRec bimax=new KewordRec(5);
        String text="我在气温餐厅吃饭，饭菜好难吃气温啊！";
//        System.out.println(bimax.rmm_segment(text));
        System.out.println(bimax.keyword(text));
    }

	


}
 