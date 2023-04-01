package cn.panjiahao.xdm.core.algorithm;

import cn.panjiahao.xdm.core.entity.Cell;
import cn.panjiahao.xdm.core.entity.DiffJob;
import cn.panjiahao.xdm.core.entity.Operation;
import cn.panjiahao.xdm.core.entity.Table;
import cn.panjiahao.xdm.core.enums.OpFlag;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.panjiahao.xdm.core.util.CommonUtil.cellArrEquals;
import static cn.panjiahao.xdm.core.util.CommonUtil.cellArrToString;

/**
 * 无表头的表diff
 *
 * @author panjiahao.cs@foxmail.com
 * @date 2023/3/31 23:55
 */
public class NoHeaderTableDiff {
    public static void diff(DiffJob diffJob) {
        TwoDimensionDiff twoDimensionDiff = new TwoDimensionDiff();
        diffJob.setBodyRowEditMethods(twoDimensionDiff.diff(diffJob.getLeftTableBody(), diffJob.getRightTableBody()));
        // 转置再diff得出列的比对结果
        diffJob.setBodyColEditMethods(twoDimensionDiff.diff(diffJob.getLeftTableBodyT(), diffJob.getRightTableBodyT()));
        getDiffRes(diffJob);
    }

    /**
     * 获取比对结果
     *
     * @param diffJob 比对任务
     */
    static void getDiffRes(DiffJob diffJob) {
        Cell[][] leftTableBody = diffJob.getLeftTableBody();
        Cell[][] rightTableBody = diffJob.getRightTableBody();
        List<Operation> rowEditMethod = diffJob.getBodyRowEditMethods().get(0);
        Cell[][] leftTableBodyT = diffJob.getLeftTableBodyT();
        Cell[][] rightTableBodyT = diffJob.getRightTableBodyT();
        List<Operation> colEditMethod = diffJob.getBodyColEditMethods().get(0);
        int headRowNumber = diffJob.getLeftTableHeader().length;


        List<List<Operation>> rowSplitOps = splitOpFromEditMethod(rowEditMethod);
        List<List<Operation>> colSplitOps = splitOpFromEditMethod(colEditMethod);
        List<Operation> rowNoneOps = rowSplitOps.get(0);
        List<Operation> rowAddOrRemoveOps = rowSplitOps.get(1);
        List<Operation> rowReplaceOps = rowSplitOps.get(2);
        List<Operation> rowMoveOps = rowSplitOps.get(3);
        List<Operation> colNoneOps = colSplitOps.get(0);
        List<Operation> colAddOrRemoveOps = colSplitOps.get(1);
        List<Operation> colReplaceOps = colSplitOps.get(2);
        List<Operation> colMoveOps = colSplitOps.get(3);

        // 左表和右表删除的行和列用于统一两表大小进行修改的比对
        Set<Integer> leftTableRemoveRowIndexSet = new HashSet<>();
        Set<Integer> leftTableRemoveColIndexSet = new HashSet<>();
        Set<Integer> rightTableRemoveRowIndexSet = new HashSet<>();
        Set<Integer> rightTableRemoveColIndexSet = new HashSet<>();
        getAddOrRemoveIndex(rowAddOrRemoveOps, leftTableRemoveRowIndexSet, rightTableRemoveRowIndexSet);
        getAddOrRemoveIndex(colAddOrRemoveOps, leftTableRemoveColIndexSet, rightTableRemoveColIndexSet);

        System.out.println("===============最终比对结果==============");

        // 计算移动部分,处理过后 addOrRemoveOps部分增删操作会转换成移动操作
        getMovePart(leftTableBody, rightTableBody, rowAddOrRemoveOps, rowMoveOps, new HashSet<>(leftTableRemoveRowIndexSet), new HashSet<>(rightTableRemoveRowIndexSet));
        getMovePart(leftTableBodyT, rightTableBodyT, colAddOrRemoveOps, colMoveOps, new HashSet<>(leftTableRemoveColIndexSet), new HashSet<>(rightTableRemoveColIndexSet));

        // 检查行/列的变动是否需要输出
        boolean[] displayRowAndCol = checkDisplayRowAndCol(rowNoneOps, rowAddOrRemoveOps, rowReplaceOps, rowMoveOps, leftTableBody.length, rightTableBody.length, colNoneOps, colAddOrRemoveOps, colReplaceOps, colMoveOps, leftTableBodyT.length, rightTableBodyT.length);
        boolean displayRow = displayRowAndCol[0];
        boolean displayCol = displayRowAndCol[1];
        if (displayRow) {
            // 当多行插入或移动到同一行后面时需要给操作排序
            List<Operation> rowAddOrRemoveOrMoveOps = Stream.concat(rowAddOrRemoveOps.stream(), rowMoveOps.stream()).collect(Collectors.toList());
            // tips:这里没有深拷贝，后续依赖rowAddOrRemoveOps和rowMoveOps的操作可能会出bug
            sortInsertAndMovePart(rowAddOrRemoveOrMoveOps, rowNoneOps, rightTableBody);
            // 输出插入、删除、移动部分
            for (Operation op : rowAddOrRemoveOrMoveOps) {
                if (op.flag == OpFlag.INSERT.getVal()) {
                    // headRowNumber表头行数，比对时去掉表头，比对结果的行数需要加上表头行数
                    System.out.printf("插入：在左表第%d行后插入 %s%n", op.rowPos1 + headRowNumber, cellArrToString(op.cellArr1));
                    // printInsertOp(op.rowPos1 + headRowNumber, op.cellArr1,true);
                } else if (op.flag == OpFlag.REMOVE.getVal()) {
                    System.out.printf("删除：删除左表的第%d行 %s%n", op.rowPos1 + headRowNumber, cellArrToString(op.cellArr1));
                    // printRemoveOp(op.rowPos1 + headRowNumber, op.cellArr1,true);
                } else if (op.flag == OpFlag.MOVE.getVal()) {
                    System.out.printf("移动：将左表的第%d行移动到第%d行后面%n", op.rowPos1 + headRowNumber, op.rowPos1New + headRowNumber);
                    // printMoveOp(op.rowPos1 + headRowNumber, op.rowPos1New + headRowNumber,true);
                } else if (op.flag == OpFlag.MOVE_REPLACE.getVal()) {
                    System.out.printf("移动且修改：将左表的第%d行移动到第%d行后面，修改成%s%n", op.rowPos1 + headRowNumber, op.rowPos1New + headRowNumber, cellArrToString(op.cellArr1New));
                    // printMoveReplaceOp(op.rowPos1 + headRowNumber, op.rowPos1New + headRowNumber, op.cellArr1,op.cellArr1New,true);
                }
            }
        }
        if (displayCol) {
            List<Operation> colAddOrRemoveOrMoveOps = Stream.concat(colAddOrRemoveOps.stream(), colMoveOps.stream()).collect(Collectors.toList());
            sortInsertAndMovePart(colAddOrRemoveOrMoveOps, colNoneOps, rightTableBodyT);
            for (Operation op : colAddOrRemoveOrMoveOps) {
                if (op.flag == OpFlag.INSERT.getVal()) {
                    System.out.printf("插入：在左表第%d列后插入 %s%n", op.rowPos1, cellArrToString(op.cellArr1));
                    // printInsertOp(op.rowPos1, op.cellArr1,false);
                } else if (op.flag == OpFlag.REMOVE.getVal()) {
                    System.out.printf("删除：删除左表的第%d列 %s%n", op.rowPos1, cellArrToString(op.cellArr1));
                    // printRemoveOp(op.rowPos1, op.cellArr1,false);
                } else if (op.flag == OpFlag.MOVE.getVal()) {
                    System.out.printf("移动：将左表的第%d列移动到第%d列后面%n", op.rowPos1, op.rowPos1New);
                    // printMoveOp(op.rowPos1, op.rowPos1New, false);
                } else if (op.flag == OpFlag.MOVE_REPLACE.getVal()) {
                    System.out.printf("移动且修改：将左表的第%d列移动到第%d列后面，修改成%s%n", op.rowPos1, op.rowPos1New, cellArrToString(op.cellArr1New));
                    // printMoveReplaceOp(op.rowPos1, op.rowPos1New, op.cellArr1, op.cellArr1New, false);
                }
            }
        }
        // 如果行和列的改动都输出，那么需要去掉左表和右表删除的行和列，逐个比较单元格得出修改；如果只输出行/列，那么输出完整的行/列修改
        if (displayRow && displayCol) {
            // 计算输出修改部分
            getModifyPart(leftTableBody, rightTableBody, leftTableRemoveRowIndexSet, leftTableRemoveColIndexSet, rightTableRemoveRowIndexSet, rightTableRemoveColIndexSet, headRowNumber);
        } else if (displayRow) {
            for (Operation op : rowReplaceOps) {
                System.out.printf("修改：将左表的第%d行 %s 修改成 %s%n", op.rowPos1 + headRowNumber, cellArrToString(op.cellArr1), cellArrToString(op.cellArr2));
                // printReplaceOp(op.rowPos1,op.cellArr1,op.cellArr2,true);
            }
        } else if (displayCol) {
            for (Operation op : colReplaceOps) {
                System.out.printf("修改：将左表的第%d列 %s 修改成 %s%n", op.rowPos1, cellArrToString(op.cellArr1), cellArrToString(op.cellArr2));
                // printReplaceOp(op.rowPos1, op.cellArr1, op.cellArr2, false);
            }
        }
    }

    /**
     * 检查行/列的变动是否需要展示
     *
     * @param rowNoneOps        行一一对应
     * @param rowAddOrRemoveOps 行增删操作
     * @param rowReplaceOps     行编辑方法
     * @param rowMoveOps        行移动操作
     * @param leftRowNumber     行数
     * @param colNoneOps        列一一对应
     * @param colReplaceOps     列编辑方法
     * @param colAddOrRemoveOps 列增删操作
     * @param colMoveOps        列移动操作
     * @param leftColNumber     列数
     * @return 下标0表示行是否需要展示，下标1表示列是否需要展示
     */
    private static boolean[] checkDisplayRowAndCol(List<Operation> rowNoneOps, List<Operation> rowAddOrRemoveOps, List<Operation> rowReplaceOps, List<Operation> rowMoveOps, int leftRowNumber, int rightRowNumber, List<Operation> colNoneOps, List<Operation> colAddOrRemoveOps, List<Operation> colReplaceOps, List<Operation> colMoveOps, int leftColNumber, int rightColNumber) {
        boolean[] displayRowAndCol = new boolean[2];
        displayRowAndCol[0] = checkDiffCanCoverTableDiff(rowNoneOps, rowAddOrRemoveOps, rowReplaceOps, leftRowNumber, rightRowNumber);
        displayRowAndCol[1] = checkDiffCanCoverTableDiff(colNoneOps, colAddOrRemoveOps, colReplaceOps, leftColNumber, rightColNumber);
        // 如果行和列的变动都可以能覆盖表的变动，我们选一个一一对应的多，变动操作少的进行展示
        if (displayRowAndCol[0] && displayRowAndCol[1]) {
            if (rowNoneOps.size() > colNoneOps.size()) {
                displayRowAndCol[1] = false;
            } else if (rowNoneOps.size() == colNoneOps.size()) {
                if (rowAddOrRemoveOps.size() + rowMoveOps.size() + rowReplaceOps.size() <= colAddOrRemoveOps.size() + colMoveOps.size() + colReplaceOps.size()) {
                    displayRowAndCol[1] = false;
                } else {
                    displayRowAndCol[0] = false;
                }
            } else {
                displayRowAndCol[0] = false;
            }
        }
        return displayRowAndCol;
    }

    /**
     * 检查行的变动是否已经足够覆盖表的变动了
     *
     * @return 入参是行/列的编辑方法就输出行/列是否能覆盖表的变动
     */
    private static boolean checkDiffCanCoverTableDiff(List<Operation> noneOps, List<Operation> addOrRemoveOps, List<Operation> replaceOps, int leftLineNumber, int rightLineNumber) {
        int add = 0;
        int remove = 0;
        for (Operation op : addOrRemoveOps) {
            if (op.flag == OpFlag.INSERT.getVal()) {
                add++;
            }
            if (op.flag == OpFlag.REMOVE.getVal()) {
                remove++;
            }
        }
        // 一一对应、修改、移动的行不影响左表行数，把增加的行算上，去掉的行减掉
        leftLineNumber = leftLineNumber + add - remove;
        return leftLineNumber == rightLineNumber;
    }

    /**
     * 计算移动部分
     *
     * @param leftTableCellMatrix      左表
     * @param rightTableCellMatrix     右表
     * @param addOrRemoveOps           增删操作
     * @param moveOps                  移动操作
     * @param leftTableRemoveIndexSet  左表删除的行下标集合
     * @param rightTableRemoveIndexSet 右表删除的行下标集合
     */
    private static void getMovePart(Cell[][] leftTableCellMatrix, Cell[][] rightTableCellMatrix, List<Operation> addOrRemoveOps, List<Operation> moveOps, Set<Integer> leftTableRemoveIndexSet, Set<Integer> rightTableRemoveIndexSet) {
        // 维护被删除行组成的新表和原表之间的行下标对应关系
        Map<Integer, Integer> leftMap = new HashMap<>();
        Map<Integer, Integer> rightMap = new HashMap<>();
        Table leftTable = removedRowIndexSetToTable(leftTableCellMatrix, leftTableRemoveIndexSet, leftMap);
        Table rightTable = removedRowIndexSetToTable(rightTableCellMatrix, rightTableRemoveIndexSet, rightMap);
        TwoDimensionDiff twoDimensionDiff = new TwoDimensionDiff();
        DiffJob diffJob = new DiffJob(leftTable, rightTable);
        diffJob.setBodyRowEditMethods(twoDimensionDiff.diff(diffJob.getLeftTableBody(), diffJob.getRightTableBody()));
        List<Operation> editMethod = diffJob.getBodyRowEditMethods().get(0);
        // 如果新表比对后有对应上的行，需要递归计算移动部分
        boolean hasMovedRow = false;
        Operation.OperationBuilder opBuilder = Operation.builder();
        for (Operation op : editMethod) {
            // 新一轮对应上的行等于原来删除和增加一行，这里把删除和增加合并成移动操作
            if (op.flag == OpFlag.REPLACE.getVal() || op.flag == OpFlag.NONE.getVal()) {
                hasMovedRow = true;
                Operation op1 = findOriginalOp(true, leftMap.get(op.rowPos1), addOrRemoveOps);
                leftTableRemoveIndexSet.remove(leftMap.get(op.rowPos1));
                addOrRemoveOps.remove(op1);
                Operation op2 = findOriginalOp(false, rightMap.get(op.rowPos2), addOrRemoveOps);
                addOrRemoveOps.remove(op2);
                rightTableRemoveIndexSet.remove(rightMap.get(op.rowPos2));
                Operation moveOp = opBuilder.rowPos1(op1.rowPos1).cellArr1(op1.cellArr1).rowPos1New(op2.rowPos1).cellArr1New(op2.cellArr1).rowPos2(op2.rowPos2).build();
                if (cellArrEquals(op1.cellArr1, op2.cellArr1)) {
                    moveOp.setFlag(OpFlag.MOVE.getVal());
                } else {
                    moveOp.setFlag(OpFlag.MOVE_REPLACE.getVal());
                }
                moveOps.add(moveOp);
            }
        }
        if (hasMovedRow) {
            getMovePart(leftTableCellMatrix, rightTableCellMatrix, addOrRemoveOps, moveOps, leftTableRemoveIndexSet, rightTableRemoveIndexSet);
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
     * 当多行移动到同一行后面时需要给移动操作排序, 例如第3行第4行移动到第5行后面，那么读取右表对应左表第5行的行后面的两行判断3和4的先后顺序
     *
     * @param addOrRemoveOrMoveOps 增加删除移动操作
     * @param noneOps              所有操作
     * @param rightTableBody       右表
     */
    private static void sortInsertAndMovePart(List<Operation> addOrRemoveOrMoveOps, List<Operation> noneOps, Cell[][] rightTableBody) {
        // 将插入操作的rowPos1New改成rowPos1，方便和移动操作一起排序
        for (Operation op : addOrRemoveOrMoveOps) {
            if (op.flag == OpFlag.INSERT.getVal()) {
                op.rowPos1New = op.rowPos1;
                op.cellArr1New = op.cellArr1;
            } else if (op.flag == OpFlag.REMOVE.getVal()) {
                op.rowPos1New = -1;
            }
        }
        addOrRemoveOrMoveOps.sort(Comparator.comparingInt(Operation::getRowPos1New));
        for (int i = 0; i < addOrRemoveOrMoveOps.size() - 1; i++) {
            Operation curOp = addOrRemoveOrMoveOps.get(i);
            if (curOp.flag == OpFlag.REMOVE.getVal()) {
                continue;
            }
            int j = i;
            while (j < addOrRemoveOrMoveOps.size() && addOrRemoveOrMoveOps.get(j).getRowPos1New() == curOp.getRowPos1New()) {
                j++;
            }
            if (j == i + 1) {
                continue;
            }
            // [i,j)区间的移动操作的目标行都一致
            int sortRowNum = j - i;
            // 右表中和左表第rowPos1New匹配的行的下标
            int rightTableZeroLineNumber = 0;
            for (Operation op : noneOps) {
                if (op.getRowPos1() == curOp.getRowPos1New()) {
                    rightTableZeroLineNumber = op.rowPos2;
                    break;
                }
            }
            for (int k = 0; k < sortRowNum; k++) {
                Cell[] waitFindRow = rightTableBody[rightTableZeroLineNumber + k];
                for (int q = i; q < j; q++) {
                    if (cellArrEquals(addOrRemoveOrMoveOps.get(q).cellArr1New, waitFindRow)) {
                        Collections.swap(addOrRemoveOrMoveOps, i, q);
                        i++;
                        break;
                    }
                }
            }
        }
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
    private static void getModifyPart(Cell[][] leftTableCellMatrix, Cell[][] rightTableCellMatrix, Set<Integer> leftTableRemoveRowIndexSet, Set<Integer> leftTableRemoveColIndexSet, Set<Integer> rightTableRemoveRowIndexSet, Set<Integer> rightTableRemoveColIndexSet, int headRowNumber) {
        List<List<Cell>> newLeftTable = trimTable(leftTableRemoveRowIndexSet, leftTableRemoveColIndexSet, leftTableCellMatrix);
        List<List<Cell>> newRightTable = trimTable(rightTableRemoveRowIndexSet, rightTableRemoveColIndexSet, rightTableCellMatrix);

        for (int i = 0; i < newLeftTable.size(); i++) {
            List<Cell> leftTableRow = newLeftTable.get(i);
            List<Cell> rightTableRow = newRightTable.get(i);
            for (int j = 0; j < leftTableRow.size(); j++) {
                Cell leftTableCell = leftTableRow.get(j);
                Cell rightTableCell = rightTableRow.get(j);
                if (!leftTableCell.getValue().equals(rightTableCell.getValue())) {
                    System.out.printf("将左表第%d行，第%d列的%s替换成%s%n", leftTableCell.getRowPos() + headRowNumber, leftTableCell.getColPos(), leftTableCell.getValue(), rightTableCell.getValue());
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
        int colNum;
        if (cellMatrix == null || cellMatrix.length == 0) {
            colNum = 0;
        } else {
            colNum = cellMatrix[0].length;
        }
        Cell[][] res = new Cell[rowNum][colNum];
        Cell.CellBuilder cellBuilder = Cell.builder();
        int index = 0;
        for (Integer removedRowIndex : removedRowIndexSet) {
            for (int j = 0; j < colNum; j++) {
                res[index][j] = cellBuilder.rowPos(index + 1).colPos(j + 1).value(cellMatrix[removedRowIndex - 1][j].getValue()).build();
            }
            indexMap.put(++index, removedRowIndex);
        }
        return Table.builder().bodyCells(res).bodyRowNum(rowNum).colNum(colNum).build();
    }

    /**
     * 从编辑方法中分出一一对应、增、删、替换的操作
     *
     * @param editMethod 编辑方法
     */
    static List<List<Operation>> splitOpFromEditMethod(List<Operation> editMethod) {
        List<List<Operation>> splitOps = new ArrayList<>(4);
        List<Operation> noneOps = new ArrayList<>();
        List<Operation> addOrRemoveOps = new ArrayList<>();
        List<Operation> replaceOps = new ArrayList<>();
        List<Operation> moveOps = new ArrayList<>();
        for (Operation op : editMethod) {
            if (op.flag == OpFlag.NONE.getVal()) {
                noneOps.add(op);
            } else if (op.flag == OpFlag.INSERT.getVal() || op.flag == OpFlag.REMOVE.getVal()) {
                addOrRemoveOps.add(op);
            } else if (op.flag == OpFlag.REPLACE.getVal()) {
                replaceOps.add(op);
            }
        }
        splitOps.add(noneOps);
        splitOps.add(addOrRemoveOps);
        splitOps.add(replaceOps);
        splitOps.add(moveOps);
        return splitOps;
    }

    /**
     * 从增删操作中得出左表删除的行和列下标，右边删除的行和列下标
     *
     * @param addOrRemoveOps           增加和删除的操作
     * @param leftTableRemoveIndexSet  左表删除的下标集合
     * @param rightTableRemoveIndexSet 右表删除的下标集合
     */
    static void getAddOrRemoveIndex(List<Operation> addOrRemoveOps, Set<Integer> leftTableRemoveIndexSet, Set<Integer> rightTableRemoveIndexSet) {
        for (Operation op : addOrRemoveOps) {
            // 将左表新增的行和列同时转换为对右边的对应行和列的删除，这样两表就能统一大小进行单元格的修改比对
            if (op.flag == OpFlag.INSERT.getVal()) {
                rightTableRemoveIndexSet.add(op.rowPos2);
            } else {
                leftTableRemoveIndexSet.add(op.rowPos1);
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
