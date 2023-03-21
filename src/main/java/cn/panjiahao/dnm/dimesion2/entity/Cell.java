package cn.panjiahao.dnm.dimesion2.entity;

import lombok.Builder;
import lombok.Data;

/**
 * 单元格
 *
 * @author panjiahao.cs@foxmail.com
 * @date 2023/3/2 19:02
 */
@Data
@Builder
public class Cell {
    /**
     * 单元格内容
     */
    private String value;
    /**
     * 单元格在表中的行（从1开始）
     */
    private int rowPos;
    /**
     * 单元格在表的行（从1开始）
     */
    private int colPos;
}
