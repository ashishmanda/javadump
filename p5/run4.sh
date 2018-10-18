#!/usr/bin/env bash
export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64
chmod a+x hadoop-0.20.2/bin/*
javac -cp ".:hadoop-0.20.2/hadoop-0.20.2-core.jar:hadoop-0.20.2/lib/commons-cli-1.2.jar:hadoop-0.20.2/lib/commons-codec-1.3.jar:hadoop-0.20.2/lib/commons-el-1.0.jar:hadoop-0.20.2/lib/commons-httpclient-3.0.1.jar:hadoop-0.20.2/lib/commons-logging-1.0.4.jar:hadoop-0.20.2/lib/commons-logging-api-1.0.4.jar:hadoop-0.20.2/lib/commons-net-1.4.1.jar:hadoop-0.20.2/lib/log4j-1.2.15.jar" src/cmsc433/p5/*.java -d bin/
jar -cvf TwitterAnalyzer.jar -C bin/ .
rm -rf temp
rm -rf out
hadoop-0.20.2/bin/hadoop jar TwitterAnalyzer.jar cmsc433/p5/Main hashtag_pair 5 p5_in/testdata_1_mod.xls out/
