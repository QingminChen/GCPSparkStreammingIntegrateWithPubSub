package com.qingmin.testProject
import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util
import java.util.{ArrayList, Date, List}

import com.google.api.core.{ApiFuture, ApiFutures}
import com.google.api.gax.core.{CredentialsProvider, FixedCredentialsProvider}
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.pubsub.v1.Publisher
import com.google.pubsub.v1.{ProjectTopicName, PubsubMessage}
import org.apache.spark.internal.Logging
import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.pubsub.SparkGCPCredentials
import org.apache.spark.streaming.{Seconds, StreamingContext}
//import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.pubsub.{PubsubUtils, SparkGCPCredentials}
import com.google.protobuf.ByteString


object sparkStreammingExample extends Logging {

  def publishToPubSub( messageBT: ByteString, outputTopicNameStr: String, credentialsLocationPathStr: String, projectIDStr: String): Unit ={
    logInfo("**********************************Qingmin*****************************************Method publishToPubSub start")
    val futures = new util.ArrayList[ApiFuture[String]]
    //    val credentialsProvider = FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(new FileInputStream(credentialsJsonFilePathCluster.value)))
    //    val outputStandardTopicName = ProjectTopicName.of(projectIDCluster.value, outputTopicCluster.value)
    val credentialsProvider = FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(new FileInputStream(credentialsLocationPathStr)))
    val outputStandardTopicName = ProjectTopicName.of(projectIDStr, outputTopicNameStr)
    //    val credentialsProvider = FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(new FileInputStream("/home/testinggcpuser/AllServicesKey.json")))
    //    val outputStandardTopicName = ProjectTopicName.of("pubsub-test-project-16951", "pubsub-project16951-output-topic2")
    val publisher = Publisher.newBuilder(outputStandardTopicName).setCredentialsProvider(credentialsProvider).build

    try {
      val pubsubMessage = PubsubMessage.newBuilder().setData(messageBT).build()
      logInfo("**********************************Qingmin*****************************************15 => BT each :" + messageBT.isEmpty)
      logInfo("**********************************Qingmin*****************************************15 => BT each :" + messageBT.isEmpty)
      val future = publisher.publish(pubsubMessage)
      futures.add(future)

    } finally {
      println("**********************************Qingmin*****************************************16 finally")
      logInfo("**********************************Qingmin*****************************************16 finally")
      logInfo("**********************************Qingmin*****************************************16 finally")
      logInfo("**********************************Qingmin*****************************************futures size： " + futures.isEmpty)

      println("**********************************Qingmin*****************************************17 finally 2")
      logInfo("**********************************Qingmin*****************************************17 finally 2")
      // Wait on any pending requests
      val messageIds = ApiFutures.allAsList(futures).get()
      import scala.collection.JavaConversions._
      for (messageId <- messageIds.toList) {
        // System.out.println("TOPIC_ID: " + outputTopic + ", Send Confirmed Timestamp: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date) + ", MESSAGE_ID_IN_INPUT_TOPIC: " + messageId)
        logInfo("**********************************Qingmin*****************************************18 -> TOPIC_ID: " + outputTopicNameStr + ", Send Confirmed Timestamp: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date) + ", MESSAGE_ID_IN_OUTPUT_TOPIC: " + messageId)

        //logInfo("**********************************Qingmin*****************************************18 -> TOPIC_ID: " + outputTopicCluster.value + ", Send Confirmed Timestamp: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date) + ", MESSAGE_ID_IN_OUTPUT_TOPIC: " + messageId)
        logInfo("**********************************Qingmin*****************************************This batch of publishing finished")
      }
      if (publisher != null) { // When finished with the publisher, shutdown to free up resources.
        publisher.shutdown()
      }
    }
  }

  def main (args: Array[String]) {
    val projectIDStr = "pubsub-test-project-16951"
    val subscriptionStr = "pubsub-project16951-subscription1"
    val outputTopicStr = "pubsub-project16951-output-topic2"

    //val credentialsJsonFilePathStr = "C:\\Codes\\IntelliJ_IDEA_WORKSPACE\\Spark-DataProc-PubSub-Test\\src\\main\\resources\\AllServicesKey.json"    //windows local path
    //val credentialsJsonFilePathStr = "/home/testinggcpuser/AllServicesKey.json" //GCP Dataproc cluster version
    //val credentialsJsonFilePathStr = "/Users/chenqingmin/Codes/Github_Tools_Workspace/GCPSparkStreammingIntegrateWithPubSub/src/main/resources/AllServicesKey.json" //Mac laptop local version
    val credentialsJsonFilePathStr = "/opt/spark/myselfCert/AllServicesKey.json" //Mac laptop K8s cluster version

    //val sparkSession : SparkSession = SparkSession.builder.master("local[4]").appName("DataProc Subsub SparkStreamming").getOrCreate()
    val sparkSession: SparkSession = SparkSession.builder.appName("DataProc Subsub SparkStreamming").getOrCreate()
    val sc = sparkSession.sparkContext
    val ssc = new StreamingContext(sc, Seconds(10))
    //    val credentialsJsonFilePathCluster = sc.broadcast(credentialsJsonFilePathStr) //GCP Dataproc version
    //    val projectIDCluster = sc.broadcast(projectIDStr)
    //    val outputTopicCluster = sc.broadcast(outputTopicStr)
    val sparkGCPCredentials = SparkGCPCredentials.builder.jsonServiceAccount(credentialsJsonFilePathStr).build()
    //    logInfo("**********************************Qingmin*****************************************Broadcast variable projectIDCluster: " + projectIDCluster.value)
    //    logInfo("**********************************Qingmin*****************************************Broadcast variable credentialsJsonFilePathCluster: " + credentialsJsonFilePathCluster.value)
    //    logInfo("**********************************Qingmin*****************************************Broadcast variable outputTopicCluster: " + outputTopicCluster.value)
    //
    //    logInfo("**********************************Qingmin*****************************************Broadcast variable projectIDCluster ID Long: " + projectIDCluster.id)
    //    logInfo("**********************************Qingmin*****************************************Broadcast variable projectIDCluster ID String: " + projectIDCluster.toString())
    //    logInfo("**********************************Qingmin*****************************************Broadcast variable credentialsJsonFilePathCluster ID Long: " + credentialsJsonFilePathCluster.id)
    //    logInfo("**********************************Qingmin*****************************************Broadcast variable credentialsJsonFilePathCluster ID String: " + credentialsJsonFilePathCluster.toString())
    //    logInfo("**********************************Qingmin*****************************************Broadcast variable outputTopicCluster ID Long: " + outputTopicCluster.id)
    //    logInfo("**********************************Qingmin*****************************************Broadcast variable outputTopicCluster ID String: " + outputTopicCluster.toString())

    val pubsubReceiverInputDStream = PubsubUtils.createStream(ssc, projectIDStr, None, subscriptionStr, sparkGCPCredentials, StorageLevel.MEMORY_AND_DISK_SER_2)
    logInfo("**********************************Qingmin*****************************************Initialized finished")

    pubsubReceiverInputDStream
      .map(message =>
        new String(message.getData(), StandardCharsets.UTF_8)
      ).map(messageStr =>
      ByteString.copyFromUtf8(messageStr)
    ).foreachRDD(messageBTRDD =>
      messageBTRDD.foreach(messageBT =>
        publishToPubSub(messageBT, outputTopicStr, credentialsJsonFilePathStr, projectIDStr)
      )
    )

    ssc.start() // Start the computation
    ssc.awaitTermination()

    while (true) {
      Thread.sleep(300000)
      ssc.stop(true, true)

    }

    print("123")

    /**
     *  1. test broadcast variable and pass the argument with main menthod, it worked
     *  1. test broadcast variable and pass the argument without main menthod, it doesn't work
     *  2. test passing the argument with main menthod but without the broadcast variable, it worked
     *  3. wrap up all the arguments needed in the method by passing  done
     *  So it wasn't a matter of populating the broadcast variable across the cluster, it was a matter of whether or not which method to closure the code( main or inherited App)
     */
  }
}
