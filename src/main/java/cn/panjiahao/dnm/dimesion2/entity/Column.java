package cn.panjiahao.dnm.dimesion2.entity;

import lombok.Builder;
import lombok.Data;


/**
 * 列，一列有多个单元格
 *
 * @author panjiahao.cs@foxmail.com
 * @date 2023/3/2 19:11
 */
@Data
@Builder
public class Column {
    /**
     * 一组单元格
     */
    private Cell[] cells;
    /**
     * 在表中的列（从1开始）
     */
    private int colPos;
}
