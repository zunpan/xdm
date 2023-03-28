package cn.panjiahao.dnm.core.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static cn.panjiahao.dnm.core.util.CommonUtil.transposeTableBody;

/**
 * @author panjiahao.cs@foxmail.com
 * @date 2023/3/21 12:03
 */
@NoArgsConstructor
@Data
public class DiffJob {
    /**
     * 左表和右表
     */
    Cell[][] leftTableHead,rightTableHead;
    Cell[][] leftTableBody,leftTableBodyT,rightTableBody,rightTableBodyT;

    public DiffJob(Table leftTable, Table rightTable) {
        this.leftTableBody = leftTable.getBodyCells();
        this.leftTableBodyT = transposeTableBody(this.leftTableBody);
        this.rightTableBody = rightTable.getBodyCells();
        this.rightTableBodyT = transposeTableBody(this.rightTableBody);
        this.leftTableHead = leftTable.getHeadCells();
        this.rightTableHead = rightTable.getHeadCells();
    }
    /**
     * 关于表体行的所有编辑方法
     */
    List<List<Operation>> bodyRowEditMethods;
    /**
     * 关于表体列的所有编辑方法
     */
    List<List<Operation>> bodyColEditMethods;
}
