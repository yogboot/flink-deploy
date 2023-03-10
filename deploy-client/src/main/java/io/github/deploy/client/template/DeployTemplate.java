package io.github.deploy.client.template;

import io.github.deploy.api.constant.JobUrlsConstants;
import io.github.deploy.api.constant.SecurityConstants;
import io.github.deploy.api.constant.SysConstants;
import io.github.deploy.api.exception.DeployException;
import io.github.deploy.api.exception.ExceptionMsgEnum;
import io.github.deploy.api.menu.Template;
import io.github.deploy.api.pojo.DeployRequest;
import io.github.deploy.api.pojo.DeployResponse;
import io.github.deploy.api.properties.DeployProperties;
import io.github.deploy.api.properties.ServerInfo;
import io.github.deploy.api.utils.HttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeployTemplate {

    private final DeployProperties deployProperties;

    public DeployTemplate.Builder build(String serverName) {
        ServerInfo serverInfo = deployProperties.getServers().get(serverName);
        if (serverInfo == null) {
            throw new DeployException(ExceptionMsgEnum.DEPLOY_SERVER_NOT_FOUND);
        }
        return new Builder(serverInfo);
    }

    public DeployTemplate.Builder build() {
        ServerInfo serverInfo = deployProperties.getServers().get(SysConstants.DEFAULT_SERVER_NAME);
        if (serverInfo == null) {
            throw new DeployException(ExceptionMsgEnum.DEPLOY_SERVER_NOT_FOUND);
        }
        return new Builder(serverInfo);
    }

    public DeployTemplate.Builder build(String host, int port, String key) {
        return new Builder(new ServerInfo(host, port, key));
    }

    public static class Builder {

        public DeployRequest deployRequest = new DeployRequest();
        private final ServerInfo serverInfo;

        public Builder(ServerInfo serverInfo) {
            this.serverInfo = serverInfo;
        }

        public DeployResponse requestDeployServer(String url, DeployRequest deployRequest) {
            Map<String, String> headers = new HashMap<>();
            headers.put(SecurityConstants.HEADER_AUTHORIZATION, serverInfo.getKey());
            try {
                return HttpUtils.doPost(url, headers, deployRequest, DeployResponse.class);
            } catch (IOException e) {
                log.error(e.getMessage());
                return new DeployResponse("500", e.getMessage());
            }
        }

        public Builder applicationId(String applicationId) {
            deployRequest.setApplicationId(applicationId);
            return this;
        }

        public Builder slotsPerTM(int slotsNum) {
            deployRequest.setSlotsPerTaskManager(slotsNum);
            return this;
        }

        public Builder parallelism(int parallelism) {
            deployRequest.setParallelism(parallelism);
            return this;
        }


        public Builder runtimeMode(int streamMode) {
            deployRequest.setStreamMode(streamMode);
            return this;
        }

        public Builder name(String name) {
            deployRequest.setName(name);
            return this;
        }

        public Builder pluginName(String pluginName) {
            deployRequest.setPluginName(pluginName);
            return this;
        }

        public Builder sql(String sql) {
            deployRequest.setSql(sql);
            return this;
        }

        public Builder jobId(String jobId) {
            deployRequest.setJobId(jobId);
            return this;
        }

        public Builder java(String java) {
            deployRequest.setJava(java);
            return this;
        }

        public Builder template(Template template) {
            deployRequest.setTemplate(template);
            return this;
        }

        public DeployResponse deploy() {
            String executeUrl = String.format(JobUrlsConstants.BASE_URL + JobUrlsConstants.EXECUTE_SQL_URL, serverInfo.getHost(), serverInfo.getPort());
            return requestDeployServer(executeUrl, deployRequest);
        }

        public DeployResponse stopJob() {
            String executeUrl = String.format(JobUrlsConstants.BASE_URL + JobUrlsConstants.STOP_JOB_URL, serverInfo.getHost(), serverInfo.getPort());
            return requestDeployServer(executeUrl, deployRequest);
        }

        public DeployResponse getData() {
            String executeUrl = String.format(JobUrlsConstants.BASE_URL + JobUrlsConstants.GET_DATA_URL, serverInfo.getHost(), serverInfo.getPort());
            return requestDeployServer(executeUrl, deployRequest);
        }

        public DeployResponse getJobStatus() {
            String executeUrl = String.format(JobUrlsConstants.BASE_URL + JobUrlsConstants.GET_JOB_STATUS_URL, serverInfo.getHost(), serverInfo.getPort());
            return requestDeployServer(executeUrl, deployRequest);
        }

        public DeployResponse getYarnStatus() {
            String executeUrl = String.format(JobUrlsConstants.BASE_URL + JobUrlsConstants.GET_YARN_STATUS_URL, serverInfo.getHost(), serverInfo.getPort());
            return requestDeployServer(executeUrl, deployRequest);
        }

        public DeployResponse getRootExceptions() {
            String executeUrl = String.format(JobUrlsConstants.BASE_URL + JobUrlsConstants.GET_ROOT_EXCEPTIONS_URL, serverInfo.getHost(), serverInfo.getPort());
            return requestDeployServer(executeUrl, deployRequest);
        }

        public DeployResponse getJobManagerLog() {
            String executeUrl = String.format(JobUrlsConstants.BASE_URL + JobUrlsConstants.GET_JOB_MANAGER_LOG_URL, serverInfo.getHost(), serverInfo.getPort());
            return requestDeployServer(executeUrl, deployRequest);
        }

        public DeployResponse getTaskManagerLog() {
            String executeUrl = String.format(JobUrlsConstants.BASE_URL + JobUrlsConstants.GET_TASK_MANAGER_LOG_URL, serverInfo.getHost(), serverInfo.getPort());
            return requestDeployServer(executeUrl, deployRequest);
        }

    }

}
