package cn.panjiahao.xdm.core.enums;

import lombok.AllArgsConstructor;

/**
 * 计算最小编辑距离时，保存状态转移来源
 *
 * @author panjiahao.cs@foxmail.com
 * @date 2023/3/2 19:38
 */
@AllArgsConstructor
public enum FROM {
    INIT(0),
    LEFT(1),
    UP(2),
    LEFT_UP_REPLACE(4),
    LEFT_UP_COPY(8);
    private int val;

    public int getVal() {
        return val;
    }
}
