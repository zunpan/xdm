package cn.panjiahao.xdm.core.entity;

import lombok.Builder;
import lombok.Data;

/**
 * 表，一张表有多行多列
 *
 * @author panjiahao.cs@foxmail.com
 * @date 2023/3/2 19:15
 */
@Data
@Builder
public class Table {
    /**
     * 表头行数
     */
    int headerRowNum;
    /**
     * 表体行数
     */
    int bodyRowNum;
    /**
     * 列数
     */
    int colNum;
    /**
     * 表头单元格
     */
    Cell[][] headerCells;
    /**
     * 表体单元格
     */
    Cell[][] bodyCells;

}
