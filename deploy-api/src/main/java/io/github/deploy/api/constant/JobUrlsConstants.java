package io.github.deploy.api.constant;

public interface JobUrlsConstants {

    String HTTP = "http://";

    String HTTPS = "https://";

    String FLINK_JOBS_OVERVIEW = "/jobs/overview";

    String BASE_URL = "http://%s:%s/flink-deploy";

    String EXECUTE_SQL_URL = "/job/executeSql";

    String EXECUTE_JAR_URL = "/job/executeJar";

    String EXECUTE_JAVA_URL = "/job/executeJava";

    String EXECUTE_JSON_URL = "/job/executeJson";

    String STOP_JOB_URL = "/job/stopJob";

    String GET_JOB_STATUS_URL = "/job/getJobStatus";

    String GET_YARN_STATUS_URL = "/job/getYarnStatus";

    String GET_DATA_URL = "/job/getData";

    String GET_JOB_MANAGER_LOG_URL = "/job/getJobManagerLogs";

    String GET_TASK_MANAGER_LOG_URL = "/job/getTaskManagerLogs";

    String GET_ROOT_EXCEPTIONS_URL = "/job/getRootExceptions";

    String GET_JOB_ID_URL = "/job/getJobId";

    String QUERY_JOB_STATUS_URL = "/job/queryJobStatus";

    String HEART_CHECK_URL = "/heartCheck";
}
