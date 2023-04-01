package cn.panjiahao.xdm.base.exception;

import cn.panjiahao.xdm.base.enums.Code;
import lombok.Data;

/**
 * @author panjiahao
 */
@Data
public class BizException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    protected int errorCode;
    protected String errorMsg;

    public BizException() {
        super();
    }

    public BizException(Code code) {
        super(code.getMsg());
        this.errorCode = code.getCode();
        this.errorMsg = code.getMsg();
    }

    public BizException(Code code, Throwable cause) {
        super(code.getMsg(), cause);
        this.errorCode = code.getCode();
        this.errorMsg = code.getMsg();
    }

    public BizException(String errorMsg) {
        super(errorMsg);
        this.errorMsg = errorMsg;
    }

    public BizException(int errorCode, String errorMsg) {
        super(errorMsg);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public BizException(int errorCode, String errorMsg, Throwable cause) {
        super(errorMsg, cause);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

}
