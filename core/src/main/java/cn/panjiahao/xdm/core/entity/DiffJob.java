package cn.panjiahao.xdm.core.entity;

import cn.panjiahao.xdm.core.util.CommonUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    Cell[][] leftTableHeader, rightTableHeader, leftTableHeaderT, rightTableHeaderT;
    Cell[][] leftTableBody, leftTableBodyT, rightTableBody, rightTableBodyT;

    public DiffJob(Table leftTable, Table rightTable) {
        this.leftTableBody = leftTable.getBodyCells();
        this.leftTableBodyT = CommonUtil.transposeMatrix(this.leftTableBody);
        this.rightTableBody = rightTable.getBodyCells();
        this.rightTableBodyT = CommonUtil.transposeMatrix(this.rightTableBody);
        this.leftTableHeader = leftTable.getHeaderCells();
        this.leftTableHeaderT = CommonUtil.transposeMatrix(this.leftTableHeader);
        this.rightTableHeader = rightTable.getHeaderCells();
        this.rightTableHeaderT = CommonUtil.transposeMatrix(this.rightTableHeader);

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
