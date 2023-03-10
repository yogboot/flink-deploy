package io.github.yogboot.api.pojo;

import io.github.yogboot.api.menu.Template;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeployRequest {

    private String jobId;

    private String executeId;

    private String name;

    private String json;

    private String java;

    private String sql;

    private Template template;

    private int masterMemoryMB = 1024;

    private int taskManagerMemoryMB = 1024;

    private int slotsPerTaskManager = 1;

    private int parallelism = 1;

    private String pluginName;

    private String pluginMainClass;

    private List<String> pluginArguments;

    private String applicationId;

    private int streamMode = 1;

}
