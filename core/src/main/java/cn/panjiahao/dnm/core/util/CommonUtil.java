package cn.panjiahao.dnm.core.util;

import cn.panjiahao.dnm.core.entity.Cell;
import cn.panjiahao.dnm.core.entity.Operation;
import cn.panjiahao.dnm.core.entity.Table;
import cn.panjiahao.dnm.core.enums.OpFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 通用工具
 *
 * @author panjiahao.cs@foxmail.com
 * @date 2023/3/2 19:43
 */
public class CommonUtil {

    /**
     * 矩阵转置
     *
     * @param matrix 矩阵
     * @return 转置矩阵
     */
    public static Cell[][] transposeMatrix(Cell[][] matrix) {
        if (matrix == null || matrix.length == 0) {
            return new Cell[0][];
        }
        int n = matrix.length;
        int m = matrix[0].length;

        Cell[][] res = new Cell[m][n];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                res[i][j] = matrix[j][i];
            }
        }
        return res;
    }

    /**
     * 表体转置
     *
     * @param table 表
     * @return 转置表
     */
    public static Table transposeMatrix(Table table) {
        Cell[][] cells = table.getBodyCells();
        int rowNum = cells.length;
        int colNum = cells[0].length;
        Cell.CellBuilder cellBuilder = Cell.builder();

        Cell[][] res = new Cell[colNum][rowNum];

        for (int i = 0; i < colNum; i++) {
            for (int j = 0; j < rowNum; j++) {
                res[i][j] = cellBuilder.rowPos(i + 1).colPos(j + 1).value(cells[j][i].getValue()).build();
            }
        }
        return Table.builder().bodyRowNum(rowNum).colNum(colNum).bodyCells(res).build();
    }

    /**
     * 比较两个Cell数组的内容是否完全一样
     *
     * @param cellArr1 Cell数组1
     * @param cellArr2 Cell数组2
     * @return 是否完全一样
     */
    public static boolean cellArrEquals(Cell[] cellArr1, Cell[] cellArr2) {
        if (cellArr1 == null && cellArr2 == null) {
            return true;
        }
        if ((cellArr1 != null && cellArr2 == null) || (cellArr1 == null && cellArr2 != null)) {
            return false;
        }
        if (cellArr1.length != cellArr2.length) {
            return false;
        }
        int len = cellArr1.length;
        for (int i = 0; i < len; i++) {
            if (cellArr1[i]!=null&&cellArr2[i].getValue()!=null&&!cellArr1[i].getValue().equals(cellArr2[i].getValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 比较两个Cell数组的内容是否完全一样
     *
     * @param cellArr1 Cell数组1
     * @param cellArr2 Cell数组2
     * @param h        前h个单元格
     * @return 是否完全一样
     */
    public static boolean cellArrEqualsWithH(Cell[] cellArr1, Cell[] cellArr2, int h) {
        if (cellArr1 == null && cellArr2 == null) {
            return true;
        }
        if ((cellArr1 != null && cellArr2 == null) || (cellArr1 == null && cellArr2 != null)) {
            return false;
        }

        for (int i = 0; i < h; i++) {
            if (!cellArr1[i].getValue().equals(cellArr2[i].getValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 打印Cell数组的内容
     *
     * @param cellArr Cell数组
     */
    public static String cellArrToString(Cell[] cellArr) {
        if (cellArr == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder("[");
        int i = 0;
        for (; i < cellArr.length - 1; i++) {
            sb.append("\"").append(cellArr[i].getValue()).append("\"").append(",");
        }
        sb.append("\"").append(cellArr[i].getValue()).append("\"").append("]");

        return sb.toString();
    }

    /**
     * 打印Cell数组的内容
     *
     * @param cellArr Cell数组
     */
    public static String cellArrToString(Cell[]... cellArr) {
        if (cellArr == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < cellArr.length; i++) {
            if (cellArr[i] == null) {
                continue;
            }
            for (int j = 0; j < cellArr[i].length; j++) {
                sb.append("\"").append(cellArr[i][j].getValue()).append("\"").append(",");
            }
        }
        sb.deleteCharAt(sb.length() - 1).append("]");
        return sb.toString();
    }

    /**
     * 打印操作
     *
     * @param op 操作
     */
    public static void printOperation(Operation op) {
        if (op == null) {
            return;
        }
        if (op.flag == OpFlag.NONE.getVal()) {
            System.out.printf("对应：左表第%d行%s == 右表第%d行%s%n", op.rowPos1, cellArrToString(op.cellArr1), op.rowPos2, cellArrToString(op.cellArr2));
            return;
        }
        if (op.flag == OpFlag.INSERT.getVal()) {
            System.out.printf("插入：在左表第%d行后插入 %s%n", op.rowPos1, cellArrToString(op.cellArr1));
        }
        if (op.flag == OpFlag.REMOVE.getVal()) {
            System.out.printf("删除：删除左表的第%d行 %s%n", op.rowPos1, cellArrToString(op.cellArr1));
        }
        if (op.flag == OpFlag.REPLACE.getVal()) {
            System.out.printf("对应 替换：左表第%d个行%s => 右表第%d行%s%n", op.rowPos1, cellArrToString(op.cellArr1), op.rowPos2, cellArrToString(op.cellArr2));
        }
    }

    /**
     * 打印Cell矩阵的内容
     *
     * @param matrix Cell矩阵
     */
    public static void printCellMatrix(Cell[][] matrix) {
        if (matrix == null || matrix.length == 0) {
            return;
        }
        for (Cell[] cells : matrix) {
            int col = 0;
            for (col = 0; col < cells.length - 1; col++) {
                System.out.print(cells[col].getValue() + " ");
            }
            System.out.println(cells[col].getValue());
        }
    }

    public static void printInsertOp(int row, Cell[] cellArr1,boolean isRowOrCol) {
        String s1,s2;
        if (isRowOrCol) {
            s1 = "行";
            s2 = "列";
        } else {
            s1 = "列";
            s2 = "行";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("插入：在左表第").append(row).append(s1).append("后的");
        for (Cell cell : cellArr1) {
            sb.append("第");
            if (isRowOrCol) {
                sb.append(cell.getColPos());
            } else {
                sb.append(cell.getRowPos());
            }
            sb.append(s2).append("插入").append(cell.getValue()).append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        System.out.println(sb.toString());
    }

    public static void printRemoveOp(int row, Cell[] cellArr1,boolean isRowOrCol) {
        String s1,s2;
        if (isRowOrCol) {
            s1 = "行";
            s2 = "列";
        } else {
            s1 = "列";
            s2 = "行";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("删除：删除左表的第").append(row).append(s1).append("的");
        for (Cell cell : cellArr1) {
            sb.append("第");
            if (isRowOrCol) {
                sb.append(cell.getColPos());
            } else {
                sb.append(cell.getRowPos());
            }
            sb.append(s2).append("的").append(cell.getValue()).append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        System.out.println(sb.toString());
    }

    public static void printReplaceOp(int row, Cell[] cellArr1, Cell[] cellArr2,boolean isRowOrCol) {
        String s1,s2;
        if (isRowOrCol) {
            s1 = "行";
            s2 = "列";
        } else {
            s1 = "列";
            s2 = "行";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("修改：将左表的第").append(row).append(s1).append("的");
        int maxLen = Math.max(cellArr1.length, cellArr2.length);
        for (int i = 0; i < cellArr1.length; i++) {
            if (!cellArr1[i].getValue().equals(cellArr2[i].getValue())) {
                sb.append("第");
                if (isRowOrCol) {
                    sb.append(cellArr1[i].getColPos());
                } else {
                    sb.append(cellArr1[i].getRowPos());
                }
                sb.append(s2).append("的").append(cellArr1[i].getValue()).append("修改成").append(cellArr2[i].getValue()).append(",");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        System.out.println(sb.toString());
    }

    public static void printMoveOp(int row, int rowNew,boolean isRowOrCol) {
        String s1;
        if (isRowOrCol) {
            s1 = "行";
        } else {
            s1 = "列";
        }
        System.out.printf("移动：将左表的第%d%s移动到第%d%s后面%n", row, s1,rowNew,s1);
    }

    public static void printMoveReplaceOp(int row, int rowNew, Cell[] cellArr1,Cell[] cellArr1New,boolean isRowOrCol) {
        String s1,s2;
        if (isRowOrCol) {
            s1 = "行";
            s2 = "列";
        } else {
            s1 = "列";
            s2 = "行";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("移动且修改：将左表的第").append(row).append(s1).append("移动到第").append(rowNew).append(s1).append("后面,");
        for (int i = 0; i < cellArr1.length; i++) {
            if (!cellArr1[i].getValue().equals(cellArr1New[i].getValue())) {
                sb.append("第");
                if (isRowOrCol) {
                    sb.append(cellArr1[i].getColPos());
                } else {
                    sb.append(cellArr1[i].getRowPos());
                }
                sb.append(s2).append("的").append(cellArr1[i].getValue()).append("修改成").append(cellArr1New[i].getValue()).append(",");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        System.out.println(sb.toString());
    }

    /**
     * 输入矩阵
     *
     * @param rowNum 行数
     * @param colNum 列数
     * @return 输入矩阵
     */
    public static String[][] inputMatrix(Scanner sc, int rowNum, int colNum) {
        String[][] table = new String[rowNum][colNum];
        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                table[i][j] = sc.next();
            }
        }
        return table;
    }

    /**
     * 字符串数组转成表头或表体
     *
     * @param strMatrix 字符串数组
     * @return Cell[][]
     */
    public static Cell[][] strMatrixToCells(String[][] strMatrix) {
        if (strMatrix == null || strMatrix.length == 0) {
            return new Cell[0][];
        }
        int rowNum = strMatrix.length;
        int colNum = strMatrix[0].length;
        Cell[][] cells = new Cell[rowNum][colNum];
        Cell.CellBuilder cellBuilder = Cell.builder();

        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                cells[i][j] = cellBuilder.rowPos(i + 1).colPos(j + 1).value(strMatrix[i][j]).build();
            }
        }
        return cells;
    }

    /**
     * 二维列表转二维数组
     * @param lists 二维列表
     * @return String[][]
     */
    public static String[][] list2Array(List<List<String>> lists) {
        if (lists == null || lists.size() == 0) {
            return new String[0][];
        }
        return lists.stream().map(l-> l.toArray(new String[0])).toArray(String[][]::new);
    }

    public static List<Operation> deepCopy(List<Operation> ops) {
        List<Operation> res = new ArrayList<>();
        Operation.OperationBuilder opBuilder = Operation.builder();
        for (Operation op : ops) {
            res.add(opBuilder.flag(op.getFlag()).cellArr1(op.getCellArr1()).rowPos1(op.getRowPos1()).cellArr2(op.getCellArr2()).rowPos2(op.getRowPos2()).build());
        }
        return res;
    }
}
