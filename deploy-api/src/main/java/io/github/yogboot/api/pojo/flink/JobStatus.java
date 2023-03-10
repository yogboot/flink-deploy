package io.github.yogboot.api.pojo.flink;

import lombok.Data;

@Data
public class JobStatus {

    private String jid;

    private String state;
}
