package io.github.yogboot.plugin.sqlstream;

import io.github.yogboot.plugin.common.utils.SqlUtils;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.logging.log4j.util.Strings;

import java.util.*;

public class SqlJob {

    public static List<String> selectSqlToOutSql(String selectSql, Map<String, List<String>> tableColumnMap) {
        List<String> sqlList = new ArrayList<>();
        String tableName = SqlUtils.getSelectSqlTableName(selectSql);
        sqlList.add("CREATE TABLE out_table (" + Strings.join(tableColumnMap.get(tableName), ',') + ") WITH ( 'connector' = 'out')");
        sqlList.add("insert into out_table " + selectSql);
        return sqlList;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("执行参数:" + args[0]);
        List<String> sqlList = Arrays.asList(args[0].split(";"));
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.getDefaultSavepointDirectory();
        env.enableCheckpointing(10000);
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(5000);
        env.getCheckpointConfig().setExternalizedCheckpointCleanup(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        env.getCheckpointConfig().setCheckpointTimeout(150000);
        env.getCheckpointConfig().setTolerableCheckpointFailureNumber(5);
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(5, 10000));
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        env.setRuntimeMode(RuntimeExecutionMode.STREAMING);
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        Map<String, List<String>> tableColumnMap = new HashMap<>();
        sqlList.forEach(metaSql -> {
            System.out.println("执行sql:" + metaSql);
            if ((sqlList.indexOf(metaSql) == sqlList.size() - 1) && !metaSql.contains("insert ") && metaSql.contains("select ")) {
                selectSqlToOutSql(metaSql, tableColumnMap).forEach(tableEnv::executeSql);
            } else if (metaSql.toLowerCase().contains("create table ")) {
                String tableName = SqlUtils.getCreateSqlTableName(metaSql);
                List<String> columnList = SqlUtils.getCreateSqlColumnList(metaSql);
                tableColumnMap.put(tableName, columnList);
                tableEnv.executeSql(metaSql);
            } else {
                tableEnv.executeSql(metaSql);
            }
        });
    }
}
