package io.github.yogboot.api.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * flink节点配置
 */
@Data
@ConfigurationProperties("deploy")
public class DeployProperties {

    /*
     * 访问密钥
     *

     */
    private String secret = "deploy-key";

    /*
     * 检查连接数据
     *

     */
    private Boolean checkServers = false;

    /*
     * 检查deploy的服务是否可以正常使用
     *

     */
    private Boolean checkEnv = true;

    /*
     * 配置服务器信息
     *

     */
    private Map<String, ServerInfo> servers;

}
