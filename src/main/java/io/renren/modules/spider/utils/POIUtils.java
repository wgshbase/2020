package io.renren.modules.spider.utils;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.util.StringUtils;
import us.codecraft.webmagic.selector.Html;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author wgsh 采集企业信息到本地的 excel 文件中的工具类
 * @Date wgshb on 2019/7/19 15:22
 */
public class POIUtils {

	public static void main(String[] args) throws Exception {
		String str = "\n" +
				"<h1 id=\"title\">广州海运（集团）房地产开发有限公司</h1> <tbody><tr> <td width=\"20%\" class=\"tb\" rowspan=\"2\"> \n" +
				"                                                  法定代表人\n" +
				"                                        </td> <td width=\"30%\" rowspan=\"2\"> <div class=\"boss-td\"> <div class=\"clearfix\" style=\"min-height: 76px;padding-top: 8px;overflow: hidden;\"> <div class=\"pull-left bheadimgkuang\"> <span class=\"usericon boss color-11\" first-letter=\"董\"></span> </div> <div class=\"bpen\"> <a href=\"/pl_pd6584335d7ce6b3bba35d27775f7704.html\" class=\"bname\"><h2 class=\"seo font-20\">董惠超</h2></a> <a class=\"btn-touzi pull-left\" onclick=\"relatedList(1,'pd6584335d7ce6b3bba35d27775f7704','董惠超');zhugeTrack('关联企业按钮点击',{'按钮位置':'工商信息','关联目标':'董惠超'});\">他关联4家企业 &gt; </a> </div> </div> </div> </td> <td width=\"20%\" class=\"tb\"> 注册资本 </td> <td width=\"30%\">  4447.5万元人民币  </td> </tr> <tr> <td width=\"20%\" class=\"tb\"> 实缴资本 </td> <td width=\"30%\"> 4447.5万元人民币 </td> </tr> <tr> <td class=\"tb\">经营状态</td> <td class=\"\">\n" +
				"                 在营（开业）企业             </td> <td class=\"tb\">成立日期</td> <td class=\"\">\n" +
				"                1993-02-10\n" +
				"            </td> </tr> <tr> <td class=\"tb\">统一社会信用代码</td> <td class=\"\">\n" +
				"                91440101190485984L\n" +
				"            </td> <td class=\"tb\">纳税人识别号</td> <td class=\"\">\n" +
				"                91440101190485984L\n" +
				"            </td> </tr> <tr> <td class=\"tb\">注册号</td> <td class=\"\">\n" +
				"                440101000209390\n" +
				"            </td> <td class=\"tb\" width=\"15%\">组织机构代码</td> <td class=\"\">\n" +
				"                19048598-4\n" +
				"            </td> </tr> <tr> <td class=\"tb\">企业类型</td> <td class=\"\">\n" +
				"                有限责任公司(法人独资)\n" +
				"            </td> <td class=\"tb\">所属行业</td> <td class=\"\">\n" +
				"                房地产业\n" +
				"            </td> </tr> <tr> <td class=\"tb\">核准日期</td> <td class=\"\" style=\"max-width:301px;\">\n" +
				"              2019-06-10\n" +
				"          </td> <td class=\"tb\">登记机关</td> <td class=\"\">\n" +
				"              广州市市场监督管理局\n" +
				"          </td> </tr> <tr> <td class=\"tb\">所属地区</td> <td class=\"\" style=\"max-width:301px;\">\n" +
				"                广东省\n" +
				"            </td> <td class=\"tb\">英文名</td> <td class=\"\">\n" +
				"                -\n" +
				"            </td> </tr> <tr> <td class=\"tb\">\n" +
				"                曾用名\n" +
				"            </td> <td class=\"\"> <span>广州海运（集团）房地产开发公司&nbsp;&nbsp;</span> </td> <td class=\"tb\">\n" +
				"                参保人数\n" +
				"            </td> <td class=\"\">\n" +
				"                17\n" +
				"            </td> </tr> <tr> <td class=\"tb\">\n" +
				"                人员规模\n" +
				"            </td> <td class=\"\">\n" +
				"                -\n" +
				"            </td> <td class=\"tb\">\n" +
				"                营业期限\n" +
				"            </td> <td class=\"\">\n" +
				"                1993-02-10 至 无固定期限\n" +
				"            </td> </tr> <tr> <td class=\"tb\">企业地址</td> <td class=\"\" colspan=\"3\">\n" +
				"                 广州市海珠区新港东路1000号1705房\n" +
				"                <a onclick=\"showMapModal('广州市海珠区新港东路1000号1705房','广州市');zhugeTrack('企业主页按钮点击',{'按钮名称':'查看地图'});\" class=\"m-l c_a\"> 查看地图</a> <a onclick=\"zhugeTrack('企业主页按钮点击',{'按钮名称':'附近企业'});\" href=\"/map?keyNo=f9c08e314014c3783bf2dddf3d89a34f\" class=\"m-l c_a\"> 附近企业</a> </td> </tr> <tr> <td class=\"tb\">经营范围</td> <td class=\"\" colspan=\"3\">\n" +
				"                 房地产咨询服务;工程技术咨询服务;室内装饰、装修;商品批发贸易(许可审批类商品除外);房地产开发经营;商品零售贸易(许可审批类商品除外);房屋建筑工程设计服务;商品交易所(金融产品交易所除外)             </td> </tr> </tbody>\n" +
				"<h1 id=\"title\">兴龙舟海运集团有限公司</h1> <tbody><tr> <td width=\"20%\" class=\"tb\" rowspan=\"2\"> \n" +
				"                                                  法定代表人\n" +
				"                                        </td> <td width=\"30%\" rowspan=\"2\"> <div class=\"boss-td\"> <div class=\"clearfix\" style=\"min-height: 76px;padding-top: 8px;overflow: hidden;\"> <div class=\"pull-left bheadimgkuang\"> <span class=\"usericon boss color-1\" first-letter=\"陈\"></span> </div> <div class=\"bpen\"> <a href=\"/pl_p8da44e57bf043e9b0b73728f7303135.html\" class=\"bname\"><h2 class=\"seo font-20\">陈爱英</h2></a> <a class=\"btn-touzi pull-left\" onclick=\"relatedList(1,'p8da44e57bf043e9b0b73728f7303135','陈爱英');zhugeTrack('关联企业按钮点击',{'按钮位置':'工商信息','关联目标':'陈爱英'});\">他关联8家企业 &gt; </a> </div> </div> </div> </td> <td width=\"20%\" class=\"tb\"> 注册资本 </td> <td width=\"30%\">  5080万元人民币  </td> </tr> <tr> <td width=\"20%\" class=\"tb\"> 实缴资本 </td> <td width=\"30%\"> 10080万元人民币 </td> </tr> <tr> <td class=\"tb\">经营状态</td> <td class=\"\">\n" +
				"                 存续             </td> <td class=\"tb\">成立日期</td> <td class=\"\">\n" +
				"                2002-07-03\n" +
				"            </td> </tr> <tr> <td class=\"tb\">统一社会信用代码</td> <td class=\"\">\n" +
				"                91331021742906792U\n" +
				"            </td> <td class=\"tb\">纳税人识别号</td> <td class=\"\">\n" +
				"                91331021742906792U\n" +
				"            </td> </tr> <tr> <td class=\"tb\">注册号</td> <td class=\"\">\n" +
				"                331021000015859\n" +
				"            </td> <td class=\"tb\" width=\"15%\">组织机构代码</td> <td class=\"\">\n" +
				"                74290679-2\n" +
				"            </td> </tr> <tr> <td class=\"tb\">企业类型</td> <td class=\"\">\n" +
				"                有限责任公司(自然人投资或控股)\n" +
				"            </td> <td class=\"tb\">所属行业</td> <td class=\"\">\n" +
				"                交通运输、仓储和邮政业\n" +
				"            </td> </tr> <tr> <td class=\"tb\">核准日期</td> <td class=\"\" style=\"max-width:301px;\">\n" +
				"              2019-09-29\n" +
				"          </td> <td class=\"tb\">登记机关</td> <td class=\"\">\n" +
				"              玉环市市场监督管理局\n" +
				"          </td> </tr> <tr> <td class=\"tb\">所属地区</td> <td class=\"\" style=\"max-width:301px;\">\n" +
				"                浙江省\n" +
				"            </td> <td class=\"tb\">英文名</td> <td class=\"\">\n" +
				"                Xinglongzhou MARINE SHIPPING Group Co., Ltd.\n" +
				"            </td> </tr> <tr> <td class=\"tb\">\n" +
				"                曾用名\n" +
				"            </td> <td class=\"\"> <span>台州市兴龙舟海运有限公司&nbsp;&nbsp;</span> </td> <td class=\"tb\">\n" +
				"                参保人数\n" +
				"            </td> <td class=\"\">\n" +
				"                4\n" +
				"            </td> </tr> <tr> <td class=\"tb\">\n" +
				"                人员规模\n" +
				"            </td> <td class=\"\">\n" +
				"                -\n" +
				"            </td> <td class=\"tb\">\n" +
				"                营业期限\n" +
				"            </td> <td class=\"\">\n" +
				"                2002-07-03 至 2022-07-02\n" +
				"            </td> </tr> <tr> <td class=\"tb\">企业地址</td> <td class=\"\" colspan=\"3\">\n" +
				"                 浙江省玉环市大麦屿街道龙山商住楼5单元502室\n" +
				"                <a onclick=\"showMapModal('浙江省玉环市大麦屿街道龙山商住楼5单元502室','台州市');zhugeTrack('企业主页按钮点击',{'按钮名称':'查看地图'});\" class=\"m-l c_a\"> 查看地图</a> <a onclick=\"zhugeTrack('企业主页按钮点击',{'按钮名称':'附近企业'});\" href=\"/map?keyNo=26d299d0ae32d0008ed3bbf810becb1f\" class=\"m-l c_a\"> 附近企业</a> </td> </tr> <tr> <td class=\"tb\">经营范围</td> <td class=\"\" colspan=\"3\">\n" +
				"                 国内沿海及长江中下游各港间普通货船、成品油船运输,浙江省内沿海原油运输,港口经营。(依法须经批准的项目,经相关部门批准后方可开展经营活动)             </td> </tr> </tbody>\n" +
				"<h1 id=\"title\">洋浦中良海运有限公司</h1> <tbody><tr> <td width=\"20%\" class=\"tb\" rowspan=\"2\"> \n" +
				"                                                  法定代表人\n" +
				"                                        </td> <td width=\"30%\" rowspan=\"2\"> <div class=\"boss-td\"> <div class=\"clearfix\" style=\"min-height: 76px;padding-top: 8px;overflow: hidden;\"> <div class=\"pull-left bheadimgkuang\"> <span class=\"usericon boss color-12\" first-letter=\"朱\"></span> </div> <div class=\"bpen\"> <a href=\"/pl_p733fda9ff06c6b2c688d5dff2919b3b.html\" class=\"bname\"><h2 class=\"seo font-20\">朱建强</h2></a> <a class=\"btn-touzi pull-left\" onclick=\"relatedList(1,'p733fda9ff06c6b2c688d5dff2919b3b','朱建强');zhugeTrack('关联企业按钮点击',{'按钮位置':'工商信息','关联目标':'朱建强'});\">他关联16家企业 &gt; </a> </div> </div> </div> </td> <td width=\"20%\" class=\"tb\"> 注册资本 </td> <td width=\"30%\">  2800万元人民币  </td> </tr> <tr> <td width=\"20%\" class=\"tb\"> 实缴资本 </td> <td width=\"30%\"> 800万元人民币 </td> </tr> <tr> <td class=\"tb\">经营状态</td> <td class=\"\">\n" +
				"                 存续（在营、开业、在册）             </td> <td class=\"tb\">成立日期</td> <td class=\"\">\n" +
				"                2004-03-05\n" +
				"            </td> </tr> <tr> <td class=\"tb\">统一社会信用代码</td> <td class=\"\">\n" +
				"                914603007543869666\n" +
				"            </td> <td class=\"tb\">纳税人识别号</td> <td class=\"\">\n" +
				"                914603007543869666\n" +
				"            </td> </tr> <tr> <td class=\"tb\">注册号</td> <td class=\"\">\n" +
				"                460300000010411\n" +
				"            </td> <td class=\"tb\" width=\"15%\">组织机构代码</td> <td class=\"\">\n" +
				"                75438696-6\n" +
				"            </td> </tr> <tr> <td class=\"tb\">企业类型</td> <td class=\"\">\n" +
				"                其他有限责任公司\n" +
				"            </td> <td class=\"tb\">所属行业</td> <td class=\"\">\n" +
				"                交通运输、仓储和邮政业\n" +
				"            </td> </tr> <tr> <td class=\"tb\">核准日期</td> <td class=\"\" style=\"max-width:301px;\">\n" +
				"              2018-01-30\n" +
				"          </td> <td class=\"tb\">登记机关</td> <td class=\"\">\n" +
				"              洋浦经济开发区市场监督管理局\n" +
				"          </td> </tr> <tr> <td class=\"tb\">所属地区</td> <td class=\"\" style=\"max-width:301px;\">\n" +
				"                海南省\n" +
				"            </td> <td class=\"tb\">英文名</td> <td class=\"\">\n" +
				"                Yangpu Sinowell Shipping Co., Ltd.\n" +
				"            </td> </tr> <tr> <td class=\"tb\">\n" +
				"                曾用名\n" +
				"            </td> <td class=\"\">\n" +
				"                                    -\n" +
				"                            </td> <td class=\"tb\">\n" +
				"                参保人数\n" +
				"            </td> <td class=\"\">\n" +
				"                282\n" +
				"            </td> </tr> <tr> <td class=\"tb\">\n" +
				"                人员规模\n" +
				"            </td> <td class=\"\">\n" +
				"                100-499人\n" +
				"            </td> <td class=\"tb\">\n" +
				"                营业期限\n" +
				"            </td> <td class=\"\">\n" +
				"                2004-03-05 至 无固定期限\n" +
				"            </td> </tr> <tr> <td class=\"tb\">企业地址</td> <td class=\"\" colspan=\"3\">\n" +
				"                 洋浦经济开发区金洋路浦馨苑18号十二层1204房\n" +
				"                <a onclick=\"showMapModal('洋浦经济开发区金洋路浦馨苑18号十二层1204房','三沙市');zhugeTrack('企业主页按钮点击',{'按钮名称':'查看地图'});\" class=\"m-l c_a\"> 查看地图</a> <a onclick=\"zhugeTrack('企业主页按钮点击',{'按钮名称':'附近企业'});\" href=\"/map?keyNo=48114ed812de3d501aee25c1c6ab1768\" class=\"m-l c_a\"> 附近企业</a> </td> </tr> <tr> <td class=\"tb\">经营范围</td> <td class=\"\" colspan=\"3\">\n" +
				"                 国内沿海普通货船运输。             </td> </tr> </tbody>\n" +
				"<h1 id=\"title\">中远海运化工物流有限公司</h1> <tbody><tr> <td width=\"20%\" class=\"tb\" rowspan=\"2\"> \n" +
				"                                                  法定代表人\n" +
				"                                        </td> <td width=\"30%\" rowspan=\"2\"> <div class=\"boss-td\"> <div class=\"clearfix\" style=\"min-height: 76px;padding-top: 8px;overflow: hidden;\"> <div class=\"pull-left bheadimgkuang\"> <span class=\"usericon boss color-12\" first-letter=\"符\"></span> </div> <div class=\"bpen\"> <a href=\"/pl_p8071953792d60da92b02e67fc9d2eac.html\" class=\"bname\"><h2 class=\"seo font-20\">符鹏</h2></a> <a class=\"btn-touzi pull-left\" onclick=\"relatedList(1,'p8071953792d60da92b02e67fc9d2eac','符鹏');zhugeTrack('关联企业按钮点击',{'按钮位置':'工商信息','关联目标':'符鹏'});\">他关联12家企业 &gt; </a> </div> </div> </div> </td> <td width=\"20%\" class=\"tb\"> 注册资本 </td> <td width=\"30%\">  37138.8万元人民币  </td> </tr> <tr> <td width=\"20%\" class=\"tb\"> 实缴资本 </td> <td width=\"30%\"> 5949.51万元人民币 </td> </tr> <tr> <td class=\"tb\">经营状态</td> <td class=\"\">\n" +
				"                 存续（在营、开业、在册）             </td> <td class=\"tb\">成立日期</td> <td class=\"\">\n" +
				"                2004-08-12\n" +
				"            </td> </tr> <tr> <td class=\"tb\">统一社会信用代码</td> <td class=\"\">\n" +
				"                91310000717851830U\n" +
				"            </td> <td class=\"tb\">纳税人识别号</td> <td class=\"\">\n" +
				"                91310000717851830U\n" +
				"            </td> </tr> <tr> <td class=\"tb\">注册号</td> <td class=\"\">\n" +
				"                310000400393941\n" +
				"            </td> <td class=\"tb\" width=\"15%\">组织机构代码</td> <td class=\"\">\n" +
				"                71785183-0\n" +
				"            </td> </tr> <tr> <td class=\"tb\">企业类型</td> <td class=\"\">\n" +
				"                有限责任公司(台港澳与境内合资)\n" +
				"            </td> <td class=\"tb\">所属行业</td> <td class=\"\">\n" +
				"                交通运输、仓储和邮政业\n" +
				"            </td> </tr> <tr> <td class=\"tb\">核准日期</td> <td class=\"\" style=\"max-width:301px;\">\n" +
				"              2004-08-12\n" +
				"          </td> <td class=\"tb\">登记机关</td> <td class=\"\">\n" +
				"              上海市市场监督管理局\n" +
				"          </td> </tr> <tr> <td class=\"tb\">所属地区</td> <td class=\"\" style=\"max-width:301px;\">\n" +
				"                上海市\n" +
				"            </td> <td class=\"tb\">英文名</td> <td class=\"\">\n" +
				"                COSCO CHEMICAL LOGISTICS CO.,LTD\n" +
				"            </td> </tr> <tr> <td class=\"tb\">\n" +
				"                曾用名\n" +
				"            </td> <td class=\"\"> <span>中远化工物流有限公司&nbsp;&nbsp;</span> <span>上海中远化工物流有限公司&nbsp;&nbsp;</span> </td> <td class=\"tb\">\n" +
				"                参保人数\n" +
				"            </td> <td class=\"\">\n" +
				"                310\n" +
				"            </td> </tr> <tr> <td class=\"tb\">\n" +
				"                人员规模\n" +
				"            </td> <td class=\"\">\n" +
				"                100-499人\n" +
				"            </td> <td class=\"tb\">\n" +
				"                营业期限\n" +
				"            </td> <td class=\"\">\n" +
				"                2004-08-12 至 2024-08-11\n" +
				"            </td> </tr> <tr> <td class=\"tb\">企业地址</td> <td class=\"\" colspan=\"3\">\n" +
				"                 上海市奉贤区目华北路799号\n" +
				"                <a onclick=\"showMapModal('上海市奉贤区目华北路799号','上海市');zhugeTrack('企业主页按钮点击',{'按钮名称':'查看地图'});\" class=\"m-l c_a\"> 查看地图</a> <a onclick=\"zhugeTrack('企业主页按钮点击',{'按钮名称':'附近企业'});\" href=\"/map?keyNo=54be78188e29968b284272f807b7a734\" class=\"m-l c_a\"> 附近企业</a> </td> </tr> <tr> <td class=\"tb\">经营范围</td> <td class=\"\" colspan=\"3\">\n" +
				"                 进出口业务及相关服务,包括自营或代理货物的进口、出口业务,接受委托为出口加工企业提供代理进出口业务;承办海运进出口货物的国际运输代理业务,包括:揽货、订舱、仓储、中转、集装箱拼装拆箱、结算运杂费、报关、报验、相关短途运输服务及运输咨询业务;道路危险货物运输(国际集装箱),道路货物运输(普通货物)、道路危险货物运输、集装箱(罐)清洗和修理,普通货物、危险化学品(危险化学品限许可证范围、民用爆炸物除外)的仓储物流业务,国内货运代理,仓储管理服务,提供第三方物流服务,物流信息咨询,化工物流科技领域内的技术开发、技术服务、技术转让、技术咨询。【依法须经批准的项目,经相关部门批准后方可开展经营活动】             </td> </tr> </tbody>\n" +
				"<h1 id=\"title\">北京中远海运物流有限公司</h1> <tbody><tr> <td width=\"20%\" class=\"tb\" rowspan=\"2\"> \n" +
				"                                                  法定代表人\n" +
				"                                        </td> <td width=\"30%\" rowspan=\"2\"> <div class=\"boss-td\"> <div class=\"clearfix\" style=\"min-height: 76px;padding-top: 8px;overflow: hidden;\"> <div class=\"pull-left bheadimgkuang\"> <span class=\"usericon boss color-10\" first-letter=\"符\"></span> </div> <div class=\"bpen\"> <a href=\"/pl_p8071953792d60da92b02e67fc9d2eac.html\" class=\"bname\"><h2 class=\"seo font-20\">符鹏</h2></a> <a class=\"btn-touzi pull-left\" onclick=\"relatedList(1,'p8071953792d60da92b02e67fc9d2eac','符鹏');zhugeTrack('关联企业按钮点击',{'按钮位置':'工商信息','关联目标':'符鹏'});\">他关联12家企业 &gt; </a> </div> </div> </div> </td> <td width=\"20%\" class=\"tb\"> 注册资本 </td> <td width=\"30%\">  27717.36万元人民币  </td> </tr> <tr> <td width=\"20%\" class=\"tb\"> 实缴资本 </td> <td width=\"30%\"> 5247.36万元人民币 </td> </tr> <tr> <td class=\"tb\">经营状态</td> <td class=\"\">\n" +
				"                 开业             </td> <td class=\"tb\">成立日期</td> <td class=\"\">\n" +
				"                1992-09-29\n" +
				"            </td> </tr> <tr> <td class=\"tb\">统一社会信用代码</td> <td class=\"\">\n" +
				"                911101051011186066\n" +
				"            </td> <td class=\"tb\">纳税人识别号</td> <td class=\"\">\n" +
				"                911101051011186066\n" +
				"            </td> </tr> <tr> <td class=\"tb\">注册号</td> <td class=\"\">\n" +
				"                110000005006445\n" +
				"            </td> <td class=\"tb\" width=\"15%\">组织机构代码</td> <td class=\"\">\n" +
				"                10111860-6\n" +
				"            </td> </tr> <tr> <td class=\"tb\">企业类型</td> <td class=\"\">\n" +
				"                有限责任公司(法人独资)\n" +
				"            </td> <td class=\"tb\">所属行业</td> <td class=\"\">\n" +
				"                交通运输、仓储和邮政业\n" +
				"            </td> </tr> <tr> <td class=\"tb\">核准日期</td> <td class=\"\" style=\"max-width:301px;\">\n" +
				"              2019-01-21\n" +
				"          </td> <td class=\"tb\">登记机关</td> <td class=\"\">\n" +
				"              北京市工商行政管理局朝阳分局\n" +
				"          </td> </tr> <tr> <td class=\"tb\">所属地区</td> <td class=\"\" style=\"max-width:301px;\">\n" +
				"                北京市\n" +
				"            </td> <td class=\"tb\">英文名</td> <td class=\"\">\n" +
				"                COSCO LOGISTICS (BEIJING) CO., LTD\n" +
				"            </td> </tr> <tr> <td class=\"tb\">\n" +
				"                曾用名\n" +
				"            </td> <td class=\"\"> <span>北京中远物流有限公司&nbsp;&nbsp;</span> <span>北京中远物流公司&nbsp;&nbsp;</span> <span>中国汽车运输北京晋煤外运公司&nbsp;&nbsp;</span> </td> <td class=\"tb\">\n" +
				"                参保人数\n" +
				"            </td> <td class=\"\">\n" +
				"                76\n" +
				"            </td> </tr> <tr> <td class=\"tb\">\n" +
				"                人员规模\n" +
				"            </td> <td class=\"\">\n" +
				"                -\n" +
				"            </td> <td class=\"tb\">\n" +
				"                营业期限\n" +
				"            </td> <td class=\"\">\n" +
				"                2003-12-22 至 2023-12-21\n" +
				"            </td> </tr> <tr> <td class=\"tb\">企业地址</td> <td class=\"\" colspan=\"3\">\n" +
				"                 北京市朝阳区麦子店西路3号9层1005室\n" +
				"                <a onclick=\"showMapModal('北京市朝阳区麦子店西路3号9层1005室','北京市');zhugeTrack('企业主页按钮点击',{'按钮名称':'查看地图'});\" class=\"m-l c_a\"> 查看地图</a> <a onclick=\"zhugeTrack('企业主页按钮点击',{'按钮名称':'附近企业'});\" href=\"/map?keyNo=3ef93aee96e1ac2e691e05db9788b7a0\" class=\"m-l c_a\"> 附近企业</a> </td> </tr> <tr> <td class=\"tb\">经营范围</td> <td class=\"\" colspan=\"3\">\n" +
				"                 普通货运(道路运输经营许可证有效期至2022年09月5日);无船承运业务;航空货运代理;货运代理;仓储、包装、配送服务;承办海运、陆运进出口货物、国际展品及私人物品的国际运输代理业务,包括揽货、订舱、仓储、中转、集装箱拼装拆箱、结箱运杂费、报关、报验、保险、相关的短途运输服务及咨询业务;物流软件开发;信息咨询(中介除外);承办展览展示、会议服务;销售煤炭(不得在北京地区开展实物煤的交易、储运活动)、矿产品(经营煤炭的不得在北京地区开展实物煤交易及储运)。(企业依法自主选择经营项目,开展经营活动;依法须经批准的项目,经相关部门批准后依批准的内容开展经营活动;不得从事本市产业政策禁止和限制类项目的经营活动。)             </td> </tr> </tbody>\n" +
				"<h1 id=\"title\">潍坊鑫航海运有限责任公司</h1> <tbody><tr> <td width=\"20%\" class=\"tb\" rowspan=\"2\"> \n" +
				"                                                  法定代表人\n" +
				"                                        </td> <td width=\"30%\" rowspan=\"2\"> <div class=\"boss-td\"> <div class=\"clearfix\" style=\"min-height: 76px;padding-top: 8px;overflow: hidden;\"> <div class=\"pull-left bheadimgkuang\"> <span class=\"usericon boss color-9\" first-letter=\"张\"></span> </div> <div class=\"bpen\"> <a href=\"/pl_pa60b576491aa2ab8d86b53014006082.html\" class=\"bname\"><h2 class=\"seo font-20\">张寿平</h2></a> <a class=\"btn-touzi pull-left\" onclick=\"relatedList(1,'pa60b576491aa2ab8d86b53014006082','张寿平');zhugeTrack('关联企业按钮点击',{'按钮位置':'工商信息','关联目标':'张寿平'});\">他关联6家企业 &gt; </a> </div> </div> </div> </td> <td width=\"20%\" class=\"tb\"> 注册资本 </td> <td width=\"30%\">  1999万元人民币  </td> </tr> <tr> <td width=\"20%\" class=\"tb\"> 实缴资本 </td> <td width=\"30%\"> 1689万元人民币 </td> </tr> <tr> <td class=\"tb\">经营状态</td> <td class=\"\">\n" +
				"                 在营（开业）企业             </td> <td class=\"tb\">成立日期</td> <td class=\"\">\n" +
				"                2004-08-19\n" +
				"            </td> </tr> <tr> <td class=\"tb\">统一社会信用代码</td> <td class=\"\">\n" +
				"                91370700767762916M\n" +
				"            </td> <td class=\"tb\">纳税人识别号</td> <td class=\"\">\n" +
				"                91370700767762916M\n" +
				"            </td> </tr> <tr> <td class=\"tb\">注册号</td> <td class=\"\">\n" +
				"                370700228053857\n" +
				"            </td> <td class=\"tb\" width=\"15%\">组织机构代码</td> <td class=\"\">\n" +
				"                76776291-6\n" +
				"            </td> </tr> <tr> <td class=\"tb\">企业类型</td> <td class=\"\">\n" +
				"                有限责任公司\n" +
				"            </td> <td class=\"tb\">所属行业</td> <td class=\"\">\n" +
				"                交通运输、仓储和邮政业\n" +
				"            </td> </tr> <tr> <td class=\"tb\">核准日期</td> <td class=\"\" style=\"max-width:301px;\">\n" +
				"              2016-04-27\n" +
				"          </td> <td class=\"tb\">登记机关</td> <td class=\"\">\n" +
				"              潍坊滨海经济技术开发区市场监督管理局\n" +
				"          </td> </tr> <tr> <td class=\"tb\">所属地区</td> <td class=\"\" style=\"max-width:301px;\">\n" +
				"                山东省\n" +
				"            </td> <td class=\"tb\">英文名</td> <td class=\"\">\n" +
				"                Weifang Xinhang MARINE SHIPPING Co., Ltd.\n" +
				"            </td> </tr> <tr> <td class=\"tb\">\n" +
				"                曾用名\n" +
				"            </td> <td class=\"\">\n" +
				"                                    -\n" +
				"                            </td> <td class=\"tb\">\n" +
				"                参保人数\n" +
				"            </td> <td class=\"\">\n" +
				"                -\n" +
				"            </td> </tr> <tr> <td class=\"tb\">\n" +
				"                人员规模\n" +
				"            </td> <td class=\"\">\n" +
				"                -\n" +
				"            </td> <td class=\"tb\">\n" +
				"                营业期限\n" +
				"            </td> <td class=\"\">\n" +
				"                2004-08-19 至 无固定期限\n" +
				"            </td> </tr> <tr> <td class=\"tb\">企业地址</td> <td class=\"\" colspan=\"3\">\n" +
				"                 潍坊滨海经济开发区周疃村东100米\n" +
				"                <a onclick=\"showMapModal('潍坊滨海经济开发区周疃村东100米','潍坊市');zhugeTrack('企业主页按钮点击',{'按钮名称':'查看地图'});\" class=\"m-l c_a\"> 查看地图</a> <a onclick=\"zhugeTrack('企业主页按钮点击',{'按钮名称':'附近企业'});\" href=\"/map?keyNo=3a55a0d2755f1b21c97d49b3f4b4defe\" class=\"m-l c_a\"> 附近企业</a> </td> </tr> <tr> <td class=\"tb\">经营范围</td> <td class=\"\" colspan=\"3\">\n" +
				"                 国内沿海及长江中下游普通货船运输 (水路运输许可证有效期以许可证为准) ;船舶机务管理,船舶海务管理,船舶检修、保养,船舶买卖、租赁、营运及资产管理(水路运输服务许可证有效期以许可证为准)。             </td> </tr> </tbody>\n" +
				"<h1 id=\"title\">江苏远东海运有限公司</h1> <tbody><tr> <td width=\"20%\" class=\"tb\" rowspan=\"2\"> \n" +
				"                                                  法定代表人\n" +
				"                                        </td> <td width=\"30%\" rowspan=\"2\"> <div class=\"boss-td\"> <div class=\"clearfix\" style=\"min-height: 76px;padding-top: 8px;overflow: hidden;\"> <div class=\"pull-left bheadimgkuang\"> <span class=\"usericon boss color-14\" first-letter=\"符\"></span> </div> <div class=\"bpen\"> <a href=\"/pl_p28bcdcacd2b53648d2b82d4033021bd.html\" class=\"bname\"><h2 class=\"seo font-20\">符道仁</h2></a> <a class=\"btn-touzi pull-left\" onclick=\"relatedList(1,'p28bcdcacd2b53648d2b82d4033021bd','符道仁');zhugeTrack('关联企业按钮点击',{'按钮位置':'工商信息','关联目标':'符道仁'});\">他关联9家企业 &gt; </a> </div> </div> </div> </td> <td width=\"20%\" class=\"tb\"> 注册资本 </td> <td width=\"30%\">  10000万元人民币  </td> </tr> <tr> <td width=\"20%\" class=\"tb\"> 实缴资本 </td> <td width=\"30%\"> 10000万元人民币 </td> </tr> <tr> <td class=\"tb\">经营状态</td> <td class=\"\">\n" +
				"                 存续（在营、开业、在册）             </td> <td class=\"tb\">成立日期</td> <td class=\"\">\n" +
				"                2002-02-08\n" +
				"            </td> </tr> <tr> <td class=\"tb\">统一社会信用代码</td> <td class=\"\">\n" +
				"                913200007322511122\n" +
				"            </td> <td class=\"tb\">纳税人识别号</td> <td class=\"\">\n" +
				"                913200007322511122\n" +
				"            </td> </tr> <tr> <td class=\"tb\">注册号</td> <td class=\"\">\n" +
				"                320000000017155\n" +
				"            </td> <td class=\"tb\" width=\"15%\">组织机构代码</td> <td class=\"\">\n" +
				"                73225111-2\n" +
				"            </td> </tr> <tr> <td class=\"tb\">企业类型</td> <td class=\"\">\n" +
				"                有限责任公司\n" +
				"            </td> <td class=\"tb\">所属行业</td> <td class=\"\">\n" +
				"                交通运输、仓储和邮政业\n" +
				"            </td> </tr> <tr> <td class=\"tb\">核准日期</td> <td class=\"\" style=\"max-width:301px;\">\n" +
				"              2019-07-11\n" +
				"          </td> <td class=\"tb\">登记机关</td> <td class=\"\">\n" +
				"              江苏省市场监督管理局\n" +
				"          </td> </tr> <tr> <td class=\"tb\">所属地区</td> <td class=\"\" style=\"max-width:301px;\">\n" +
				"                江苏省\n" +
				"            </td> <td class=\"tb\">英文名</td> <td class=\"\">\n" +
				"                Jiangsu Far East Shipping Co., Ltd.\n" +
				"            </td> </tr> <tr> <td class=\"tb\">\n" +
				"                曾用名\n" +
				"            </td> <td class=\"\">\n" +
				"                                    -\n" +
				"                            </td> <td class=\"tb\">\n" +
				"                参保人数\n" +
				"            </td> <td class=\"\">\n" +
				"                34\n" +
				"            </td> </tr> <tr> <td class=\"tb\">\n" +
				"                人员规模\n" +
				"            </td> <td class=\"\">\n" +
				"                -\n" +
				"            </td> <td class=\"tb\">\n" +
				"                营业期限\n" +
				"            </td> <td class=\"\">\n" +
				"                2002-02-08 至 无固定期限\n" +
				"            </td> </tr> <tr> <td class=\"tb\">企业地址</td> <td class=\"\" colspan=\"3\">\n" +
				"                 南京市汉中路120号青华大厦东楼15层\n" +
				"                <a onclick=\"showMapModal('南京市汉中路120号青华大厦东楼15层','南京市');zhugeTrack('企业主页按钮点击',{'按钮名称':'查看地图'});\" class=\"m-l c_a\"> 查看地图</a> <a onclick=\"zhugeTrack('企业主页按钮点击',{'按钮名称':'附近企业'});\" href=\"/map?keyNo=5870193f5838ae4c7ec647396bd450ea\" class=\"m-l c_a\"> 附近企业</a> </td> </tr> <tr> <td class=\"tb\">经营范围</td> <td class=\"\" colspan=\"3\">\n" +
				"                 国际船舶普通货物运输,国际船舶管理(按经营资格登记证所列范围经营),向境外派遣各类劳务人员(含海员);国内沿海散货船、其它货船的海务、机务管理和安全防污染管理。(依法须经批准的项目,经相关部门批准后方可开展经营活动)             </td> </tr> </tbody>";
		File file = new File("E:/5.xls");
		FileOutputStream fos = new FileOutputStream(file);
		List<Map<String, String>> records = str2maplist(str);

		createFixationSheet(fos, records);
	}

	private static List<Map<String, String>> str2maplist(String str) {
		List<Map<String, String>> result = new ArrayList<>();

		if(str.contains("</tbody>")) {
			String[] trs = str.split("</tbody>");
			for(int i = 0; i < trs.length; i++) {
				if(trs[i].contains("</tr>")) {
					Map<String, String> map = new HashMap<>();
					String[] tds = trs[i].split("class=\"tb\"");
					for(int j = 0; j < tds.length; j++) {
						Html temp = new Html(tds[j]);
						if(temp.toString().contains("title")) {
							map.put("qymc", temp.xpath("//h1[@id='title']/text()").toString());
						}
						if(tds[j].contains("法定代表人")) {
							map.put("fddbr", temp.xpath("//h2[@class='seo font-20']/text()").toString());
						} else if(tds[j].contains("注册资本")) {
							map.put("zczb", tagTrim(tds[j]).replace("注册资本", "").trim());
						}  else if(tds[j].contains("统一社会信用代码")) {
							map.put("tyshxydm", tagTrim(tds[j]).replace("统一社会信用代码", "").trim());
						} else if(tds[j].contains("企业类型")) {
							map.put("qylx",tagTrim(tds[j]).replace("企业类型", "").trim());
						} else if(tds[j].contains("人员规模")) {
							map.put("rygm", tagTrim(tds[j]).replace("人员规模", "").trim());
						} else if(tds[j].contains("成立日期")) {
							map.put("clrq", tagTrim(tds[j]).replace("成立日期", "").trim());
						} else if(tds[j].contains("核准日期")) {
							map.put("hzrq", tagTrim(tds[j]).replace("核准日期", "").trim());
						} else if(tds[j].contains("经营状态")) {
							map.put("jyzt", tagTrim(tds[j]).replace("经营状态", "").trim());
						} else if(tds[j].contains("所属行业")) {
							map.put("sshy", tagTrim(tds[j]).replace("所属行业", "").trim());
						} else if(tds[j].contains("营业期限")) {
							map.put("yyqx", tagTrim(tds[j]).replace("营业期限", "").trim());
						} else if(tds[j].contains("企业地址")) {
							map.put("qydz", tagTrim(tds[j]).replace("企业地址", "").trim());
						} else if(tds[j].contains("经营范围")) {
							map.put("jyfw", tagTrim(tds[j]).replace("经营范围", "").trim());
						} else if(tds[j].contains("所属地区")) {
							map.put("ssdq", tagTrim(tds[j]).replace("所属地区", "").trim());
						}
					}
					result.add(map);

				}
			}
		}

		return result;
	}

	public static String tagTrim(String str) {
		return str.replaceAll("<.*?>", "").replaceAll(">", "").replaceAll("<.*?\"[\\s\\S]*\"","").replace("<td" ,"");
	}

	public static void createFixationSheet(OutputStream os,
										   List<Map<String, String>> records) throws Exception {
		// 创建工作薄
		HSSFWorkbook wb = new HSSFWorkbook();
		// 在工作薄上建一张工作表
		HSSFSheet sheet = wb.createSheet();

		HSSFCellStyle topstyle = wb.createCellStyle();
		HSSFFont topfont = wb.createFont();
		topfont.setFontName("宋体");
		topfont.setFontHeightInPoints((short) 15);// 字体大小
		topstyle.setFont(topfont);
		topstyle.setAlignment(HorizontalAlignment.CENTER);
		topstyle.setLocked(true);

		topstyle.setBorderBottom(BorderStyle.THIN); //下边框
		topstyle.setBorderLeft(BorderStyle.THIN);//左边框
		topstyle.setBorderTop(BorderStyle.THIN);//上边框
		topstyle.setBorderRight(BorderStyle.THIN);//右边框

//		sheet.createFreezePane(0, 2,1,0);

		HSSFRow topColumn = sheet.createRow((short) 1);
		cteateCell(wb, topColumn, (short) 0, "序号", topstyle);
		cteateCell(wb, topColumn, (short) 1, "企业名称", topstyle);
		cteateCell(wb, topColumn, (short) 2, "法定代表人", topstyle);
		cteateCell(wb, topColumn, (short) 3, "所属地区", topstyle);
		cteateCell(wb, topColumn, (short) 4, "统一社会信用代码", topstyle);
		cteateCell(wb, topColumn, (short) 5, "注册资本", topstyle);
		cteateCell(wb, topColumn, (short) 6, "企业类型", topstyle);
		cteateCell(wb, topColumn, (short) 7, "人员规模", topstyle);
		cteateCell(wb, topColumn, (short) 8, "成立日期", topstyle);
		cteateCell(wb, topColumn, (short) 9, "核准日期", topstyle);
		cteateCell(wb, topColumn, (short) 10, "经营状态", topstyle);
		cteateCell(wb, topColumn, (short) 11, "所属行业", topstyle);
		cteateCell(wb, topColumn, (short) 12, "营业期限", topstyle);
		cteateCell(wb, topColumn, (short) 13, "企业地址", topstyle);
		cteateCell(wb, topColumn, (short) 14, "经营范围", topstyle);

		int i = 1;

		HSSFCellStyle cellstyle = wb.createCellStyle();
		cellstyle.setAlignment(HorizontalAlignment.CENTER);

		cellstyle.setBorderBottom(BorderStyle.THIN); //下边框
		cellstyle.setBorderLeft(BorderStyle.THIN);//左边框
		cellstyle.setBorderTop(BorderStyle.THIN);//上边框
		cellstyle.setBorderRight(BorderStyle.THIN);//右边框


		for(int j = 0; j < records.size(); j++) {
			HSSFRow rowi = sheet.createRow(++i);
			Map<String, String> record = records.get(j);
			cteateCell(wb, rowi, (short) 0, "" + (j + 1), cellstyle);
			if(!StringUtils.isEmpty(record.get("qymc"))) {
				cteateCell(wb, rowi, (short) 1, record.get("qymc"), cellstyle);
			}
			if(!StringUtils.isEmpty(record.get("fddbr"))) {
				cteateCell(wb, rowi, (short) 2, record.get("fddbr"), cellstyle);
			}
			if(!StringUtils.isEmpty(record.get("ssdq"))) {
				cteateCell(wb, rowi, (short) 3, record.get("ssdq"), cellstyle);
			}
			if(!StringUtils.isEmpty(record.get("tyshxydm"))) {
				cteateCell(wb, rowi, (short) 4, record.get("tyshxydm"), cellstyle);
			}
			if(!StringUtils.isEmpty(record.get("zczb"))) {
				cteateCell(wb, rowi, (short) 5, record.get("zczb"), cellstyle);
			}
			if(!StringUtils.isEmpty(record.get("qylx"))) {
				cteateCell(wb, rowi, (short) 6, record.get("qylx"), cellstyle);
			}
			if(!StringUtils.isEmpty(record.get("rygm"))) {
				cteateCell(wb, rowi, (short) 7, record.get("rygm"), cellstyle);
			}
			if(!StringUtils.isEmpty(record.get("clrq"))) {
				cteateCell(wb, rowi, (short) 8, record.get("clrq"), cellstyle);
			}
			if(!StringUtils.isEmpty(record.get("hzrq"))) {
				cteateCell(wb, rowi, (short) 9, record.get("hzrq"), cellstyle);
			}
			if(!StringUtils.isEmpty(record.get("jyzt"))) {
				cteateCell(wb, rowi, (short) 10, record.get("jyzt"), cellstyle);
			}
			if(!StringUtils.isEmpty(record.get("sshy"))) {
				cteateCell(wb, rowi, (short) 11, record.get("sshy"), cellstyle);
			}
			if(!StringUtils.isEmpty(record.get("yyqx"))) {
				cteateCell(wb, rowi, (short) 12, record.get("yyqx"), cellstyle);

			}
			if(!StringUtils.isEmpty(record.get("qydz"))) {
				cteateCell(wb, rowi, (short) 13, record.get("qydz"), cellstyle);

			}
			if(!StringUtils.isEmpty(record.get("jyfw"))) {
				cteateCell(wb, rowi, (short) 14, record.get("jyfw"), cellstyle);
			}



		}

		sheet.autoSizeColumn((short)0); //调整第一列宽度
		sheet.autoSizeColumn((short)1); //调整第二列宽度
		sheet.autoSizeColumn((short)2); //调整第三列宽度
		sheet.autoSizeColumn((short)3); //调整第四列宽度
		sheet.autoSizeColumn((short)4); //调整第四列宽度
		sheet.autoSizeColumn((short)5); //调整第四列宽度
		sheet.autoSizeColumn((short)6); //调整第四列宽度
		sheet.autoSizeColumn((short)7); //调整第一列宽度
		sheet.autoSizeColumn((short)8); //调整第二列宽度
		sheet.autoSizeColumn((short)9); //调整第三列宽度
		sheet.autoSizeColumn((short)10); //调整第四列宽度
		sheet.autoSizeColumn((short)11); //调整第四列宽度
		sheet.autoSizeColumn((short)12); //调整第四列宽度
		sheet.autoSizeColumn((short)13); //调整第四列宽度
		sheet.autoSizeColumn((short)14); //调整第四列宽度

		wb.write(os);
		os.flush();
		os.close();
		System.out.println("文件生成");

	}

	/*private static String getTableHeadVal(DlpCheckTask task, SystemUser user) throws ParseException {
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat fullSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat shortSdf = new SimpleDateFormat("yyyy-MM-dd");
		sb.append(task.getName())
				.append("_辅助检查分析报告(")
//				.append(user.getUsername())
//				.append(")/")
				.append(shortSdf.format(fullSdf.parse(task.getCreateTime())) + "/")
				.append(user.getUsername() + ")");

		return sb.toString();
	}*/

	private static String getCheckStatus(Integer checkStatus) {
		String result = "";
		switch (checkStatus) {
			case 0:
				result = "尚未检查";
				break;
			case 1:
				result = "正常";
				break;
			case 2:
				result = "预警";
				break;
				default:
					result = "未知";
		}
		return result;
	}

	private static void cteateCell(HSSFWorkbook wb, HSSFRow row, short col,
                                   String val, HSSFCellStyle cellStyle) {
		HSSFCell cell = row.createCell(col);
		cell.setCellValue(val);
		cell.setCellStyle(cellStyle);
	}
}
