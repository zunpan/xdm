package cn.panjiahao.dnm.dimesion2;

import cn.panjiahao.dnm.dimesion2.entity.DiffJob;

import java.util.Scanner;

import static cn.panjiahao.dnm.dimesion2.util.CommonUtil.inputMatrix;
import static cn.panjiahao.dnm.dimesion2.util.CommonUtil.strMatrixToTable;

/**
 * @author panjiahao.cs@foxmail.com
 * @date 2023/3/21 14:18
 */
public class Test {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int rowNum1, colNum1, rowNum2, colNum2;
        rowNum1 = sc.nextInt();
        colNum1 = sc.nextInt();
        String[][] strMatrix1 = inputMatrix(sc, rowNum1, colNum1);
        rowNum2 = sc.nextInt();
        colNum2 = sc.nextInt();
        String[][] strMatrix2 = inputMatrix(sc, rowNum2, colNum2);

        DiffJob diffJob = new DiffJob(strMatrixToTable(strMatrix1), strMatrixToTable(strMatrix2));
        TableDiff.diff(diffJob);
    }
}
