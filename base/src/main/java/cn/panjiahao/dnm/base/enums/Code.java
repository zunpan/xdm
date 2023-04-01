package cn.panjiahao.dnm.base.enums;

import lombok.Getter;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * 该枚举类表示响应体的响应码，响应头的响应码可以使用HttpStatus枚举类
 *
 * @author panjiahao
 */
@Getter
public enum Code {

    //通用部分
    SUCCESS(200, "成功"),
    NOT_FOUND(404, "404"),

    /*----------业务模块 6000~6999----------*/
    ROW_SPAN_OVER_TABLE_SPAN(6000, "存在行宽度超出表的宽度的情况"),
    COLUMN_NAME_IS_NULL(6001, "存在列名为空的情况");

    private final int code;
    private final String msg;

    Code(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * Implementing a fromCode method on an enum type
     */
    private static final Map<Integer, Code> INT_TO_ENUM = Stream.of(values()).collect(toMap(Code::getCode, code -> code));

    /**
     * Returns Code for code, if any
     *
     * @param code 响应码
     * @return 响应码对应的响应枚举
     */
    public static Optional<Code> fromCode(int code) {
        return Optional.ofNullable(INT_TO_ENUM.get(code));
    }
}

