package cn.panjiahao.dnm.core.entity;

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
     * 行数
     */
    int rowNum;
    /**
     * 列数
     */
    int colNum;
    /**
     * 表头单元格
     */
    Cell[][] headerCells;
    /**
     * 除表头外所有单元格
     */
    Cell[][] cells;

}
