package io.github.yogboot.api.pojo.dto;

import lombok.Data;

import java.util.List;

@Data
public class JobStatusResultDto {

    private List<JobStatusDto> jobs;
}
