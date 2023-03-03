package cn.panjiahao.dnm.dimesion2.entity;

import cn.panjiahao.dnm.dimesion2.entity.Column;
import cn.panjiahao.dnm.dimesion2.entity.Row;
import lombok.Builder;
import lombok.Data;

/**
 * 表，一张表有多行多列
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
     * 所有行
     */
    Row[] rows;
    /**
     * 所有列
     */
    Column[] columns;
    /**
     * 所有单元格
     */
    Cell[][] cells;
}
