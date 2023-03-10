package io.github.yogboot.api.pojo;

import lombok.Data;

@Data
public class RequestDto {

    private String flinkSql;

    private String applicationId;

    private String flinkJobId;

    private int slotsPerTM = 1;

    private int parallelism = 1;

    private int streamMode = 1;

}
