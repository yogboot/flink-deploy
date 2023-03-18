package io.github.yogboot.common.response;

import io.github.yogboot.api.pojo.base.BaseResponse;
import lombok.Getter;
import lombok.Setter;

public class SuccessException extends RuntimeException {

	@Setter @Getter
    private BaseResponse<Object> baseResponse;

	public SuccessException(BaseResponse<Object> baseResponse) {
		this.baseResponse = baseResponse;
	}
}
