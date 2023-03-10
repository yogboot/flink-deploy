package io.github.yogboot.api.pojo;

import io.github.yogboot.api.pojo.base.BaseResponse;
import io.github.yogboot.api.pojo.dto.DeployData;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DeployResponse extends BaseResponse<DeployData> {

    public DeployResponse(String code, String msg, DeployData deployData) {
        super(code, msg, deployData);
    }

    public DeployResponse(String code, String msg) {
        super(code, msg);
    }
}
