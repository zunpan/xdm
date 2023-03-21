package cn.panjiahao.dnm.dimesion2.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Operation {
    /**
     * 操作方式 0 什么都不干 1 插入 2 删除 3 替换 4 只移动 5 移动且修改
     */
    public int flag;
    /**
     * 操作的左表的行
     */
    public Cell[] cellArr1;
    /**
     * 在左表中的操作下标，针对插入和删除操作
     */
    public int rowPos1;
    /**
     * 针对移动操作，移动到的新行下标
     */
    public int rowPos1New;
    /**
     * 针对移动操作，新行内容（实际上就是cellArr2）,rowPos1的cellArr1移动到rowPos1New的cellArr2
     */
    public Cell[] cellArr1New;
    /**
     * 操作的右表的行, 针对替换操作, cellArr1 替换为 cellArr2
     */
    public Cell[] cellArr2;
    /**
     * 在右表中的操作下标，针对替换操作
     */
    public int rowPos2;
}