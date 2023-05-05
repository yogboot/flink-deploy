# flink-deploy
flink 远程提交 sql任务

#设置环境变量
export JAVA_HOME=/usr/java/jdk1.8.0
export CLASSPATH=.:$CLASSPATH:$JAVA_HOME/lib
export PATH=$PATH:$JAVA_HOME/bin
export DEPLOY_HOME=/usr/local/flink-deploy
export PATH=$PATH:$DEPLOY_HOME/bin
export FLINK_HOME=/usr/local/flink-1.13
export PATH=$PATH:$FLINK_HOME/bin
export HADOOP_HOME=/usr/hdp/3.1.0/hadoop
export HADOOP_CLASSPATH=/usr/hdp/3.1.0/hadoop-yarn/lib
export HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop
export YARN_CONF_DIR=$HADOOP_HOME/etc/hadoop
export PATH=$PATH:$HADOOP_HOME/bin
export PATH=$PATH:$HADOOP_HOME/sbin
export PATH=$PATH:$HADOOP_CLASSPATH
export HADOOP_USER_NAME=hdfs
export PATH=$PATH:$HADOOP_USER_NAME

#开启flink历史服务
bash ./bin/start-cluster.sh
bash ./bin/stop-cluster.sh
bin/historyserver.sh start

#启动服务
chmod -R 777 /usr/local/flink-deploy
deploy version
deploy config
deploy-key
deploy start
