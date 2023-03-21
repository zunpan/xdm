package cn.panjiahao.dnm.dimesion2.entity;

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
     * 左表和右表，以及它们的转置
     */
    Table leftTable, rightTable;

    public DiffJob(Table leftTable, Table rightTable) {
        this.leftTable = leftTable;
        this.rightTable = rightTable;
    }

    /**
     * 关于行的所有编辑方法
     */
    List<List<Operation>> rowEditMethods;
}
