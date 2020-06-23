/home/testinggcpuser/spark-2.4.7-bin-scala2.11.12-hadoop31-k8s-docker-pip/bin/spark-submit \
--deploy-mode cluster \
--class com.qingmin.testProject.sparkStreammingExample \
--master k8s://https://35.232.25.62 \
--properties-file /home/testinggcpuser/gke-test/configurations/properties \  # cloudshell host file system
local:///opt/spark/jars/sparkStreaming-pubsub-dataproc-1.0-SNAPSHOT.jar