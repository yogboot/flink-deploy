package io.github.deploy.api.pojo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.deploy.api.pojo.flink.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeployData {

    private List<String> columnNames;

    private List<List<String>> dataList;

    private String flinkJobId;

    private JobStatusDto jobInfo;

    private String jobLog;

    private String deployLog;

    private List<JobStatusDto> jobInfoList;

    private String executeId;

    private String applicationId;

    private String finalStatus;

    private String yarnState;

    private JobStatus jobStatus;

    private List<String> rootExceptions;

    private List<String> jobManagerLogs;

    private List<String> taskManagerLogs;

    private String webInterfaceURL;
}
