package io.renren.modules.spider.utils;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 自然语言抽取接口工具类
 */
@Component
public class NLPUtils {

    /**
     * solr日期转换
     * @param solrDate
     * @return
     */
    public static String formDate(Date solrDate, String pattern) {
        String newDate = "";
        try {
            if (solrDate == null) {
                return null;
            }
            Calendar ca = Calendar.getInstance();
            ca.setTime(solrDate);
            ca.add(Calendar.HOUR_OF_DAY, -8);
            newDate = new SimpleDateFormat(pattern).format(ca.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newDate;
    }

    /**
     * 返回
     *
     */
    public static Map<String, List<String>> getEntityWithRate(HashMap<String, Object> result) {
        Map<String, List<String>> r = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        if(null != result && result.size() > 0) {
            for(Map.Entry<String, Object> entry : result.entrySet()) {
                List<String> k = new ArrayList<>();
                LinkedHashMap<String, Integer> val = (LinkedHashMap) entry.getValue();
                if(null != val && val.size() > 0) {
                    for(Map.Entry<String, Integer> en : val.entrySet()) {
                        k.add(en.getKey() + "（"+en.getValue()+"）");
                    }
                }
                r.put(entry.getKey(), k);
            }
        }
        return r;
    }

    /**
     * 返回新闻的关键字带比率
     *
     */
    public static List<String> getEntityKeywordsWithRate(HashMap<String, Object> result) {
        List<String> keywords = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        if(null != result && result.size() > 0) {
            for(Map.Entry<String, Object> entry : result.entrySet()) {
                keywords.add(entry.getKey()) ;
            }
        }
        if(keywords.size() > 6) {
            return keywords.subList(0 , 6);
        } else {
            return keywords;
        }
    }

    /**
     * 返回新闻的关键字
     *
     */
    public static String getEntityKeywords(HashMap<String, Object> result) {
        StringBuilder sb = new StringBuilder();
        if(null != result && result.size() > 0) {
            for(Map.Entry entry : result.entrySet()) {
                sb.append(entry.getKey() + ";");
            }
            if(sb.toString().endsWith(";")) {
                return sb.toString().substring(0, sb.toString().length() - 1);
            }
            return sb.toString();
        }
        return sb.toString();
    }

    /**
     * 返回字段项需要显示的内容
     */
    public static Map<String, String> getEntityResult(Map<String, Set<String>> recognize) {
        // 抽取的安全实体
        Map<String, String> entityResult = new HashMap<>();

        StringBuilder sb = new StringBuilder();
        Set<String> person = recognize.get("person");
        if(null != person && person.size() > 0) {
            ArrayList<String> l = new ArrayList<>(person);
            for(String s : l) {
                if(l.indexOf(s) == (l.size() - 1)) {
                    sb.append(s);
                } else {
                    sb.append(s + ";");
                }

            }
            if(l.size() > 0) {
                entityResult.put("person", sb.toString());
            }
        }
        sb = new StringBuilder();
        Set<String> place = recognize.get("place");
        if(null != place && place.size() > 0) {
            ArrayList<String> l = new ArrayList<>(place);
            for(String s : l) {
                if(l.indexOf(s) == (l.size() - 1)) {
                    sb.append(s);
                } else {
                    sb.append(s + ";");
                }

            }
            if(l.size() > 0) {
                entityResult.put("place", sb.toString());
            }
        }
        sb = new StringBuilder();
        Set<String> time = recognize.get("time");
        if(null != time && time.size() > 0) {
            ArrayList<String> l = new ArrayList<>(time);
            for(String s : l) {
                if(l.indexOf(s) == (l.size() - 1)) {
                    sb.append(s);
                } else {
                    sb.append(s + ";");
                }

            }
            if(l.size() > 0) {
                entityResult.put("time", sb.toString());
            }
        }
        sb = new StringBuilder();
        Set<String> organ = recognize.get("organ");
        if(null != organ && organ.size() > 0) {
            ArrayList<String> l = new ArrayList<>(organ);
            for(String s : l) {
                if(l.indexOf(s) == (l.size() - 1)) {
                    sb.append(s);
                } else {
                    sb.append(s + ";");
                }

            }
            if(l.size() > 0) {
                entityResult.put("organ", sb.toString());
            }
        }

        return entityResult;
    }

    /**
     * 若集合的数量超过 5 个，返回 5 个结果，否则返回实际的数量
     */
    public static List<String> getPartCollection(Set<String> set) {
        if(null != set) {
            List<String> list = new ArrayList<>(set);
            if(list.size() > 5) {
                return list.subList(0, 5);
            } else {
                return list;
            }
        } else {
            return new ArrayList<>();
        }

    }

//    /**
//     * 返回实体抽取关系图需要的结果
//     */
//    public List<Map<String, Set<String>>> getRelationViewEntityResult(String title, String content) {
//        // 抽取的安全实体
//        Map<String, Set<String>> recognize = nlpService.recognize(content);
//
//        List<Map<String, Set<String>>> entityResult = new ArrayList<>();
//        Map<String, Set<String>> entity = new HashMap<>();
//        Set<String> temp = new HashSet<>();
//        temp.add("实体");
//        entity.put("parentNode", temp);
//        temp = new HashSet<>();
//        temp.add("人名");
//        temp.add("地名");
//        temp.add("机构名");
//        temp.add("时间");
//        entity.put("childNodes", temp);
//        entityResult.add(entity);
//
//        temp = new HashSet<>();
//        temp.add("人名");
//        entity = new HashMap<>();
//        entity.put("parentNode", temp);
//        entity.put("childNodes", recognize.get("person"));
//        entityResult.add(entity);
//        temp = new HashSet<>();
//        temp.add("地名");
//        entity = new HashMap<>();
//        entity.put("parentNode", temp);
//        entity.put("childNodes", recognize.get("place"));
//        entityResult.add(entity);
//        temp = new HashSet<>();
//        temp.add("机构名");
//        entity = new HashMap<>();
//        entity.put("parentNode", temp);
//        entity.put("childNodes", recognize.get("organ"));
//        entityResult.add(entity);
//        temp = new HashSet<>();
//        temp.add("时间");
//        entity = new HashMap<>();
//        entity.put("parentNode", temp);
//        entity.put("childNodes", recognize.get("time"));
//        entityResult.add(entity);
//
//        return entityResult;
//    }

    public static Map<String, String> getFormattedEntityResult(Map<String, Set<String>> recognize, Map<String, Object> formattedResult) {
        // 抽取的安全实体
        Map<String, String> entityResult = new HashMap<>();

        StringBuilder sb = new StringBuilder();
        Set<String> person = recognize.get("person");
        Map<String, String> personMap = (Map<String, String>) formattedResult.get("person");
        if(null != person && person.size() > 0) {
            ArrayList<String> l = new ArrayList<>(person);
            for(String s : l) {
                if(personMap.containsKey(s) && !(sb.toString().contains(personMap.get(s) + ";"))) {
                    sb.append(personMap.get(s)).append(";");
                } else {
                    continue;
                }
            }
            if(sb.toString().length() > 0) {
                entityResult.put("person", sb.toString().substring(0, sb.toString().length() - 1));
            } else {
                entityResult.put("person", "");
            }
        }
        sb = new StringBuilder();
        Set<String> place = recognize.get("place");
        Map<String, String> placeMap = (Map<String, String>) formattedResult.get("place");
        if(null != place && place.size() > 0) {
            ArrayList<String> l = new ArrayList<>(place);
            for(String s : l) {
                if(placeMap.containsKey(s) && !(sb.toString().contains(placeMap.get(s) + ";"))) {
                    sb.append(placeMap.get(s)).append(";");
                } else {
                    continue;
                }
            }
            if(sb.toString().length() > 0) {
                entityResult.put("place", sb.toString().substring(0, sb.toString().length() - 1));
            } else {
                entityResult.put("place", "");
            }
        }
        sb = new StringBuilder();
        Set<String> time = recognize.get("time");
        if(null != time && time.size() > 0) {
            ArrayList<String> l = new ArrayList<>(time);
            for(String s : l) {
                if(l.indexOf(s) == (l.size() - 1)) {
                    sb.append(s);
                } else {
                    sb.append(s + ";");
                }

            }
            if(l.size() > 0) {
                entityResult.put("time", sb.toString());
            }
        }
        sb = new StringBuilder();
        Set<String> organ = recognize.get("organ");
        Map<String, String> organMap = (Map<String, String>) formattedResult.get("organ");
        if(null != organ && organ.size() > 0) {
            ArrayList<String> l = new ArrayList<>(organ);
            for(String s : l) {
                if(organMap.containsKey(s) && !(sb.toString().contains(organMap.get(s) + ";"))) {
                    sb.append(organMap.get(s)).append(";");
                } else {
                    continue;
                }
            }
            if(sb.toString().length() > 0) {
                entityResult.put("organ", sb.toString().substring(0, sb.toString().length() - 1));
            } else {
                entityResult.put("organ", "");
            }
        }

        return entityResult;
    }

}
