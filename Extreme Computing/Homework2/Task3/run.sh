export JAVA_HOME=/usr/lib/jvm/java-1.8.0-sun-1.8.0.91/
export PATH=${JAVA_HOME}/bin:${PATH}
export HADOOP_CLASSPATH=${JAVA_HOME}/lib/tools.jar
hdfs dfs -rm -r /user/s1616245/assignment2/task2/output/
hadoop com.sun.tools.javac.Main ./*.java
jar cf task2.jar ./*.class
hadoop jar task2.jar TopQuestion \
/user/s1616245/assignment2/task2/input/* \
/user/s1616245/assignment2/task2/output
hdfs dfs -cat /user/s1616245/assignment2/task2/output/*
