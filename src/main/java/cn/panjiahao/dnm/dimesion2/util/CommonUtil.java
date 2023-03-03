package cn.panjiahao.dnm.dimesion2.util;

import cn.panjiahao.dnm.dimesion2.TwoDimensionDiff1;
import cn.panjiahao.dnm.dimesion2.entity.Cell;
import cn.panjiahao.dnm.dimesion2.entity.Column;
import cn.panjiahao.dnm.dimesion2.entity.Row;
import cn.panjiahao.dnm.dimesion2.entity.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 通用工具
 * @author panjiahao.cs@foxmail.com
 * @date 2023/3/2 19:43
 */
public class CommonUtil {

    /**
     * 矩阵转置
     * @param matrix 矩阵
     * @return  转置矩阵
     */
    public static Cell[][] transposeMatrix(Cell[][] matrix) {
        int n = matrix.length;
        int m = matrix[0].length;

        Cell[][] res = new Cell[m][n];

        for(int i=0;i<m;i++){
            for(int j=0;j<n;j++){
                res[i][j] = matrix[j][i];
            }
        }
        return res;
    }

    /**
     * 比较两个Cell数组的内容是否完全一样
     * @param cellArr1 Cell数组1
     * @param cellArr2 Cell数组2
     * @return  是否完全一样
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
        for(int i=0;i<len;i++){
            if (!cellArr1[i].getValue().equals(cellArr2[i].getValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 打印Cell数组的内容
     * @param cellArr Cell数组
     */
    public static String printCellArr(Cell[] cellArr) {
        if (cellArr == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder("[");
        int i = 0;
        for (; i < cellArr.length-1; i++) {
            sb.append("\"").append(cellArr[i].getValue()).append("\"").append(",");
        }
        sb.append("\"").append(cellArr[i].getValue()).append("\"").append("]");

        return sb.toString();
    }

    /**
     * 打印Cell矩阵的内容
     * @param matrix Cell矩阵
     */
    public static void printCellMatrix(Cell[][] matrix) {
        if (matrix == null) {
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

    /**
     * 输入矩阵
     * @param rowNum 行数
     * @param colNum 列数
     * @return 输入矩阵
     */
    public static String[][] inputMatrix(Scanner sc,int rowNum, int colNum) {
        String[][] table = new String[rowNum][colNum];
        for(int i=0;i<rowNum;i++){
            for(int j=0;j<colNum;j++){
                table[i][j] = sc.next();
            }
        }
        return table;
    }

    /**
     * 字符串数组转成Table
     * @param strMatrix 字符串数组
     * @return Table
     */
    public static Table strMatrixToTable(String[][] strMatrix) {
        int rowNum = strMatrix.length;
        int colNum = strMatrix[0].length;
        Cell[][] cells = new Cell[rowNum][colNum];
        Cell.CellBuilder cellBuilder = Cell.builder();
        Table.TableBuilder tableBuilder = Table.builder();

        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                cells[i][j] = cellBuilder.rowPos(i + 1).colPos(j + 1).value(strMatrix[i][j]).build();
            }
        }
        return tableBuilder.rowNum(rowNum).colNum(colNum).cells(cells).build();
    }


    public static List<TwoDimensionDiff1.Operation> deepCopy(List<TwoDimensionDiff1.Operation> ops) {
        List<TwoDimensionDiff1.Operation> res = new ArrayList<>();
        TwoDimensionDiff1.Operation.OperationBuilder opBuilder = TwoDimensionDiff1.Operation.builder();
        for (TwoDimensionDiff1.Operation op : ops) {
            res.add(opBuilder.flag(op.getFlag()).cellArr1(op.getCellArr1()).rowPos1(op.getRowPos1()).cellArr2(op.getCellArr2()).rowPos2(op.getRowPos2()).build());
        }
        return res;
    }
}
