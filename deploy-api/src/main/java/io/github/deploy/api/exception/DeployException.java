package io.github.deploy.api.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeployException extends AbstractException {

    public DeployException(ExceptionEnum abstractExceptionEnum) {
        super(abstractExceptionEnum);
    }

    public DeployException(String code, String msg) {
        super(code, msg);
    }

    public DeployException(String msg) {
        super(msg);
    }

}
