package io.github.yogboot.server.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import io.github.yogboot.api.exception.DeployException;
import io.github.yogboot.api.pojo.DeployRequest;
import io.github.yogboot.api.pojo.dto.DeployData;
import io.github.yogboot.api.pojo.flink.JobExceptions;
import io.github.yogboot.api.pojo.flink.JobStatus;
import io.github.yogboot.api.utils.YamlUtils;
import io.github.yogboot.server.utils.HadoopUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.client.deployment.ClusterDeploymentException;
import org.apache.flink.client.deployment.ClusterSpecification;
import org.apache.flink.client.program.*;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.DeploymentOptions;
import org.apache.flink.configuration.GlobalConfiguration;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.jobgraph.SavepointRestoreSettings;
import org.apache.flink.yarn.YarnClientYarnClusterInformationRetriever;
import org.apache.flink.yarn.YarnClusterDescriptor;
import org.apache.flink.yarn.configuration.YarnConfigOptionsInternal;
import org.apache.flink.yarn.configuration.YarnDeploymentTarget;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static org.apache.flink.configuration.CoreOptions.DEFAULT_PARALLELISM;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeployService {
    public java.io.InputStream getHadoopConfigPath(String configName) {
        String hadoopHomeDir = System.getenv("HADOOP_HOME");
        try {
            return Files.newInputStream(Paths.get(hadoopHomeDir + File.separator + "etc" + File.separator + "hadoop" + File.separator + configName));
        } catch (IOException e) {
            e.printStackTrace();
            throw new DeployException("50001", configName + "配置文件不存在");
        }
    }

    public org.apache.hadoop.conf.Configuration generateHadoopConf() {
        org.apache.hadoop.conf.Configuration hadoopConf = new org.apache.hadoop.conf.Configuration(false);
        hadoopConf.addResource(getHadoopConfigPath("core-site.xml"));
        hadoopConf.addResource(getHadoopConfigPath("hdfs-site.xml"));
        hadoopConf.addResource(getHadoopConfigPath("yarn-site.xml"));
        return hadoopConf;
    }

    public DeployData executeSql(DeployRequest deployRequest) {
        String flinkHomeDir = System.getenv("FLINK_HOME");
        String deployHomeDir = System.getenv("DEPLOY_HOME");
        String flinkLib = System.getenv("FLINK_LIB");
        String flinkJar = System.getenv("FLINK_JAR");
        String userAppJar = System.getenv("USERAPP_JAR");
        log.warn("FLINK_LIB:", flinkLib);
        log.warn("FLINK_JAR:", flinkJar);
        log.warn("USERAPP_JAR:", userAppJar);
        YarnClient yarnClient = YarnClient.createYarnClient();
        YarnConfiguration yarnConfig = new YarnConfiguration(generateHadoopConf());
        yarnClient.init(yarnConfig);
        yarnClient.start();
        Configuration flinkConfig = GlobalConfiguration.loadConfiguration(flinkHomeDir + "/conf");
        flinkConfig.setString(YarnConfigOptionsInternal.APPLICATION_LOG_CONFIG_FILE, flinkHomeDir + "/conf/log4j.properties");
        String filePath = "";
        flinkConfig.set(DeploymentOptions.TARGET, YarnDeploymentTarget.PER_JOB.getName());
        flinkConfig.set(DeploymentOptions.SHUTDOWN_IF_ATTACHED, true);
        ClusterSpecification clusterSpecification = new ClusterSpecification.ClusterSpecificationBuilder()
            .setMasterMemoryMB(deployRequest.getMasterMemoryMB())
            .setTaskManagerMemoryMB(deployRequest.getTaskManagerMemoryMB())
            .setSlotsPerTaskManager(deployRequest.getSlotsPerTaskManager() <= 0 ? 1 : deployRequest.getSlotsPerTaskManager())
            .createClusterSpecification();
        YarnClusterDescriptor descriptor = new YarnClusterDescriptor(
            flinkConfig,
            yarnConfig,
            yarnClient,
            YarnClientYarnClusterInformationRetriever.create(yarnClient),
            false);
        if (Strings.isEmpty(deployRequest.getSql())) {
            throw new DeployException("50015", "sql 不能为空！");
        }
        PackagedProgram program;
        String className = "";
        ClusterClientProvider<ApplicationId> provider;
        JobGraph jobGraph;
        String flinkJobId = "";
        try {
            if (1 == deployRequest.getStreamMode()) {
                filePath = deployHomeDir + "/plugins/deploy-sqlstream-plugin.jar";
                className = "io.github.yogboot.plugin.sqlstream.SqlJob";
            } else {
                filePath = deployHomeDir + "/plugins/deploy-sqlbatch-plugin.jar";
                className = "io.github.yogboot.plugin.sqlbatch.SqlJob";
            }
            try {
                File[] jars = new File(flinkHomeDir + "/lib/").listFiles();
                List<File> shipFiles = new ArrayList<>();
                List<URL> classpathFiles = new ArrayList<>();
                if (jars != null) {
                    for (File jar : jars) {
                        if (jar.getName().contains("flink-dist")) {
                            try {
                                descriptor.setLocalJarPath(new Path(jar.toURI().toURL().toString()));
                            } catch (MalformedURLException e) {
                                log.error(e.getMessage());
                                throw new DeployException("50015", e.getMessage());
                            }
                        } else if (jar.getName().contains("flink-connector")
                            || jar.getName().contains("hive-exec")
                            || jar.getName().contains("libfb303")) {
                            try {
                                shipFiles.add(jar);
                                classpathFiles.add(jar.toURI().toURL());
                            } catch (MalformedURLException e) {
                                log.error(e.getMessage());
                                throw new DeployException("50015", e.getMessage());
                            }
                        } else {
                            shipFiles.add(jar);
                        }
                    }
                }
                File[] connectorJars = new File(deployHomeDir + "/connector/").listFiles();
                if (connectorJars != null) {
                    shipFiles.addAll(Arrays.asList(connectorJars));
                    for (File connectorJar : connectorJars) {
                        try {
                            classpathFiles.add(connectorJar.toURI().toURL());
                        } catch (MalformedURLException e) {
                            log.error(e.getMessage());
                            throw new DeployException("50015", e.getMessage());
                        }
                    }
                }
                shipFiles.add(new File(flinkHomeDir + "/conf/log4j.properties"));
                descriptor.addShipFiles(shipFiles);
                program = PackagedProgram.newBuilder()
                    .setJarFile(new File(filePath))
                    .setEntryPointClassName(className)
                    .setArguments(deployRequest.getSql())
                    .setUserClassPaths(classpathFiles)
                    .setSavepointRestoreSettings(SavepointRestoreSettings.none())
                    .build();
                jobGraph = PackagedProgramUtils.createJobGraph(program, flinkConfig, deployRequest.getParallelism() > 0 ? deployRequest.getParallelism() : flinkConfig.getInteger(DEFAULT_PARALLELISM), false);
            } catch (ProgramInvocationException e) {
                e.printStackTrace();
                throw new DeployException("50014", e.getMessage());
            }
            provider = descriptor.deployJobCluster(clusterSpecification, jobGraph, true);
            flinkJobId = jobGraph.getJobID().toString();
        } catch (ClusterDeploymentException e) {
            e.printStackTrace();
            throw new DeployException("50015", e.getMessage());
        }
        ClusterClient<ApplicationId> clusterClient = provider.getClusterClient();
        String applicationId = clusterClient.getClusterId().toString();
        String webInterfaceURL = clusterClient.getWebInterfaceURL();
        return DeployData.builder()
            .applicationId(applicationId)
            .flinkJobId(flinkJobId)
            .webInterfaceURL(webInterfaceURL)
            .build();
    }

    public DeployData getTaskManagerLog(DeployRequest deployRequest) {
        Map<String, String> map = HadoopUtils.parseYarnLog(deployRequest.getApplicationId());
        String taskManagerLog = map.get("taskmanager.log") == null ? "" : map.get("taskmanager.log");
        return DeployData.builder()
            .taskManagerLogs(Arrays.asList(taskManagerLog.split("\n")))
            .build();
    }

    public DeployData getJobManagerLog(DeployRequest deployRequest) {
        Map<String, String> map = HadoopUtils.parseYarnLog(deployRequest.getApplicationId());
        String jobManagerLog = map.get("jobmanager.log") == null ? "" : map.get("jobmanager.log");
        return DeployData.builder()
            .jobManagerLogs(Arrays.asList(jobManagerLog.split("\n")))
            .build();
    }

    public DeployData getYarnStatus(DeployRequest deployRequest) throws IOException, YarnException {
        YarnClient yarnClient = YarnClient.createYarnClient();
        YarnConfiguration yarnConfig = new YarnConfiguration(generateHadoopConf());
        yarnClient.init(yarnConfig);
        yarnClient.start();
        ApplicationReport applicationReport;
        try {
            applicationReport = yarnClient.getApplicationReport(ApplicationId.fromString(deployRequest.getApplicationId()));
        } catch (Exception e) {
            throw new DeployException("50020", e.getMessage());
        }
        FinalApplicationStatus finalApplicationStatus = applicationReport.getFinalApplicationStatus();
        YarnApplicationState yarnApplicationState = applicationReport.getYarnApplicationState();
        return DeployData.builder().finalStatus(finalApplicationStatus.name()).yarnState(yarnApplicationState.name()).build();
    }

    public DeployData killYarn(DeployRequest deployRequest) throws IOException, YarnException {
        YarnClient yarnClient = YarnClient.createYarnClient();
        YarnConfiguration yarnConfig = new YarnConfiguration(generateHadoopConf());
        yarnClient.init(yarnConfig);
        yarnClient.start();
        try {
            yarnClient.killApplication(ApplicationId.fromString(deployRequest.getApplicationId()));
        } catch (Exception e) {
            throw new DeployException("50017", e.getMessage());
        }
        return DeployData.builder().build();
    }

    public DeployData getJobStatus(DeployRequest deployRequest) throws IOException, YarnException {
        ResponseEntity<JobStatus> response;
        try {
            response = new RestTemplate().getForEntity(YamlUtils.getFlinkJobHistoryUrl() + "/jobs/" + deployRequest.getJobId(), JobStatus.class);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new DeployException("50016", "作业正在运行中或者作业不存在");
        }
        return DeployData.builder().jobStatus(response.getBody()).build();
    }

    public DeployData getRootExceptions(DeployRequest deployRequest) {
        ResponseEntity<JobExceptions> response;
        try {
            response = new RestTemplate().getForEntity(YamlUtils.getFlinkJobHistoryUrl() + "/jobs/" + deployRequest.getJobId() + "/exceptions", JobExceptions.class);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new DeployException("50016", "作业正在运行中或者作业不存在");
        }
        String rootExceptions;
        if (response.getBody() == null || response.getBody().getRootException() == null) {
            rootExceptions = "";
        } else {
            rootExceptions = response.getBody().getRootException();
        }
        return DeployData.builder().rootExceptions(Arrays.asList(rootExceptions.split("\n"))).build();
    }

    public DeployData getData(DeployRequest deployRequest) throws IOException, YarnException {
        Map<String, String> map = HadoopUtils.parseYarnLog(deployRequest.getApplicationId());
        String dataLog = map.get("taskmanager.out");
        String jsonStr = "[" + dataLog + "]";
        if (Strings.isEmpty(dataLog)) {
            return DeployData.builder().build();
        }
        List<String> columnNames = new ArrayList<>();
        String managerLog = map.get("taskmanager.log");
        Pattern pattern = compile("(table=\\[default_catalog.default_database.out_table\\], fields=\\[).+?]", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(managerLog);
        if (matcher.find()) {
            String trim = matcher.group().trim().replace(" ", "");
            columnNames = Arrays.asList(trim.substring(59, trim.length() - 1).split(","));
        }
        TypeReference<List<List<String>>> typeRef = new TypeReference<List<List<String>>>() {
        };
        return DeployData.builder().dataList(JSON.parseObject(jsonStr, typeRef)).columnNames(columnNames).build();
    }
}
