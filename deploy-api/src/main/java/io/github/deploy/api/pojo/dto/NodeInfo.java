package io.github.deploy.api.pojo.dto;

import io.github.deploy.api.menu.DataFormat;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NodeInfo {

    private String nodeId;

    private String name;

    private String brokerList;

    private String topic;

    private String zookeeper;

    private DataFormat dataFormat;
}
