package com.mss.crawler.spiderjson.util.parsedoc;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.stopword.CoreStopWordDictionary;
import com.hankcs.hanlp.seg.common.Term;

import io.renren.modules.spider.dao.NewsDao;
import io.renren.modules.spider.entity.News;


public class TFIDF
{
    private static int keywordsNumber = 15;
    
    /**
     * 改变关键词的数量, 默认 5
     * change the number of keywords,default 5
     * @param keywordNum(int): number of keywords that need to be extracted
     */
    public static void setKeywordsNumber(int keywordNum)
    {
        keywordsNumber = keywordNum;
    }

	/**
	 * 计算某一段文字里面每一个词的 TF(Term Frequency) 词频值
     * calculate TF value of each word in terms of  the content of file
     * @param fileContent(String): content of file
     * @return(HashMap<String, Float>): "words:TF value" pairs
     */
    public static HashMap<String, Float> getTF(String fileContent)
    {    
    	List<Term> terms=new ArrayList<Term>();
        ArrayList<String> words = new ArrayList<String>();
       
        //terms=HanLP.segment(fileContent);
        terms =HanLP.newSegment().seg(fileContent);
        //terms = TextRank.parseBaiduAPI2Terms(fileContent);
        for(Term t:terms)
        {
        	if(TFIDF.shouldInclude(t) && t.nature.startsWith("n") && !t.nature.startsWith("nr") )
        	{
        		words.add(t.word);
        	}      		
        }
        
        // get TF values, wordCount 集合对应的 key 为词, 而对应的 value 则为该词所对应的总数
    	 HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
    	 // TFValues 的 key 对应为词, 而 value 对应的则是该词的 TF 值
    	 HashMap<String, Float> TFValues = new HashMap<String, Float>();
    	 for(String word : words)
         {
             if(wordCount.get(word) == null)
             {
            	 wordCount.put(word, 1);
             }
             else
             {
            	 wordCount.put(word, wordCount.get(word) + 1);
             }
         }
    	 
    	 // 当前文本中所有的词语的数量
         int wordLen = words.size();
         //traverse the HashMap
         Iterator<Map.Entry<String, Integer>> iter = wordCount.entrySet().iterator(); 
         while(iter.hasNext())
         {
             Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>)iter.next();
             TFValues.put(entry.getKey().toString(), Float.parseFloat(entry.getValue().toString()) / wordLen);
           //System.out.println(entry.getKey().toString() + " = "+  Float.parseFloat(entry.getValue().toString()) / wordLen);
         }
         return TFValues;
     } 
  
    
    /**
     * 判断某一个词是否属于停用词
     * judge whether a word belongs to stop words
     * @param term(Term): word needed to be judged
     * @return(boolean):  if the word is a stop word,return false;otherwise return true    
     */
    public static boolean shouldInclude(Term term)
    {
        return CoreStopWordDictionary.shouldInclude(term);
    }
      
    
    /**
     * 计算指定的文件夹下的每一个文件的 TF 值
     * calculate TF values for each word of each file under a directory
     * @param dirPath(String): path of the directory
     * @return(HashMap<String,HashMap<String, Float>>): path of file and its  corresponding "word-TF Value" pairs
     * @throws IOException
     */
    public static HashMap<String,HashMap<String, Float>> tfForDir(String dirPath) 
    {
        HashMap<String, HashMap<String, Float>> allTF = new HashMap<String, HashMap<String, Float>>();
        List<String> filelist = ReadDir.readDirFileNames(dirPath);
        
        for(String file : filelist)
        {
            HashMap<String, Float> dict = new HashMap<String, Float>();
            String title = ReadFile.loadFile(file).get("title");// modify the loadFile method of class ReadFile
            String content = ReadFile.loadFile(file).get("content");// modify the loadFile method of class ReadFile
            content =title+"。"+content;
            if(StringUtils.isEmpty(content)) {
            	content = "";
            }
            dict = TFIDF.getTF(content);
            allTF.put(file, dict);
        }
        return allTF;
    }

    
    /**
     * 计算指定的文件下的文件的 idf 值
     * calculate IDF values for each word  under a directory
     * @param dirPath(String): path of the directory
     * @return(HashMap<String, Float>): "word:IDF Value" pairs
     */
    public static HashMap<String, Float> idfForDir(String dirPath)
    {
    	List<String> fileList = new ArrayList<String>();
    	fileList = ReadDir.readDirFileNames(dirPath);
    	int docNum = fileList.size();  
    	
        Map<String, Set<String>> passageWords = new HashMap<String, Set<String>>();        
        // get words that are not repeated of a file 
        int count =1;
        for(String filePath:fileList)
        {   
        	System.out.println(count+++"filePath: "+filePath);
        	
        	List<Term> terms=new ArrayList<Term>();
            Set<String> words = new HashSet<String>();
            String title = ReadFile.loadFile(filePath).get("title"); // remember to modify the loadFile method of class ReadFile
            String content = ReadFile.loadFile(filePath).get("content"); // remember to modify the loadFile method of class ReadFile
            try {
				//terms=HanLP.segment(content);
            	terms =HanLP.newSegment().seg(title+"。"+content);
             //  terms = TextRank.parseBaiduAPI2Terms(content);
			} catch (Exception e) {
				terms = new ArrayList<Term>();
				e.printStackTrace();
			}
            for(Term t:terms)
            {
            	if(TFIDF.shouldInclude(t)&&t.nature.startsWith("n") && !t.nature.startsWith("nr"))
            	{
            		words.add(t.word);
            	}      		
            }
            passageWords.put(filePath, words);
        }
        System.out.println("get IDF values------");
        // get IDF values
        HashMap<String, Integer> wordPassageNum = new HashMap<String, Integer>();
        for(String filePath : fileList)
        {
            Set<String> wordSet = new HashSet<String>();
            wordSet = passageWords.get(filePath);
            for(String word:wordSet)
            {           	
                if(wordPassageNum.get(word) == null)
                	wordPassageNum.put(word,1);
                else             
                	wordPassageNum.put(word, wordPassageNum.get(word) + 1);           
            }
        }
        
        HashMap<String, Float> wordIDF = new HashMap<String, Float>(); 
        Iterator<Map.Entry<String, Integer>> iter_dict = wordPassageNum.entrySet().iterator();
        while(iter_dict.hasNext())
        {
            Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>)iter_dict.next();
            float value = (float)Math.log( docNum / (Float.parseFloat(entry.getValue().toString())) );
            wordIDF.put(entry.getKey().toString(), value);
            //System.out.println(entry.getKey().toString() + "=" +value);
        }
        return wordIDF;
    }

    
    /**
     * 计算指定的文件夹下的每一篇文件的 TFIDF 值
     * calculate TF-IDF value for each word of each file under a directory
     * @param dirPath(String): path of the directory
     * @return(Map<String, HashMap<String, Float>>): path of file and its corresponding "word:TF-IDF Value" pairs
     */
    public static Map<String, HashMap<String, Float>> getDirTFIDF(String dirPath)
    {
        HashMap<String, HashMap<String, Float>> dirFilesTF = new HashMap<String, HashMap<String, Float>>();  
        HashMap<String, Float> dirFilesIDF = new HashMap<String, Float>(); 
        
        dirFilesTF = TFIDF.tfForDir(dirPath);
        dirFilesIDF = TFIDF.idfForDir(dirPath);
        
        Map<String, HashMap<String, Float>> dirFilesTFIDF = new HashMap<String, HashMap<String, Float>>(); 
        Map<String,Float> singlePassageWord= new HashMap<String,Float>();
        List<String> fileList = new ArrayList<String>();
        fileList = ReadDir.readDirFileNames(dirPath);
        for (String filePath: fileList)
        {
        	HashMap<String,Float> temp= new HashMap<String,Float>();
        	singlePassageWord = dirFilesTF.get(filePath);
        	Iterator<Map.Entry<String, Float>> it = singlePassageWord.entrySet().iterator();
        	while(it.hasNext())
        	{
        		Map.Entry<String, Float> entry = it.next();
        		String word = entry.getKey();
        		if(entry.getValue() != null && dirFilesIDF.get(word) != null) {
                    Float TFIDF = entry.getValue()*dirFilesIDF.get(word);
                    temp.put(word, TFIDF);
                }
        	}
        	dirFilesTFIDF.put(filePath, temp);
        }
        return dirFilesTFIDF;
    }
    /**
     * 计算某一篇文章的正文抽取出的 TFIDF 值
     * calculate TF-IDF value for each word of each file under a directory
     * @param dirPath(String): path of the directory
     * @return(Map<String, HashMap<String, Float>>): path of file and its corresponding "word:TF-IDF Value" pairs
     */
    public static Map<String,Float> getTFIDF(String title, String content,Map<String, Float> dirFilesIDF)
    {
        HashMap<String, Float> dict = new HashMap<String, Float>();
        content =title+"。"+content;
        if(StringUtils.isEmpty(content)) {
        	content = "";
        }
        dict = TFIDF.getTF(content);
        
        	HashMap<String,Float> temp= new HashMap<String,Float>();
        	Iterator<Map.Entry<String, Float>> it = dict.entrySet().iterator();
        	while(it.hasNext())
        	{
        		Map.Entry<String, Float> entry = it.next();
        		String word = entry.getKey();
        		if(entry.getValue() != null && dirFilesIDF.get(word) != null) {
                    Float TFIDF = entry.getValue()*dirFilesIDF.get(word);
                    temp.put(word, TFIDF);
                }
        	} 
        return temp;
    }
 
    
    /**
     * 返回指定的路径下的每一篇文档的关键字
     * get keywords of each file under a certain directory 
     * @param dirPath(String): path of directory
     * @return(Map<String,List<String>>): path of file and its corresponding keywords
     */
    public static Map<String,List<String>> getKeywords(String dirPath)
    {
    	List<String> fileList = new ArrayList<String>();
    	fileList = ReadDir.readDirFileNames(dirPath);
    	
    	// calculate TF-IDF value for each word of each file under the dirPath
    	Map<String, HashMap<String, Float>> dirTFIDF = new HashMap<String, HashMap<String, Float>>(); 
    	dirTFIDF = TFIDF.getDirTFIDF(dirPath);
    	
    	Map<String,List<String>> keywordsForDir = new HashMap<String,List<String>>(); 
    	for (String file:fileList)
    	{
    		Map<String,Float> singlePassageTFIDF= new HashMap<String,Float>();
    		singlePassageTFIDF = dirTFIDF.get(file);
    		
    		//sort the keywords in terms of TF-IDF value in descending order
	        List<Map.Entry<String,Float>> entryList=new ArrayList<Map.Entry<String,Float>>(singlePassageTFIDF.entrySet());
	        
	
	        Collections.sort(entryList,new Comparator<Map.Entry<String,Float>>()
	        {
	        	public int compare(Map.Entry<String,Float> c1,Map.Entry<String,Float> c2)
	        	{
	        		return c2.getValue().compareTo(c1.getValue()); 	        		
	        	}
	        }
	        );
	        	        
	       // get keywords 
            List<String> systemKeywordList=new ArrayList<String>();
            for(int k=0;k<keywordsNumber;k++)
            {
            	try
            	{

            	    String keyword = entryList.get(k).getKey();
                    /**
                     * 标点符号、常用词、以及“名词、动词、形容词、副词之外的词”
                     */
                    Set<String> stopWordSet = NewsExtract.readText2Set("stopwords.txt");

                    // 排除停用词范围内的名词
                    if(!stopWordSet.contains(keyword) && keyword.length() > 1) {
                        systemKeywordList.add(entryList.get(k).getKey());
                    }

            	}
            	catch(IndexOutOfBoundsException e)
            	{
            		continue;
            	}
            }
            
            keywordsForDir.put(file, systemKeywordList);
        }
        return keywordsForDir;
    }
           
}
