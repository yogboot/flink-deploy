package io.github.deploy.api.pojo.dto;

import lombok.Data;

@Data
public class JobStatusDto {

    private String jid;

    private String name;

    private String state;

    private String duration;
}
