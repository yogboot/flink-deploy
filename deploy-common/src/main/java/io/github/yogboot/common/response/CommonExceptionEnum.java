package io.github.yogboot.common.response;

import io.github.yogboot.api.exception.ExceptionEnum;
import lombok.Getter;

public enum CommonExceptionEnum implements ExceptionEnum {

    KEY_IS_NULL("50005", "key is null"),

    KEY_IS_ERROR("50006", "key is error"),;

    @Getter
    private final String code;

    @Getter
    private final String msg;

    CommonExceptionEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
