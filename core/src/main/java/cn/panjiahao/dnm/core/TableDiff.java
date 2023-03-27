package cn.panjiahao.dnm.core;

import cn.panjiahao.dnm.core.entity.Cell;
import cn.panjiahao.dnm.core.entity.DiffJob;
import cn.panjiahao.dnm.core.entity.Operation;
import cn.panjiahao.dnm.core.entity.Table;
import cn.panjiahao.dnm.core.enums.OpFlag;

import java.util.*;

import static cn.panjiahao.dnm.core.util.CommonUtil.*;

/**
 * @author panjiahao.cs@foxmail.com
 * @date 2023/3/21 15:03
 */
public class TableDiff {

    public static void diff(DiffJob diffJob) {
        // System.out.println("将每一行看成一个元素进行比对：");
        TwoDimensionDiff.diff(diffJob);
        // 转置再diff得出列的比对结果
        DiffJob diffJobT = new DiffJob(transposeMatrix(diffJob.getLeftTable()), transposeMatrix(diffJob.getRightTable()));
        // System.out.println("将每一列看成一个元素进行比对：");
        TwoDimensionDiff.diff(diffJobT);
        getDiffRes(diffJob, diffJobT);
    }

    /**
     * 获取比对结果
     *
     * @param diffJob 比对任务
     */
    static void getDiffRes(DiffJob diffJob, DiffJob diffJobT) {
        Cell[][] leftTableCellMatrix = diffJob.getLeftTable().getCells();
        Cell[][] rightTableCellMatrix = diffJob.getRightTable().getCells();
        List<Operation> rowEditMethod = diffJob.getRowEditMethods().get(0);
        Cell[][] leftTableCellMatrixT = diffJobT.getLeftTable().getCells();
        Cell[][] rightTableCellMatrixT = diffJobT.getRightTable().getCells();
        List<Operation> colEditMethod = diffJobT.getRowEditMethods().get(0);

        List<Operation> rowAddOrRemoveOps = new ArrayList<>();
        List<Operation> colAddOrRemoveOps = new ArrayList<>();
        // 左表和右表删除的行和列用于统一两表大小进行修改的比对
        Set<Integer> leftTableRemoveRowIndexSet = new HashSet<>();
        Set<Integer> leftTableRemoveColIndexSet = new HashSet<>();
        Set<Integer> rightTableRemoveRowIndexSet = new HashSet<>();
        Set<Integer> rightTableRemoveColIndexSet = new HashSet<>();

        getAddOrRemoveIndexFromEditMethod(rowEditMethod, rowAddOrRemoveOps, leftTableRemoveRowIndexSet, rightTableRemoveRowIndexSet);
        getAddOrRemoveIndexFromEditMethod(colEditMethod, colAddOrRemoveOps, leftTableRemoveColIndexSet, rightTableRemoveColIndexSet);

        System.out.println("===============最终比对结果==============");
        // 计算移动部分,处理过后 addOrRemoveOps还包含修改的操作
        getMovePart(leftTableCellMatrix, rightTableCellMatrix, rowAddOrRemoveOps, new HashSet<>(leftTableRemoveRowIndexSet), new HashSet<>(rightTableRemoveRowIndexSet));
        getMovePart(leftTableCellMatrixT, rightTableCellMatrixT, colAddOrRemoveOps, new HashSet<>(leftTableRemoveColIndexSet), new HashSet<>(rightTableRemoveColIndexSet));
        // 计算修改部分
        getModifyPart(leftTableCellMatrix, rightTableCellMatrix, leftTableRemoveRowIndexSet, leftTableRemoveColIndexSet, rightTableRemoveRowIndexSet, rightTableRemoveColIndexSet);
        // 插入、删除部分
        for (Operation op : rowAddOrRemoveOps) {
            // 表头行数，比对时去掉表头，比对结果的行数需要加上表头行数
            int tableHeadRowNum = 1;
            if (op.flag == OpFlag.INSERT.getVal()) {
                System.out.printf("插入：在左表第%d行后插入 %s%n", op.rowPos1+tableHeadRowNum, printCellArr(op.cellArr1));
            } else if (op.flag == OpFlag.REMOVE.getVal()) {
                System.out.printf("删除：删除左表的第%d行 %s%n", op.rowPos1+tableHeadRowNum, printCellArr(op.cellArr1));
            } else if (op.flag == OpFlag.MOVE.getVal()) {
                System.out.printf("移动：将左表的第%d行移动到第%d行后面%n", op.rowPos1+tableHeadRowNum, op.rowPos1New+tableHeadRowNum);
            } else if (op.flag == OpFlag.MOVE_REPLACE.getVal()) {
                System.out.printf("移动且修改：将左表的第%d行移动到第%d行后面，修改成%s%n", op.rowPos1+tableHeadRowNum, op.rowPos1New+tableHeadRowNum, printCellArr(op.cellArr1New));
            }
        }
        for (Operation op : colAddOrRemoveOps) {
            if (op.flag == OpFlag.INSERT.getVal()) {
                System.out.printf("插入：在左表第%d列后插入 %s%n", op.rowPos1, printCellArr(op.cellArr1));
            } else if (op.flag == OpFlag.REMOVE.getVal()) {
                System.out.printf("删除：删除左表的第%d列 %s%n", op.rowPos1, printCellArr(op.cellArr1));
            } else if (op.flag == OpFlag.MOVE.getVal()) {
                System.out.printf("移动：将左表的第%d列移动到第%d列后面%n", op.rowPos1, op.rowPos1New);
            } else if (op.flag == OpFlag.MOVE_REPLACE.getVal()) {
                System.out.printf("移动且修改：将左表的第%d列移动到第%d列后面，修改成%s%n", op.rowPos1, op.rowPos1New, printCellArr(op.cellArr1New));
            }
        }
    }

    /**
     * 计算移动部分
     *
     * @param leftTableCellMatrix      左表
     * @param rightTableCellMatrix     右表
     * @param originalAddOrRemoveOps   第一次比对生成的行编辑方法
     * @param leftTableRemoveIndexSet  左表删除的行下标集合
     * @param rightTableRemoveIndexSet 右表删除的行下标集合
     */
    private static void getMovePart(Cell[][] leftTableCellMatrix, Cell[][] rightTableCellMatrix, List<Operation> originalAddOrRemoveOps, Set<Integer> leftTableRemoveIndexSet, Set<Integer> rightTableRemoveIndexSet) {
        // 维护被删除行组成的新表和原表之间的行下标对应关系
        Map<Integer, Integer> leftMap = new HashMap<>();
        Map<Integer, Integer> rightMap = new HashMap<>();
        Table leftTable = removedRowIndexSetToTable(leftTableCellMatrix, leftTableRemoveIndexSet, leftMap);
        Table rightTable = removedRowIndexSetToTable(rightTableCellMatrix, rightTableRemoveIndexSet, rightMap);
        DiffJob diffJob = new DiffJob(leftTable, rightTable);
        TwoDimensionDiff.diff(diffJob);
        List<Operation> editMethod = diffJob.getRowEditMethods().get(0);
        // 如果新表比对后有对应上的行，需要递归计算移动部分
        boolean hasMovedRow = false;
        for (Operation op : editMethod) {
            // 新一轮对应上的行等于原来删除和增加一行，这里把删除操作改为移动操作，增加操作改成什么都不做
            if (op.flag == OpFlag.REPLACE.getVal() || op.flag == OpFlag.NONE.getVal()) {
                hasMovedRow = true;
                Operation op1 = findOriginalOp(true, leftMap.get(op.rowPos1), originalAddOrRemoveOps);
                leftTableRemoveIndexSet.remove(leftMap.get(op.rowPos1));
                Operation op2 = findOriginalOp(false, rightMap.get(op.rowPos2), originalAddOrRemoveOps);
                rightTableRemoveIndexSet.remove(rightMap.get(op.rowPos2));
                op1.setRowPos1New(op2.rowPos1);
                op1.setCellArr1New(op2.cellArr1);
                if (cellArrEquals(op1.cellArr1, op2.cellArr1)) {
                    op1.setFlag(OpFlag.MOVE.getVal());
                } else {
                    op1.setFlag(OpFlag.MOVE_REPLACE.getVal());
                }
                op2.flag = OpFlag.NONE.getVal();
            }
        }
        if (hasMovedRow) {
            getMovePart(leftTableCellMatrix, rightTableCellMatrix, originalAddOrRemoveOps, leftTableRemoveIndexSet, rightTableRemoveIndexSet);
        }
    }

    /**
     * 根据Op的行下标找到对应的Op
     *
     * @param isLeftOrRight          true表示左表，false表示右表
     * @param rowPos                 行下标
     * @param originalAddOrRemoveOps 第一次比对的增删操作
     * @return Op
     */
    private static Operation findOriginalOp(boolean isLeftOrRight, int rowPos, List<Operation> originalAddOrRemoveOps) {
        for (Operation addOrRemoveOp : originalAddOrRemoveOps) {
            if (isLeftOrRight) {
                if (addOrRemoveOp.rowPos1 == rowPos) {
                    return addOrRemoveOp;
                }
            } else {
                if (addOrRemoveOp.rowPos2 == rowPos) {
                    return addOrRemoveOp;
                }
            }
        }
        return null;
    }

    /**
     * 去掉左表和右表删除的行和列，剩下的单元格逐个比较得出修改结果
     *
     * @param leftTableCellMatrix         左表
     * @param rightTableCellMatrix        右表
     * @param leftTableRemoveRowIndexSet  左表删除的行下标
     * @param leftTableRemoveColIndexSet  左表删除的列下标
     * @param rightTableRemoveRowIndexSet 右表删除的行下标
     * @param rightTableRemoveColIndexSet 右表删除的列下标
     */
    private static void getModifyPart(Cell[][] leftTableCellMatrix, Cell[][] rightTableCellMatrix, Set<Integer> leftTableRemoveRowIndexSet, Set<Integer> leftTableRemoveColIndexSet, Set<Integer> rightTableRemoveRowIndexSet, Set<Integer> rightTableRemoveColIndexSet) {
        List<List<Cell>> newLeftTable = trimTable(leftTableRemoveRowIndexSet, leftTableRemoveColIndexSet, leftTableCellMatrix);
        List<List<Cell>> newRightTable = trimTable(rightTableRemoveRowIndexSet, rightTableRemoveColIndexSet, rightTableCellMatrix);
        int tableHeadNum = 1;
        for (int i = 0; i < newLeftTable.size(); i++) {
            List<Cell> leftTableRow = newLeftTable.get(i);
            List<Cell> rightTableRow = newRightTable.get(i);
            for (int j = 0; j < leftTableRow.size(); j++) {
                Cell leftTableCell = leftTableRow.get(j);
                Cell rightTableCell = rightTableRow.get(j);
                if (!leftTableCell.getValue().equals(rightTableCell.getValue())) {
                    System.out.printf("将左表第%d行，第%d列的%s替换成%s%n", leftTableCell.getRowPos()+tableHeadNum, leftTableCell.getColPos(), leftTableCell.getValue(), rightTableCell.getValue());
                }
            }
        }
    }

    /**
     * 删除的行组成一个新表
     *
     * @param cellMatrix         原表
     * @param removedRowIndexSet 删除的行集合
     * @param indexMap           删除的行的下标映射到新表的下标（从1开始）
     * @return 新表
     */
    static Table removedRowIndexSetToTable(Cell[][] cellMatrix, Set<Integer> removedRowIndexSet, Map<Integer, Integer> indexMap) {
        int rowNum = removedRowIndexSet.size();
        int colNum = cellMatrix[0].length;
        Cell[][] res = new Cell[rowNum][colNum];
        Cell.CellBuilder cellBuilder = Cell.builder();
        int index = 0;
        for (Integer removedRowIndex : removedRowIndexSet) {
            for (int j = 0; j < colNum; j++) {
                res[index][j] = cellBuilder.rowPos(index + 1).colPos(j + 1).value(cellMatrix[removedRowIndex - 1][j].getValue()).build();
            }
            indexMap.put(++index, removedRowIndex);
        }
        return Table.builder().cells(res).rowNum(rowNum).colNum(colNum).build();
    }

    /**
     * 从编辑方法中得出左表删除的行和列下标，右边删除的行和列下标
     *
     * @param editMethod               编辑方法
     * @param addOrRemoveOps           增加和删除的操作
     * @param leftTableRemoveIndexSet  左表删除的下标集合
     * @param rightTableRemoveIndexSet 右表删除的下标集合
     */
    static void getAddOrRemoveIndexFromEditMethod(List<Operation> editMethod, List<Operation> addOrRemoveOps, Set<Integer> leftTableRemoveIndexSet, Set<Integer> rightTableRemoveIndexSet) {
        for (Operation op : editMethod) {
            if (op.flag == OpFlag.INSERT.getVal() || op.flag == OpFlag.REMOVE.getVal()) {
                addOrRemoveOps.add(op);
                // 将左表新增的行和列同时转换为对右边的对应行和列的删除，这样两表就能统一大小进行单元格的修改比对
                if (op.flag == OpFlag.INSERT.getVal()) {
                    rightTableRemoveIndexSet.add(op.rowPos2);
                } else {
                    leftTableRemoveIndexSet.add(op.rowPos1);
                }
            }
        }
    }

    /**
     * 删除表的指定行和列
     *
     * @param removeRowIndexSet 删除的行集合
     * @param removeColIndexSet 删除的列集合
     * @param cellMatrix        表
     * @return 新表
     */
    static List<List<Cell>> trimTable(Set<Integer> removeRowIndexSet, Set<Integer> removeColIndexSet, Cell[][] cellMatrix) {
        List<List<Cell>> newTable = new ArrayList<>();

        for (int i = 0; i < cellMatrix.length; i++) {
            if (removeRowIndexSet.contains(i + 1)) {
                continue;
            }
            List<Cell> row = new ArrayList<>();
            for (int j = 0; j < cellMatrix[i].length; j++) {
                if (removeColIndexSet.contains(j + 1)) {
                    continue;
                } else {
                    row.add(cellMatrix[i][j]);
                }
            }
            newTable.add(row);
        }
        return newTable;
    }
}
