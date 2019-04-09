hdfs dfs -cat  /user/s1616245/assignment1/task6/output/*
hadoop com.sun.tools.javac.Main Entropy.java
jar cf task6-1.jar Entropy*.class
hadoop jar task6-1.jar Entropy /user/s1616245/assignment1/task6/input/* /user/s1616245/assignment1/task6/output