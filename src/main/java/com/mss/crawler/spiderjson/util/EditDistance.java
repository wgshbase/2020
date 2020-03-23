package com.mss.crawler.spiderjson.util;

import io.renren.modules.spider.entity.News;
import io.renren.modules.spider.utils.SimHashAlgoService;

import java.util.List;

/**
 * 编辑距离(Edit Distance)求字符串相似度
 *
 */
public class EditDistance {

    /**
     * 求三个数中最小的一个
     * @param one
     * @param two
     * @param three
     * @return
     */
    public static int min(int one, int two, int three) {
        int min = one;
        if (two < min) {
            min = two;
        }
        if (three < min) {
            min = three;
        }
        return min;
    }

    /**
     * 求编辑距离(Edit Distance)
     * @param str1
     * @param str2
     * @return 编辑距离
     */
    public static int editDistance(String str1, String str2) {
        int d[][]; // 矩阵
        int y = str1.length();
        int x = str2.length();
        char ch1; // str1的
        char ch2; // str2的
        int temp; // 记录相同字符,在某个矩阵位置值的增量,不是0就是1
        if (y == 0) {
            return x;
        }
        if (x == 0) {
            return y;
        }
        d = new int[y + 1][x + 1]; // 计算编辑距离二维数组
        for (int j = 0; j <= x; j++) { // 初始化编辑距离二维数组第一行
            d[0][j] = j;
        }
        for (int i = 0; i <= y; i++) { // 初始化编辑距离二维数组第一列
            d[i][0] = i;
        }
        for (int i = 1; i <= y; i++) { // 遍历str1
            ch1 = str1.charAt(i - 1);
            // 去匹配str2
            for (int j = 1; j <= x; j++) {
                ch2 = str2.charAt(j - 1);
                if (ch1 == ch2) {
                    temp = 0;
                } else {
                    temp = 1;
                }
                // 左边+1,上边+1, 左上角+temp取最小
                d[i][j] = min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + temp);
            }
        }
        return d[y][x];
    }

    /**
     * 计算相似度
     * @param str1
     * @param str2
     * @return
     */
    public static double similar(String str1, String str2) {
        str1 = trimNewsContent(str1);
        str2 = trimNewsContent(str2);
        // System.out.println("------------" + str1);
        // System.out.println("");
        // System.out.println("");
        // System.out.println("------------" + str2);
        int ed = editDistance(str1, str2);
        return 1 - (double) ed / Math.max(str1.length(), str2.length());
    }

    /**
     * 计算相似度
     * @return
     */
    public static double similar(List<News> news) {
        String str1 = trimNewsContent(news.get(0).getContent());
        String str2 = trimNewsContent(news.get(1).getContent());
        int ed = editDistance(str1, str2);
        return 1 - (double) ed / Math.max(str1.length(), str2.length());
    }

    public static String trimNewsContent(String content) {
        return content.replaceAll("<style.*?</style>","").replaceAll("<.*?>", "").replace("本文来源于国外网站，以下是翻译内容。向下阅读可查看原文","").replace("原文","").replaceAll("[^\\u4e00-\\u9fa5\\d]*","").replace("news_cn_","").replace("news_wx_","");
    }

    public static void main(String[] args) {
	    SimHashAlgoService simhashAlgoService = new SimHashAlgoService();
	    String string = " </div>";
	    String string2 = "</div>";
	    // 返回的指纹已经被切分成4段，方便利用指纹作对比。具体对比方式可自行百度。
	    // List<String> fingerPrints1 = simhashAlgoService.simHash(string,64);
	    // List<String> fingerPrints2 = simhashAlgoService.simHash(string2,64);

        System.out.println("字符串相似度: " + new EditDistance().similar(string, string2));


    }
}
