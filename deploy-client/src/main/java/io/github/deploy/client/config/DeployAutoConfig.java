package io.github.deploy.client.config;

import io.github.deploy.api.constant.MsgConstants;
import io.github.deploy.api.constant.SecurityConstants;
import io.github.deploy.api.constant.JobUrlsConstants;
import io.github.deploy.api.pojo.DeployResponse;
import io.github.deploy.api.properties.DeployProperties;
import io.github.deploy.api.properties.ServerInfo;
import io.github.deploy.api.utils.HttpUtils;
import io.github.deploy.client.template.DeployTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableConfigurationProperties({DeployProperties.class, ServerInfo.class})
@RequiredArgsConstructor
public class DeployAutoConfig {

    private final DeployProperties deployProperties;

    @Bean("deployTemplate")
    public DeployTemplate deployTemplate() {
        return new DeployTemplate(deployProperties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "deploy.servers.default", name = {"host", "port", "key"})
    public void checkServerStatus() {
        if (!deployProperties.getCheckServers()) {
            return;
        }
        if (deployProperties.getCheckServers() == null) {
            return;
        }
        deployProperties.getServers().forEach((k, v) -> {
            try {
                String heartCheckUrl = String.format(JobUrlsConstants.BASE_URL + JobUrlsConstants.HEART_CHECK_URL, v.getHost(), v.getPort());
                Map<String, String> headers = new HashMap<>();
                headers.put(SecurityConstants.HEADER_AUTHORIZATION, v.getKey());
                DeployResponse deployResponse = HttpUtils.doGet(heartCheckUrl, headers, DeployResponse.class);
                if (MsgConstants.SUCCESS_CODE.equals(deployResponse.getCode())) {
                    System.out.println(k + ":" + v.getHost() + ":[ok]");
                } else {
                    System.out.println(k + ":" + v.getHost() + ":[error]");
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                System.out.println(k + ":" + v.getHost() + ":[error]");
            }
        });
    }
}
