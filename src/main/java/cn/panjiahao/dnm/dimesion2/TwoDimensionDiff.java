package cn.panjiahao.dnm.dimesion2;

import cn.panjiahao.dnm.dimesion2.entity.Cell;
import cn.panjiahao.dnm.dimesion2.entity.DiffJob;
import cn.panjiahao.dnm.dimesion2.entity.Operation;
import cn.panjiahao.dnm.dimesion2.enums.FROM;
import cn.panjiahao.dnm.dimesion2.enums.OpFlag;
import cn.panjiahao.dnm.dimesion2.util.CommonUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cn.panjiahao.dnm.dimesion2.util.CommonUtil.deepCopy;

/**
 * 二维数据比对算法
 *
 * @author panjiahao.cs@foxmail.com
 * @date 2023/3/2 19:43
 */
public class TwoDimensionDiff {
    /**
     * dp[i][j]表示将左表的[1,i]行编辑成右表的[1,j]行的最小编辑距离
     */
    static int[][] dp;
    /**
     * path[i][j]记录dp[i][j]从哪个方向转移过来
     */
    static int[][] path;

    static void diff(DiffJob diffJob) {
        Cell[][] leftTableCellMatrix = diffJob.getLeftTable().getCells();
        Cell[][] rightTableCellMatrix = diffJob.getRightTable().getCells();
        int rowNum1 = leftTableCellMatrix.length;
        int rowNum2 = rightTableCellMatrix.length;
        levenshteinDP(leftTableCellMatrix, rightTableCellMatrix);
        List<Operation> rowEditMethod = new ArrayList<>();
        List<List<Operation>> rowEditMethods = new ArrayList<>();
        dfs(leftTableCellMatrix, rightTableCellMatrix, rowNum1, rowNum2, rowEditMethod, rowEditMethods);
        diffJob.setRowEditMethods(rowEditMethods);
    }

    /**
     * 比对的基本单位是单元格
     *
     * @param leftTableCellMatrix  左表
     * @param rightTableCellMatrix 右表
     * @return levenshteinDP数组
     */
    static int[][] levenshteinDP(Cell[][] leftTableCellMatrix, Cell[][] rightTableCellMatrix) {
        int rowNum1 = leftTableCellMatrix.length;
        int rowNum2 = rightTableCellMatrix.length;
        // dp[i][j]leftTableCellMatrix[0...i-1]rightTableCellMatrix[0...j-1]的最小编辑距离（cost）
        dp = new int[rowNum1 + 1][rowNum2 + 1];
        // path记录此方格的来源是多个此类枚举值的布尔或值
        path = new int[rowNum1 + 1][rowNum2 + 1];

        for (int i = 1; i <= rowNum1; i++) {
            dp[i][0] = dp[i - 1][0] + leftTableCellMatrix[i - 1].length;
            path[i][0] = FROM.UP.getVal();
        }
        for (int j = 1; j <= rowNum2; j++) {
            dp[0][j] = dp[0][j - 1] + rightTableCellMatrix[j - 1].length;
            path[0][j] = FROM.LEFT.getVal();
        }

        for (int i = 1; i < rowNum1 + 1; i++) {
            for (int j = 1; j < rowNum2 + 1; j++) {
                path[i][j] = FROM.INIT.getVal();
                // 删除、增加的cost都是行的长度
                int left = dp[i][j - 1] + rightTableCellMatrix[j - 1].length;
                int up = dp[i - 1][j] + leftTableCellMatrix[i - 1].length;
                int leftUp;
                boolean replace;

                if (CommonUtil.cellArrEquals(leftTableCellMatrix[i - 1], rightTableCellMatrix[j - 1])) {
                    leftUp = dp[i - 1][j - 1];
                    replace = false;
                } else {
                    // 修改的cost是两个字符串数组的LCS距离
                    leftUp = dp[i - 1][j - 1] + lcsCost(leftTableCellMatrix[i - 1], rightTableCellMatrix[j - 1]);
                    replace = true;
                }

                dp[i][j] = Math.min(Math.min(left, up), leftUp);

                if (dp[i][j] == left) {
                    path[i][j] |= FROM.LEFT.getVal();
                }
                if (dp[i][j] == up) {
                    path[i][j] |= FROM.UP.getVal();
                }
                // 对应：两字符串完全一样或者可以修改成一样
                if (dp[i][j] == leftUp) {
                    if (replace) {
                        path[i][j] |= FROM.LEFT_UP_REPLACE.getVal();
                    } else {
                        path[i][j] |= FROM.LEFT_UP_COPY.getVal();
                    }
                }
            }
        }
        return dp;
    }

    /**
     * 从path数组中得出所有编辑方法
     *
     * @param leftTableCellMatrix  左表
     * @param rightTableCellMatrix 右表
     * @param i                    左表第i行
     * @param j                    右边第j行
     * @param ops                  编辑方法
     * @param editMethods          所有编辑方法
     */
    static void dfs(Cell[][] leftTableCellMatrix, Cell[][] rightTableCellMatrix, int i, int j, List<Operation> ops, List<List<Operation>> editMethods) {
        Operation op = new Operation();
        op.rowPos1 = i;
        op.rowPos2 = j;

        if ((path[i][j] & FROM.LEFT.getVal()) > 0) {
            // 左边方格，插入一个字符串 b[j-1] 而来
            op.flag = OpFlag.INSERT.getVal();
            op.cellArr1 = rightTableCellMatrix[j - 1];
            ops.add(op);

            // 向左 DFS
            dfs(leftTableCellMatrix, rightTableCellMatrix, i, j - 1, ops, editMethods);
            ops.remove(ops.size() - 1);
        }

        if ((path[i][j] & FROM.UP.getVal()) > 0) {
            // 上面方格，删除一个字符串 a[i-1] 而来
            op.flag = OpFlag.REMOVE.getVal();
            op.cellArr1 = leftTableCellMatrix[i - 1];
            ops.add(op);

            // 向上 DFS
            dfs(leftTableCellMatrix, rightTableCellMatrix, i - 1, j, ops, editMethods);
            ops.remove(ops.size() - 1);
        }

        if ((path[i][j] & FROM.LEFT_UP_COPY.getVal()) > 0 || (path[i][j] & FROM.LEFT_UP_REPLACE.getVal()) > 0) {
            if ((path[i][j] & FROM.LEFT_UP_REPLACE.getVal()) > 0) {
                // 左上方格，替换 a[i-1] 到 b[j-1] 而来
                op.flag = OpFlag.REPLACE.getVal();
                op.cellArr1 = leftTableCellMatrix[i - 1];
                op.cellArr2 = rightTableCellMatrix[j - 1];
            } else {
                // 拷贝而来，无需记录
                // 置 0 表示忽略
                op.flag = OpFlag.NONE.getVal();
            }
            ops.add(op);
            // 向左上 DFS
            dfs(leftTableCellMatrix, rightTableCellMatrix, i - 1, j - 1, ops, editMethods);
            ops.remove(ops.size() - 1);
        }

        if (i == 0 && j == 0) {
            List<Operation> res = deepCopy(ops);
            Collections.reverse(res);
            editMethods.add(res);
            // 反向打印 ops 序列
            // for (int k = ops.size() - 1; k >= 0; k--) {
            //     printOperation(ops.get(k));
            // }
            // 打印结束
            // System.out.println(("=======已结束一种编辑方式======="));
        }
    }

    static int lcsCost(Cell[] cellArr1, Cell[] cellArr2) {
        int rowNum1 = cellArr1.length;
        int rowNum2 = cellArr2.length;
        // dp[i][j] 表示 cellArr1[0,i]和cellArr2[0,j]的最长公共子序列的长度
        int[][] dp = new int[rowNum1 + 1][rowNum2 + 1];

        for (int i = 0; i < rowNum1; i++) {
            for (int j = 0; j < rowNum2; j++) {
                if (cellArr1[i].getValue().equals(cellArr2[j].getValue())) {
                    dp[i + 1][j + 1] = dp[i][j] + 1;
                } else {
                    dp[i + 1][j + 1] = Math.max(dp[i][j + 1], dp[i + 1][j]);
                }
            }
        }

        return Math.max(rowNum1, rowNum2) - dp[rowNum1][rowNum2];
    }
}
