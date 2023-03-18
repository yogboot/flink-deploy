package io.github.yogboot.server.controller;

import io.github.yogboot.api.constant.JobUrlsConstants;
import io.github.yogboot.api.exception.DeployException;
import io.github.yogboot.api.pojo.DeployRequest;
import io.github.yogboot.api.pojo.dto.DeployData;
import io.github.yogboot.common.response.SuccessResponse;
import io.github.yogboot.server.service.DeployService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class JobController {

    private final DeployService deployBizService;

    @SuccessResponse("部署成功")
    @PostMapping(JobUrlsConstants.EXECUTE_SQL_URL)
    public DeployData executeFlinkSql(@RequestBody DeployRequest deployRequest) {
        return deployBizService.executeSql(deployRequest);
    }

    @SuccessResponse("获取yarn作业状态成功")
    @PostMapping(JobUrlsConstants.GET_YARN_STATUS_URL)
    public DeployData getYarnStatus(@RequestBody DeployRequest deployRequest) {
        try {
            return deployBizService.getYarnStatus(deployRequest);
        } catch (IOException | YarnException e) {
            log.error(e.getMessage());
            throw new DeployException("执行异常", "50001");
        }
    }

    @SuccessResponse("获取JobManager日志成功")
    @PostMapping(JobUrlsConstants.GET_JOB_MANAGER_LOG_URL)
    public DeployData getJobManagerLog(@RequestBody DeployRequest deployRequest) {
        return deployBizService.getJobManagerLog(deployRequest);
    }

    @SuccessResponse("获取TaskManager日志成功")
    @PostMapping(JobUrlsConstants.GET_TASK_MANAGER_LOG_URL)
    public DeployData getTaskManagerLog(@RequestBody DeployRequest deployRequest) {
        return deployBizService.getTaskManagerLog(deployRequest);
    }

    @SuccessResponse("获取flink作业异常日志")
    @PostMapping(JobUrlsConstants.GET_ROOT_EXCEPTIONS_URL)
    public DeployData getRootExceptions(@RequestBody DeployRequest deployRequest) {
        return deployBizService.getRootExceptions(deployRequest);
    }

    @SuccessResponse("停止作业")
    @PostMapping(JobUrlsConstants.STOP_JOB_URL)
    public DeployData stopJob(@RequestBody DeployRequest deployRequest) {
        try {
            return deployBizService.killYarn(deployRequest);
        } catch (IOException | YarnException e) {
            log.error(e.getMessage());
            throw new DeployException("执行异常", "50001");
        }
    }

    @SuccessResponse("获取flink作业状态")
    @PostMapping(JobUrlsConstants.GET_JOB_STATUS_URL)
    public DeployData getJobStatus(@RequestBody DeployRequest deployRequest) {
        try {
            return deployBizService.getJobStatus(deployRequest);
        } catch (IOException | YarnException e) {
            log.error(e.getMessage());
            throw new DeployException("执行异常", "50001");
        }
    }

    @SuccessResponse(msg = "心跳检测成功")
    @GetMapping(JobUrlsConstants.HEART_CHECK_URL)
    public DeployData heartCheck() {
        return DeployData.builder().jobLog("正常").build();
    }

    @SuccessResponse("查询数据成功")
    @PostMapping(JobUrlsConstants.GET_DATA_URL)
    public DeployData getData(@RequestBody DeployRequest deployRequest) {
        try {
            return deployBizService.getData(deployRequest);
        } catch (IOException | YarnException e) {
            log.error(e.getMessage());
            throw new DeployException("执行异常", "50001");
        }
    }

}
