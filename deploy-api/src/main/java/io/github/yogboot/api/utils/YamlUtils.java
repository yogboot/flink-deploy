package io.github.yogboot.api.utils;

import io.github.yogboot.api.constant.JobUrlsConstants;
import io.github.yogboot.api.exception.DeployException;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
public class YamlUtils {

    public static String getFlinkJobHistoryUrl() {
        Yaml yaml = new Yaml();
        String flinkConfigPath = System.getenv("FLINK_HOME") + File.separator + "conf" + File.separator + "flink-conf.yaml";
        try {
            InputStream inputStream = Files.newInputStream(Paths.get(flinkConfigPath));
            Map<String, String> load = yaml.load(inputStream);
            String address = load.get("historyserver.web.address");
            if (Strings.isEmpty(address)) {
                throw new DeployException("50010", "请配置historyserver.web.address属性，并开启flink的jobHistory服务");
            }
            String port = String.valueOf(load.get("historyserver.web.port"));
            if (Strings.isEmpty(port)) {
                throw new DeployException("50010", "请配置historyserver.web.port属性，并开启flink的jobHistory服务");
            }
            return JobUrlsConstants.HTTP + address + ":" + port;
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
