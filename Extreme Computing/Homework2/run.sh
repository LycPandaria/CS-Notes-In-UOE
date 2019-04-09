export JAVA_HOME=/usr/lib/jvm/java-1.8.0-sun-1.8.0.91/
export PATH=${JAVA_HOME}/bin:${PATH}
export HADOOP_CLASSPATH=${JAVA_HOME}/lib/tools.jar
hdfs dfs -rm -r /user/s1616245/assignment2/task3/output/
hadoop com.sun.tools.javac.Main ./*.java
jar cf task3.jar ./*.class
hadoop jar task3.jar TopUser \
/data/assignments/ex2/part2/stackSmall.txt \
/user/s1616245/assignment2/task3/output
hdfs dfs -cat /user/s1616245/assignment2/task3/output/*
