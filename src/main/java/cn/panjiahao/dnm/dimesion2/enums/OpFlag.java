package cn.panjiahao.dnm.dimesion2.enums;

import lombok.AllArgsConstructor;

/**
 * @author panjiahao.cs@foxmail.com
 * @date 2023/3/2 22:00
 */
@AllArgsConstructor
public enum OpFlag {
    NONE(0),
    INSERT(1),
    REMOVE(2),
    REPLACE(3),
    MOVE(4),
    MOVE_REPLACE(5);

    int val;

    public int getVal() {
        return val;
    }
}
