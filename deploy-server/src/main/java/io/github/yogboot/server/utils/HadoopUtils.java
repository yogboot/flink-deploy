package io.github.yogboot.server.utils;

import io.github.yogboot.api.constant.JobUrlsConstants;
import io.github.yogboot.api.exception.DeployException;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class HadoopUtils {

    public static Map<String, String> parseYarnLog(String applicationId) {
        String hadoopHomeDir = System.getenv("HADOOP_HOME");
        Configuration yarnConf = new Configuration(false);
        try {
            yarnConf.addResource(Files.newInputStream(Paths.get(hadoopHomeDir + File.separator + "etc" + File.separator + "hadoop" + File.separator + "yarn-site.xml")));
            yarnConf.addResource(Files.newInputStream(Paths.get(hadoopHomeDir + File.separator + "etc" + File.separator + "hadoop" + File.separator + "mapred-site.xml")));
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException("未找到yarn配置文件");
        }
        YarnClient yarnClient = YarnClient.createYarnClient();
        YarnConfiguration yarnConfig = new YarnConfiguration(yarnConf);
        yarnClient.init(yarnConfig);
        yarnClient.start();
        if (Strings.isEmpty(yarnConfig.get("yarn.resourcemanager.webapp.address"))) {
            throw new DeployException("50012", "请在yarn-site.xml中配置yarn.resourcemanager.webapp.address属性:${yarn.resourcemanager.hostname}:8088");
        }
        String jobHistoryAddress = yarnConfig.get("mapreduce.jobhistory.webapp.address");
        if (Strings.isEmpty(jobHistoryAddress)) {
            throw new DeployException("50012", "请在mapred-site.xml中配置mapreduce.jobhistory.webapp.address属性:0.0.0.0:19888");
        }
        Map appInfoMap;
        try {
            appInfoMap = new RestTemplate().getForObject(JobUrlsConstants.HTTP + yarnConfig.get("yarn.resourcemanager.webapp.address") + "/ws/v1/cluster/apps/" + applicationId, Map.class);
        } catch (HttpClientErrorException e) {
            throw new DeployException("50018", "作业不存在");
        }
        Map<String, Map<String, Object>> appMap = (Map<String, Map<String, Object>>) appInfoMap.get("app");
        String amContainerLogsUrl = String.valueOf(appMap.get("amContainerLogs"));
        Map<String, String> resultLog = new HashMap<>();
        parseYarnHtml(resultLog, amContainerLogsUrl, jobHistoryAddress);
        parseYarnHtml(resultLog, amContainerLogsUrl.replace("000001", "000002"), jobHistoryAddress);
        return resultLog;
    }

    public static void parseYarnHtml(Map<String, String> resultLog, String url, String jobHistoryAddress) {
        Document doc;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
        Elements contentEls = doc.getElementsByClass("content");
        if (contentEls.isEmpty()) {
            throw new RuntimeException("数据解析异常");
        }
        Elements preElements = contentEls.get(0).getElementsByTag("pre");
        for (Element element : preElements) {
            String elementText = element.text();
            if (elementText.isEmpty()) {
                continue;
            }
            Element thirdElement = element.previousElementSibling();
            String logUrl = thirdElement.select("a[href]").attr("href");
            String logStr;
            if (!logUrl.isEmpty()) {
                try {
                    logStr = Jsoup.connect(JobUrlsConstants.HTTP + jobHistoryAddress + logUrl).get().body().getElementsByTag("pre").text();
                } catch (IOException e) {
                    log.error(e.getMessage());
                    throw new RuntimeException(e);
                }
                thirdElement = thirdElement.previousElementSibling();
            } else {
                logStr = elementText;
            }
            Element firstElement = thirdElement.previousElementSibling().previousElementSibling();
            resultLog.put(firstElement.text().replace("Log Type:", "").trim(), logStr);
        }
    }
}
