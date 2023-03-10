package io.github.yogboot.api.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "deploy.servers")
public class ServerInfo {

    private String host;

    private int port;

    private String key;
}
