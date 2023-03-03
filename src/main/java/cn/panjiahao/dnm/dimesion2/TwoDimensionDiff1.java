package cn.panjiahao.dnm.dimesion2;

import cn.panjiahao.dnm.dimesion2.entity.Cell;
import cn.panjiahao.dnm.dimesion2.entity.Table;
import cn.panjiahao.dnm.dimesion2.enums.FROM;
import cn.panjiahao.dnm.dimesion2.enums.OpFlag;
import cn.panjiahao.dnm.dimesion2.util.CommonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

import static cn.panjiahao.dnm.dimesion2.util.CommonUtil.*;

/**
 * 二维数据比对算法1
 *
 * @author panjiahao.cs@foxmail.com
 * @date 2023/3/2 19:43
 */
public class TwoDimensionDiff1 {
    static Table leftTable, rightTable;
    /**
     * dp[i][j]表示将左表的[1,i]行编辑成右表的[1,j]行的最小编辑距离
     */
    static int[][] dp;
    /**
     * path[i][j]记录dp[i][j]从哪个方向转移过来
     */
    static int[][] path;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int rowNum1, colNum1, rowNum2, colNum2;
        rowNum1 = sc.nextInt();
        colNum1 = sc.nextInt();
        String[][] strMatrix1 = inputMatrix(sc, rowNum1, colNum1);
        rowNum2 = sc.nextInt();
        colNum2 = sc.nextInt();
        String[][] strMatrix2 = inputMatrix(sc, rowNum2, colNum2);
        leftTable = strMatrixToTable(strMatrix1);
        rightTable = strMatrixToTable(strMatrix2);
        Cell[][] leftTableCellMatrix = leftTable.getCells();
        Cell[][] rightTableCellMatrix = rightTable.getCells();

        levenshteinDP(leftTableCellMatrix, rightTableCellMatrix);
        List<Operation> rowOps = new ArrayList<Operation>();
        List<List<Operation>> rowOpEditMethod = new ArrayList<>();
        System.out.println("将每一行看成一个元素进行比对：");
        dfs(leftTableCellMatrix, rightTableCellMatrix, rowNum1, rowNum2, rowOps, rowOpEditMethod);

        // T表示转置
        Cell[][] leftTableCellMatrixT = transposeMatrix(leftTableCellMatrix);
        Cell[][] rightTableCellMatrixT = transposeMatrix(rightTableCellMatrix);
        int rowNum1T = leftTableCellMatrixT.length;
        int rowNum2T = rightTableCellMatrixT.length;
        System.out.println("将每一列看成一个元素进行比对：");
        levenshteinDP(leftTableCellMatrixT, rightTableCellMatrixT);
        List<Operation> colOps = new ArrayList<Operation>();
        List<List<Operation>> colOpEditMethod = new ArrayList<>();
        dfs(leftTableCellMatrixT, rightTableCellMatrixT, rowNum1T, rowNum2T, colOps, colOpEditMethod);

        getDiffRes(leftTableCellMatrix, rightTableCellMatrix, rowOpEditMethod.get(0), colOpEditMethod.get(0));
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
            for (int k = ops.size() - 1; k >= 0; k--) {
                printOperation(ops.get(k));
            }
            // 打印结束
            System.out.println(("=======已结束一种编辑方式======="));
        }
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Operation {
        // 操作方式 0 什么都不干 1 插入 2 删除 3 替换
        int flag;
        // 操作的左表的行
        Cell[] cellArr1;
        // 在左表中的操作下标，针对插入和删除操作
        int rowPos1;
        // 操作的右表的行, 针对替换操作, cellArr1 替换为 cellArr2
        Cell[] cellArr2;
        // 在右表中的操作下标，针对替换操作
        int rowPos2;
    }

    static void printOperation(Operation op) {
        if (op == null) {
            return;
        }
        if (op.flag == OpFlag.NONE.getVal()) {
            System.out.printf("对应：左表第%d行%s == 右表第%d行%s%n", op.rowPos1, printCellArr(op.cellArr1), op.rowPos2, printCellArr(op.cellArr2));
            return;
        }
        if (op.flag == OpFlag.INSERT.getVal()) {
            System.out.printf("插入：在左表第%d行后插入 %s%n", op.rowPos1, printCellArr(op.cellArr1));
        }
        if (op.flag == OpFlag.REMOVE.getVal()) {
            System.out.printf("删除：删除左表的第%d行 %s%n", op.rowPos1, printCellArr(op.cellArr1));
        }
        if (op.flag == OpFlag.REPLACE.getVal()) {
            System.out.printf("对应 替换：左表第%d个行%s => 右表第%d行%s%n", op.rowPos1, printCellArr(op.cellArr1), op.rowPos2, printCellArr(op.cellArr2));
        }
    }

    /**
     * 获取比对结果
     *
     * @param rowOps 把每一行看作一个元素进行比对得到的操作
     * @param colOps 把每一列看作一个元素进行比对得到的操作
     */
    private static void getDiffRes(Cell[][] leftTableCellMatrix, Cell[][] rightTableCellMatrix, List<Operation> rowOps, List<Operation> colOps) {
        List<Operation> rowAddOrRemoveOps = new ArrayList<>();
        List<Operation> colAddOrRemoveOps = new ArrayList<>();
        Set<Integer> leftTableRemoveRowIndexSet = new HashSet<>();
        Set<Integer> leftTableRemoveColIndexSet = new HashSet<>();
        Set<Integer> rightTableRemoveRowIndexSet = new HashSet<>();
        Set<Integer> rightTableRemoveColIndexSet = new HashSet<>();

        for (Operation op : rowOps) {
            if (op.flag == OpFlag.INSERT.getVal() || op.flag == OpFlag.REMOVE.getVal()) {
                rowAddOrRemoveOps.add(op);
                // 将左表新增的行和列转换为对右边的对应行和列的删除，这样两表就能统一大小进行单元格的修改比对
                if (op.flag == OpFlag.INSERT.getVal()) {
                    rightTableRemoveRowIndexSet.add(op.rowPos2);
                } else {
                    leftTableRemoveRowIndexSet.add(op.rowPos1);
                }
            }
        }

        for (Operation op : colOps) {
            // 列对应关系
            if (op.flag == OpFlag.INSERT.getVal() || op.flag == OpFlag.REMOVE.getVal()) {
                colAddOrRemoveOps.add(op);
                if (op.flag == OpFlag.INSERT.getVal()) {
                    rightTableRemoveColIndexSet.add(op.rowPos2);
                } else {
                    leftTableRemoveColIndexSet.add(op.rowPos1);
                }
            }
        }

        List<List<Cell>> newLeftTable = new ArrayList<>();
        List<List<Cell>> newRightTable = new ArrayList<>();

        for (int i = 0; i < leftTableCellMatrix.length; i++) {
            if (leftTableRemoveRowIndexSet.contains(i + 1)) {
                continue;
            }
            List<Cell> newRow = new ArrayList<>();
            for (int j = 0; j < leftTableCellMatrix[i].length; j++) {
                if (leftTableRemoveColIndexSet.contains(j + 1)) {
                    continue;
                } else {
                    newRow.add(leftTableCellMatrix[i][j]);
                }
            }
            newLeftTable.add(newRow);
        }

        for (int i = 0; i < rightTableCellMatrix.length; i++) {
            if (rightTableRemoveRowIndexSet.contains(i + 1)) {
                continue;
            }
            List<Cell> newRow = new ArrayList<>();
            for (int j = 0; j < rightTableCellMatrix[i].length; j++) {
                if (rightTableRemoveColIndexSet.contains(j + 1)) {
                    continue;
                } else {
                    newRow.add(rightTableCellMatrix[i][j]);
                }
            }
            newRightTable.add(newRow);
        }

        System.out.println("===============最终比对结果==============");
        for (Operation op : rowAddOrRemoveOps) {
            if (op.flag == OpFlag.INSERT.getVal()) {
                System.out.printf("插入：在左表第%d行后插入 %s%n", op.rowPos1, printCellArr(op.cellArr1));
            }
            if (op.flag == OpFlag.REMOVE.getVal()) {
                System.out.printf("删除：删除左表的第%d行 %s%n", op.rowPos1, printCellArr(op.cellArr1));
            }
        }
        for (Operation op : colAddOrRemoveOps) {
            if (op.flag == OpFlag.INSERT.getVal()) {
                System.out.printf("插入：在左表第%d列后插入 %s%n", op.rowPos1, printCellArr(op.cellArr1));
            }
            if (op.flag == OpFlag.REMOVE.getVal()) {
                System.out.printf("删除：删除左表的第%d列 %s%n", op.rowPos1, printCellArr(op.cellArr1));
            }
        }

        for (int i = 0; i < newLeftTable.size(); i++) {
            List<Cell> leftTableRow = newLeftTable.get(i);
            List<Cell> rightTableRow = newRightTable.get(i);
            for (int j = 0; j < leftTableRow.size(); j++) {
                Cell leftTableCell = leftTableRow.get(j);
                Cell rightTableCell = rightTableRow.get(j);
                if (!leftTableCell.getValue().equals(rightTableCell.getValue())) {
                    System.out.printf("将左表第%d行，第%d列的%s替换成%s%n", leftTableCell.getRowPos(), leftTableCell.getColPos(), leftTableCell.getValue(), rightTableCell.getValue());
                }
            }
        }
    }

    private static int lcsCost(Cell[] cellArr1, Cell[] cellArr2) {
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
