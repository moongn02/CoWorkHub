package cn.moongn.coworkhub.common.exception;

import cn.moongn.coworkhub.common.api.ResultCode;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final ResultCode resultCode;

    public ApiException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    public ApiException(String message) {
        super(message);
        this.resultCode = ResultCode.FAILED;
    }
} 