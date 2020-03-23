package io.renren.modules.spider.dao;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.*;

import io.renren.modules.spider.entity.News;
import io.renren.modules.spider.entity.WebInfo;
import io.renren.modules.sys.dao.BaseDao;




/**
 * 爬取信息
 * 
 * @author wangdw
 * @email dawei.happy@gmail.com
 * @date 2016年9月18日 上午9:43:39
 */

@Mapper
public interface NewsDao extends BaseDao<News> {

	/**
	 * 更新新闻的字段
	 */
	void updateNewsInformation(Map<String, Object> params);

	/**
	 * 临时方法, 更新新闻的字段
	 */
	@Update("UPDATE news_wx_copy1 set "
			+ "content=#{content}"
			+ "WHERE title=#{title}")
	void updateNewsColumns(News news);
	
	/**
	 * 临时方法, 仅供测试使用
	 */
	@Select("SELECT * from news_wx_copy1")
	List<News> getNews2Makeup();
	
	/**
	 * 通过keyword匹配title检索*/
	List<News> queryListByKeyword(Map<String, Object> map);
	
	/**
	 * 通过keyword匹配title检索总条数*/
	int queryListCountByKeyword(Map<String, Object> map);
	
	/**
	 * 通过keyword匹配title检索总条数*/
	News queryById(Map<String, Object> map);
	
	/**
	 * 通过 crawlerdate 查询对应的总的所有符合条件的数据
	 * @param params
	 * @return
	 */
	List<LinkedHashMap<String, Object>> getAllBy(Map<String, Object> params);
	
	List<News> getNamespace4Replace();

	/**
	 * 批量更新新闻
	 * @param lists
	 */
	void updatebatch(List<News> lists, @Param("tbname") String tableName);

	/**
	 * 更新新闻
	 * @param news
	 */
	void updateNews(@Param("formattedContent") String formattedContent, 
			@Param("tbname") String tableName,
			@Param("id") String id);
	
	/**
	 * 更新新闻的摘要字段
	 */
	void updateNewsSummary(News news);
	
	/**
	 * 获取所有的新闻对象
	 * @return
	 */
	List<News> selectNews2update();

	/**
	 * 获取武器库武器的所有的名称
	 */
	List<String> getWeaponNames();

	/**
	 * 辅助方法, 通过 附件名称查询对应的 publicname
	 * @param attachfile
	 * @return
	 */
	List<String> getPublicnameByAttachfilesName(@Param("tablename") String tablename, @Param("attachfiles")String attachfile);

	/**
	 * 辅助方法, 通过 id 查询对应的 publicname 
	 * @param substring
	 * @return
	 */
	String getPublicnameById(@Param("tablename") String tablename,@Param("id") String id);

	/**
	 * 返回指定的时间区间内的所有的 pdf 的属性值
	 * @param begindate
	 * @param enddate
	 * @return
	 */
	List<News> getNewsPdfs(@Param("begindate") String begindate, @Param("enddate") String enddate);

	void clearNotExistPdf(News news);

    /**
     * 获取所有的重复的标题
     */
    List<String> getDulpTitles(@Param("tablename") String tablename);

    /**
     * 获取标题重复的 title, 最小 crawlerdate 的集合
     * @param tablename
     * @return
     */
    List<News> getTitleWithMinCrawlerdate(@Param("tablename") String tablename, @Param("list") List<String> titles);

    /**
     * 获取标题重复的所有的 news 的集合
     * @param tablename
     * @return
     */
    List<News> getTitleWithCrawlerdate(@Param("tablename") String tablename, @Param("list") List<String> titles);

    /**
     * 批量删除 数据
     */
    void deleteNewsBatch(@Param("tablename") String tablename, @Param("list") List<String> ids);
    
    /**
     * 查询所有的新闻的正文
     */
    List<News> getNewsContent(@Param("tablename") String tablename);
    
    /**
     * 按照id进行数据库的数据的替换
     */
    void updateNewsContentById(@Param("tablename") String tablename, @Param("content") String content, @Param("id") String id);
    
    /**
     * 添加封面等相关信息
     * 
     */
    @Insert("INSERT INTO news_img(id, title, attchfiles, content, crawlerdate, headimg) VALUES(#{id}, #{title}, #{attchfiles}, #{content}, #{crawlerdate}, #{headimg})")
    void insertHeadImg(News news);

    /**
     * 查询所有的封面的信息
     * @return
     */
    @Select("SELECT id,title,attchfiles,headimg from news_img where attchfiles != ''")
	List<News> selectNewsAttchfiles();

    /**
     * 更新封面字段
     * 
     * @param news
     */
    @Update("UPDATE news_img SET headimg = #{headimg} where id = #{id}")
	void updateHeadimg(News news);
    
    /**
     * // ID, Title, WebName(publicname-number),IssueTime(pubdate),WebInfoContent(content)
     */
    @Select("select id as ID, title as Title, pubdate as IssueTime, attchfiles as attachfiles, content as WebInfoContent,CONCAT_WS('-',publicname,chatnumber) as WebName from news_wx_19 where crawlerdate > #{begindate} and crawlerdate < #{enddate}")
    List<WebInfo> selectNewsAsWebInfo(Map<String, Object> params);

    // 临时方法, 获取数据库的所有的 id
    @Select("SELECT id FROM ${tablename}")
	List<String> getAllIds(@Param("tablename")String tablename);

    // 临时方法, 查询数据库中的 正文部分
	@Select("SELECT id,content from news_en where crawlerdate > '2018-12-05'")
	List<News> selectContentFromNewEn();

	// 临时方法, 更新数据库的 附件 及 发布时间字段
	@Update("UPDATE news_en SET attchfiles = #{attchfiles},pubdate = #{pubdate} where id = #{id}")
	void updatePdfAndPubdate(News news);

	// 临时方法, 查询对应的附件文件名称, 下载到本地
	@Select("SELECT url FROM news_en where crawlerdate > '2018-12-04' and attchfiles != ''")
	List<News> getAttachFiles2Download();

	@Select("SELECT id, attchfiles from news_en")
	List<News> getNews2Delete();

	@Delete("update news_en set attchfiles = #{attchfiles} where id = #{id}")
	void updateAttchfiles2null(News news);

	@Select("SELECT id, title, summary, sourceSite, title_tr, content_tr,siteDomain, pubdate FROM news_en")
	List<News> getTargetNewsEnColumns();

	@Select("SELECT id, content_tr,attchfiles FROM news_en")
	List<News> selectNewsContent2AddImg();

	@Update("UPDATE news_en SET content_tr = #{contentTr} where id=#{id}")
	void updateContentTr(News news);

	// 查询新闻的正文部分
	@Select("SELECT id, title,content FROM ${tablename} WHERE pubdate > #{pubdate}")
	List<News> selectNewsContent(@Param("tablename")String tablename, @Param("pubdate")String pubdate);

	// 查询新闻的总数目
	@Select("SELECT count(title) FROM ${tablename} WHERE pubdate > #{pubdate}")
	int selectTotalCount(@Param("tablename") String tablename, @Param("pubdate")String pubdate);

	// 查询指定的 id 的集合对应的新闻的最小的发布时间
	String selectMinPubdateByGivingIdlist(@Param("list") List<String> idList, @Param("tablename") String tablename);

	// 获取指定的 id 的集合中大于 minPubdate 的 id 的集合
	List<String> removeDulpRecordsHavingOlderPubdate(@Param("list")List<String> idList, @Param("minPubdate") String minPubdate, @Param("tablename") String tablename);

	// 获取发布时间为 minPubdate 的新闻的集合
	List<News> selectCountRecordsWithMinPubdate(@Param("list")List<String> idList, @Param("minPubdate") String minPubdate, @Param("tablename") String tablename);

	// 添加航天百科到本地数据库
	@Insert("INSERT INTO hymax_hang_tian(id, title, summary, tags, xin_wen_zheng_wen, keywords) VALUES(#{id}, #{title}, #{summary}, #{keywords}, #{content}, #{keywords})")
	void addHangtianbaike2LocalTable(News news);

	// 查询之前的 航天百科包含的标题
	@Select("SELECT title FROM hymax_hang_tian_bai_ke_copy")
	List<String> getHangtianbaikeNameList();

	@Insert("INSERT INTO aa(classification_number, major_subject, subject_segmentation, translated_name, isbn, title, deputy_title, book_name, conference_proceedings, version, author, editor, publishing_house, publication_date, number_pages, binding, currency, price, introduction, language, illus, subject_heading, reader_object, fine_paperback_isbn_control, size, xlsname) " +
			"VALUES(#{classification_number}, #{major_subject}, #{subject_segmentation}, #{translated_name}, #{isbn}, #{title}, #{deputy_title}, #{book_name}, #{conference_proceedings}, #{version}, #{author}, #{editor}, #{publishing_house}, #{publication_date}, #{number_pages}, #{binding}, #{currency}, #{price}, #{introduction}, #{language}, #{illus}, #{subject_heading}, #{reader_object}, #{fine_paperback_isbn_control}, #{size}, #{xlsname})")
	void addExcel2AA(Map<String,String> map);

	@Select("SELECT id, introduction as summary from aa where introduction_tr is null")
	List<News> selectIntroduction();

	@Update("UPDATE aa set introduction_tr = #{summary} WHERE id = #{id}")
	void updateAA(News news);

	@Select("SELECT * FROM news_en")
	List<News> selectTotalNews();

	@Insert("INSERT INTO bs_xwzx_11(id, create_time, title, content, cover, datetime, ly, url, keywords)" +
			"values(#{id}, #{crawlerdate}, #{titleTr}, #{contentTr}, #{headimg}, #{pubdate}, #{src}, #{url}, #{keywords})")
	void insertEn2XWZX(News news);

	@Select("select * from news_en where content like #{targetFile}")
	List<News> findTargetNews(@Param("targetFile") String taretFile);

	@Select("SELECT * FROM news_en_zhs WHERE pubdate > #{begindate} AND pubdate < #{enddate}")
	List<News>  findZHSNewsByDate(@Param("begindate") String begindate, @Param("enddate") String enddate);

	@Select("SELECT title,content,id FROM news_cn_copy2 ")
	List<News> findTatgetNewsData();

	@Select("SELECT id, info,title from news_en_satellite")
	List<News> getInfos();

	@Update("UPDATE news_en_satellite SET content = #{content} WHERE id = #{id}")
	void updateContent(@Param("id") String id, @Param("content") String content);

	@Select("SELECT id, info, title from news_en_satellite where info != ''")
	List<News> getTargetTables();

	@Update("UPDATE news_en_satellite SET info = #{info} WHERE id = #{id}")
	void updateInfo(@Param("id") String id, @Param("info") String info);

	@Select("SELECT id, info from news_en_satellite where info like '%jpg%'")
	List<News> getBadImageInfo();

	@Insert("INSERT INTO bs_qxzb(title, wwm, rwlx, yfdw, zsjs, rwsz, wxzk, fssj, gjwxbsf, wxzl, dl, gd, ddwz, yxzh, zyrw, wxpt, yzhj, fsdd, gdlx) " +
			"VALUES(#{title}, #{wwm}, #{rwlx}, #{yfdw}, #{zsjs}, #{rwsz}, #{wxzk}, #{fssj}, #{gjwxbsf}, #{wxzl}, #{dl}, #{gd}" +
			", #{ddwz}, #{yxzh}, #{zyrw}, #{wxpt}, #{yzhj}, #{fsdd}, #{gdlx})")
	void insert2Satellite(Map<String, String> map);

	@Select("SELECT id, content from bs_zcwx")
	List<News> getTargetTablesContent();

	@Update("UPDATE bs_zcwx SET content = #{content} WHERE id = #{id}")
	void updateInfoContent(@Param("id") String id, @Param("content") String content);

	@Select("SELECT id, author from news_en_com_mil_copy")
	List<News> getTargetTablesAuthor();

	@Update("UPDATE bs_zcwx set rwlx=#{rwlx}, dl=#{dl}, yfdw=#{yfdw}, " +
			"rwsz=#{rwsz}, wxzl=#{wxzl}, wxpt=#{wxpt}, yzhj=#{yzhj}, gdlx=#{gdlx} " +
			"where id=#{id}")
	void updateBSWxOtherColumns(Map<String, String> map);

	@Insert("INSERT INTO news_en(id, title, content, pubdate, url, crawlerdate) VALUES(#{id}, #{title},#{content},#{pubdate},#{url}, #{crawlerdate})")
	void addChromeDriverNews2NewsEn(News news);


	@Select("select * from news_cn order by pubDate desc")
	List<News> findSheMiNews();

	List<News> searchProblemByQuery(Map<String, Object> params);

	int searchProblemCountByQuery(Map<String, Object> params);

	// 获取所有的数据
	@Select("SELECT * FROM ${tablename} WHERE crawlerdate > #{crawlerdate}")
	List<News> getAllByParams(Map<String, String> params);

	@Insert("INSERT INTO bs_news_en(id, title, content, datetime, source, url, cover, source_tr, url_tr, cover_tr, title_tr, datetime_tr, content_tr) VALUES(#{id}, #{title}, #{content}, #{datetime}, #{source}, #{url}, #{cover}, #{source}, #{url}, #{cover}, #{title_tr},#{datetime},#{content_tr})")
	void addNews2NewsPlat(Map<String, String> params);

	@Select("SELECT id, SINOTRANSID FROM zz_000")
	List<Map<String, Object>> getAllNews();

	@Select("SELECT SINOTRANSID FROM zz_copy")
	List<String> getIdList();

	@Delete("DELETE FROM zz_000 where id = #{id}")
	void deleteZZ(@Param("id") Integer id);

	@Select("SELECT corpus_cn FROM ${tablename}")
	List<String> getCorpusCnList(@Param("tablename") String tablename);

	@Insert("INSERT INTO corpus_new(id, create_time, corpus_cn, occur_times) VALUES(#{id}, #{createTime}, #{corpusCn}, #{occurTimes})")
	void addCorpusNew(Map<String, Object> map);

	@Select("SELECT occur_times FROM corpus_new WHERE corpus_cn = #{corpusCn}")
	Integer getCorpusNewOccurTimes(@Param("corpusCn") String corpusCn);

	@Update("UPDATE corpus_new SET occurs_times = #{occursTimes} where corpus_cn = #{corpusCn}")
	void updateCorpusNewOccurTimes(@Param("corpusCn") String corpusCn, @Param("occursTimes") Integer occursTimes);

    void saveRecordBatch(List<Map<String, String>> map);

    void saveAnotherRecordBatch(List<Map<String, String>> result);
}
