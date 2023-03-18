package io.github.yogboot.server.config;

import io.github.yogboot.api.properties.DeployProperties;
import io.github.yogboot.server.utils.PrintUtils;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(DeployProperties.class)
public class DeployConfig {

    private final ConfigurableApplicationContext context;

    @Bean
    @ConditionalOnProperty(prefix = "deploy", name = "check-env", havingValue = "true")
    public void checkEnvironment() {
        String flinkHomePath = System.getenv("FLINK_HOME");
        if (Strings.isEmpty(flinkHomePath)) {
            PrintUtils.printErrorLog("请配置FLINK_HOME环境变量");
            context.close();
            return;
        }
        String deployHomePath = System.getenv("DEPLOY_HOME");
        if (Strings.isEmpty(deployHomePath)) {
            PrintUtils.printErrorLog("请配置DEPLOY_HOME环境变量");
            context.close();
            return;
        }
        String yarnConfDir = System.getenv("HADOOP_HOME");
        if (Strings.isEmpty(yarnConfDir)) {
            PrintUtils.printErrorLog("请配置HADOOP_HOME环境变量");
            context.close();
        }
    }
}
