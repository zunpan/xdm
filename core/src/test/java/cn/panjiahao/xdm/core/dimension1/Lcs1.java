package cn.panjiahao.xdm.core.dimension1;

import org.junit.jupiter.api.Test;

public class Lcs1 {

    @Test
    void lcs1Test() {
        String text1 = "abcbdab";
        String text2 = "bdcaba";
        // System.out.println("最长公共子序列："+ lcs(text1,text2));
        System.out.println("最小编辑距离：" + lcsCost(text1, text2));
    }

    public static String lcs(String text1, String text2) {
        int len1 = text1.length();
        int len2 = text2.length();
        int[][] dp = lcsDP(text1, text2);
        int lcsLen = dp[len1][len2];
        StringBuilder sb = new StringBuilder();
        // 插入和删除是相对的，对text1删除字符等于对text2插入字符，统一对一个字符串对插入和删除可以得到另一个字符串
        while (len1 > 0 || len2 > 0) {
            // 向上移动
            if (len1 > 0 && dp[len1][len2] == dp[len1 - 1][len2]) {
                System.out.println("删除：删除text1第" + len1 + "个字符" + text1.charAt(len1 - 1));
                // System.out.println(",或者给text2第"+len2+"个字符后面插入"+text1.charAt(len1-1));
                len1--;
            }
            // 向左移动
            else if (len2 > 0 && dp[len1][len2] == dp[len1][len2 - 1]) {
                System.out.println("插入：给text1第" + len1 + "个字符后面插入" + text2.charAt(len2 - 1));
                // System.out.println(",或者删除text2第"+len2+"个字符"+text2.charAt(len2-1));
                len2--;
            }
            // 向左上方移动
            else {
                if (sb.length() < lcsLen) {
                    sb.append(text1.charAt(len1 - 1));
                    System.out.println("对应：text1的第" + len1 + "个字符 == text2的第" + len2 + "个字符");
                    len1--;
                    len2--;
                }
            }
        }
        return sb.reverse().toString();
    }

    public static int lcsLen(String text1, String text2) {
        return lcs(text1, text2).length();
    }

    public static int lcsCost(String text1, String text2) {
        int lcsLen = lcsLen(text1, text2);
        return Math.max(text1.length(), text2.length()) - lcsLen;
    }

    /**
     * 比对的基本单位是单个字符
     *
     * @param text1 字符串1
     * @param text2 字符串2
     * @return lcsDP
     */
    private static int[][] lcsDP(String text1, String text2) {
        int len1 = text1.length();
        int len2 = text2.length();
        // dp[i][j] 表示 text1[0,i]和text2[0,j]的最长公共子序列的长度
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i < len1; i++) {
            for (int j = 0; j < len2; j++) {
                if (text1.charAt(i) == text2.charAt(j)) {
                    dp[i + 1][j + 1] = dp[i][j] + 1;
                } else {
                    dp[i + 1][j + 1] = Math.max(dp[i][j + 1], dp[i + 1][j]);
                }
            }
        }
        return dp;
    }
}
