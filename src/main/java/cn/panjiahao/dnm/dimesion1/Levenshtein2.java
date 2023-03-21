package cn.panjiahao.dnm.dimesion1;

import java.util.ArrayList;
import java.util.List;

/**
 * 参考文章：https://writings.sh/post/algorithm-minimum-edit-distance
 */
public class Levenshtein2 {
    static final int FROM_INIT = 0;
    static final int FROM_LEFT = 1;
    static final int FROM_UP = 2;
    static final int FROM_LEFT_UP_REPLACE = 4;
    static final int FROM_LEFT_UP_COPY = 8;
    static int[][] dp;
    static int[][] path;

    public static void main(String[] args) {
        String[] text1 = new String[]{"aaa", "b", "c", "b", "d", "aa", "b"};
        String[] text2 = new String[]{"bb", "d", "c", "ac", "b", "a"};
        int len1 = text1.length;
        int len2 = text2.length;
        levenshteinDP(text1, text2);
        List<Operation> ops = new ArrayList<Operation>();
        dfs(text1, text2, len1, len2, ops);
    }

    /**
     * 比对的基本单位是一个字符串
     *
     * @param text1 字符串数组1
     * @param text2 字符串数组2
     * @return levenshteinDP数组
     */
    static int[][] levenshteinDP(String[] text1, String[] text2) {
        int len1 = text1.length;
        int len2 = text2.length;
        // dp[i][j]表示从text1[0...i-1]到text2[0...j-1]的最小编辑距离（cost）
        dp = new int[len1 + 1][len2 + 1];
        // path记录此方格的来源是多个此类枚举值的布尔或值
        path = new int[len1 + 1][len2 + 1];

        for (int i = 1; i < len1 + 1; i++) {
            dp[i][0] = dp[i - 1][0] + text1[i - 1].length();
            path[i][0] = FROM_UP;
        }
        for (int j = 1; j < len2 + 1; j++) {
            dp[0][j] = dp[0][j - 1] + text2[j - 1].length();
            path[0][j] = FROM_LEFT;
        }

        for (int i = 1; i < len1 + 1; i++) {
            for (int j = 1; j < len2 + 1; j++) {
                path[i][j] = FROM_INIT;
                // 删除、增加的cost都是字符串的长度
                int left = dp[i][j - 1] + text2[j - 1].length();
                int up = dp[i - 1][j] + text1[i - 1].length();
                int leftUp;
                boolean replace;

                if (text1[i - 1].equals(text2[j - 1])) {
                    leftUp = dp[i - 1][j - 1];
                    replace = false;
                } else {
                    // 修改的cost是两个字符串的长度较小值
                    leftUp = dp[i - 1][j - 1] + Math.min(text1[i - 1].length(), text2[j - 1].length());
                    replace = true;
                }

                dp[i][j] = Math.min(Math.min(left, up), leftUp);

                if (dp[i][j] == left) {
                    path[i][j] |= FROM_LEFT;
                }
                if (dp[i][j] == up) {
                    path[i][j] |= FROM_UP;
                }
                // 对应：两字符串完全一样或者可以修改成一样
                if (dp[i][j] == leftUp) {
                    if (replace) {
                        path[i][j] |= FROM_LEFT_UP_REPLACE;
                    } else {
                        path[i][j] |= FROM_LEFT_UP_COPY;
                    }
                }
            }
        }
        return dp;
    }

    static void dfs(String[] text1, String[] text2, int i, int j, List<Operation> ops) {
        Operation op = new Operation();
        ops.add(op);
        op.index1 = i;
        op.index2 = j;

        if ((path[i][j] & FROM_LEFT) > 0) {
            // 左边方格，插入一个字符串 b[j-1] 而来
            op.flag = 1;
            // op.index1 = i;
            op.s1 = text2[j - 1];
            // 向左 DFS
            dfs(text1, text2, i, j - 1, ops);
            ops.remove(ops.size() - 1);
        }

        if ((path[i][j] & FROM_UP) > 0) {
            // 上面方格，删除一个字符串 a[i-1] 而来
            op.flag = 2;
            // op.index1 = i;
            op.s1 = text1[i - 1];
            // 向上 DFS
            dfs(text1, text2, i - 1, j, ops);
            ops.remove(ops.size() - 1);
        }

        if ((path[i][j] & FROM_LEFT_UP_COPY) > 0 || (path[i][j] & FROM_LEFT_UP_REPLACE) > 0) {
            if ((path[i][j] & FROM_LEFT_UP_REPLACE) > 0) {
                // 左上方格，替换 a[i-1] 到 b[j-1] 而来
                op.flag = 3;
                op.s1 = text1[i - 1];
                op.s2 = text2[j - 1];
            } else {
                // 拷贝而来，无需记录
                // 置 0 表示忽略
                op.flag = 0;
            }

            // 向左上 DFS
            dfs(text1, text2, i - 1, j - 1, ops);
            ops.remove(ops.size() - 1);
        }

        if (i == 1 && j == 1) {
            // 反向打印 ops 序列
            for (int k = ops.size() - 1; k >= 0; k--) {
                printOperation(ops.get(k));
            }
            // 打印结束
            System.out.println(("=======已结束一种编辑方式======="));
        }
    }

    static class Operation {
        // 操作方式 0 什么都不干 1 插入 2 删除 3 替换
        int flag;
        // 操作的字符串 1
        String s1;
        // 在字符串1中的操作下标，针对插入和删除操作
        int index1;
        // 操作的字符串 2, 针对替换操作, ch1 替换为 ch2
        String s2;
        // 在字符串2中的操作下标，针对替换操作
        int index2;
    }

    static void printOperation(Operation op) {
        if (op == null) {
            return;
        }
        if (op.flag == 0) {
            System.out.printf("对应：字符串数组1第%d个字符串%s == 字符串数组2第%d个字符串%s%n", op.index1, op.s1, op.index2, op.s2);
            return;
        }
        if (op.flag == 1) {
            System.out.printf("插入：在字符串数组1第%d个字符串后插入 %s%n", op.index1, op.s1);
        }
        if (op.flag == 2) {
            System.out.printf("删除：删除字符串数组1的第%d个字符串 %s%n", op.index1, op.s1);
        }
        if (op.flag == 3) {
            System.out.printf("对应 替换：字符串数组1第%d个字符串%s => 字符串数组2第%d个字符串%s%n", op.index1, op.s1, op.index2, op.s2);
        }
    }

    static void printMatrix(int[][] matrix) {
        int n = matrix.length;

        for (int row = 0; row < matrix.length; row++) {
            int col = 0;
            for (col = 0; col < matrix[row].length - 1; col++) {
                System.out.print(matrix[row][col] + " ");
            }
            System.out.println(matrix[row][col]);
        }

    }

}
