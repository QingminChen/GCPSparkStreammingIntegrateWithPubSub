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


object sparkStreammingExample extends App with Logging {


  val projectIDStr = "pubsub-test-project-16951"
  val subscription = "pubsub-project16951-subscription1"
  val outputTopicStr = "pubsub-project16951-output-topic2"

  //val credentialsJsonFilePathStr = "C:\\Codes\\IntelliJ_IDEA_WORKSPACE\\Spark-DataProc-PubSub-Test\\src\\main\\resources\\AllServicesKey.json"    //local path
  val credentialsJsonFilePathStr = "/home/testinggcpuser/AllServicesKey.json"  //GCP Dataproc version

  //val sparkSession : SparkSession = SparkSession.builder.master("local[4]").appName("DataProc Subsub SparkStreamming").getOrCreate()
  val sparkSession : SparkSession = SparkSession.builder.appName("DataProc Subsub SparkStreamming").getOrCreate()
  val sc = sparkSession.sparkContext
  val ssc = new StreamingContext(sc, Seconds(10))
  val credentialsJsonFilePathCluster = sc.broadcast(credentialsJsonFilePathStr)  //GCP Dataproc version
  val projectIDCluster = sc.broadcast(projectIDStr)
  val outputTopicCluster= sc.broadcast(outputTopicStr)
  val sparkGCPCredentials = SparkGCPCredentials.builder.jsonServiceAccount(credentialsJsonFilePathStr).build()
  logInfo("**********************************Qingmin*****************************************Broadcast variable projectIDCluster: "+ projectIDCluster.value)
  logInfo("**********************************Qingmin*****************************************Broadcast variable credentialsJsonFilePathCluster: "+ credentialsJsonFilePathCluster.value)
  logInfo("**********************************Qingmin*****************************************Broadcast variable outputTopicCluster: "+ outputTopicCluster.value)

  logInfo("**********************************Qingmin*****************************************Broadcast variable projectIDCluster ID Long: "+ projectIDCluster.id)
  logInfo("**********************************Qingmin*****************************************Broadcast variable projectIDCluster ID String: "+ projectIDCluster.toString())
  logInfo("**********************************Qingmin*****************************************Broadcast variable credentialsJsonFilePathCluster ID Long: "+ credentialsJsonFilePathCluster.id)
  logInfo("**********************************Qingmin*****************************************Broadcast variable credentialsJsonFilePathCluster ID String: "+ credentialsJsonFilePathCluster.toString())
  logInfo("**********************************Qingmin*****************************************Broadcast variable outputTopicCluster ID Long: "+ outputTopicCluster.id)
  logInfo("**********************************Qingmin*****************************************Broadcast variable outputTopicCluster ID String: "+ outputTopicCluster.toString())

  val pubsubReceiverInputDStream = PubsubUtils.createStream(ssc, projectIDStr, None, subscription, sparkGCPCredentials, StorageLevel.MEMORY_AND_DISK_SER_2)
  logInfo("**********************************Qingmin*****************************************Initialized finished")

  def publishToPubSub( messageBT: ByteString): Unit ={
    logInfo("**********************************Qingmin*****************************************Method publishToPubSub start")
    val futures = new util.ArrayList[ApiFuture[String]]

    //val credentialsProvider = FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(new FileInputStream(credentialsJsonFilePathCluster.value)))
    //val outputStandardTopicName = ProjectTopicName.of(projectIDCluster.value, outputTopicCluster.value)
    val credentialsProvider = FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(new FileInputStream("/home/testinggcpuser/AllServicesKey.json")))
    val outputStandardTopicName = ProjectTopicName.of("pubsub-test-project-16951", "pubsub-project16951-output-topic2")
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
        logInfo("**********************************Qingmin*****************************************18 -> TOPIC_ID: " + outputTopicStr + ", Send Confirmed Timestamp: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date) + ", MESSAGE_ID_IN_OUTPUT_TOPIC: " + messageId)

        logInfo("**********************************Qingmin*****************************************18 -> TOPIC_ID: " + outputTopicCluster.value + ", Send Confirmed Timestamp: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date) + ", MESSAGE_ID_IN_OUTPUT_TOPIC: " + messageId)

      }
      if (publisher != null) { // When finished with the publisher, shutdown to free up resources.
        publisher.shutdown()
      }
    }
  }

  pubsubReceiverInputDStream
    .map(message =>
    //logInfo("**********************************Qingmin*****************************************15: "+message.getData().toString)
     new String(message.getData(), StandardCharsets.UTF_8)
    ) // .foreachRDD( rddEach => logInfo("**********************************Qingmin*****************************************15: "+rddEach.isEmpty()) )

    .map( messageStr =>
       ByteString.copyFromUtf8(messageStr)
    )
      //.flatMap( message => message.getData()

  .foreachRDD( messageBTRDD =>
      //logInfo("**********************************Qingmin*****************************************15 -> messageBTRDD value Status: "+messageBTRDD.isEmpty()+", messageBTRDD value size: "+messageBTRDD.count())
      messageBTRDD.foreach(messageBT =>
        //logInfo("**********************************Qingmin*****************************************15 -> messageBT value Status: "+messageBT.toString)
        publishToPubSub(messageBT)
//          try {
//            val pubsubMessage = PubsubMessage.newBuilder().setData(messageBT).build()
//            logInfo("**********************************Qingmin*****************************************15 => BT each :" + messageBT.isEmpty)
//            logInfo("**********************************Qingmin*****************************************15 => BT each :" + messageBT.isEmpty)
//            val future = publisher.publish(pubsubMessage)
//            futures.add(future)
//
//          } finally {
//            println("**********************************Qingmin*****************************************16 finally")
//            logInfo("**********************************Qingmin*****************************************16 finally")
//            logInfo("**********************************Qingmin*****************************************16 finally")
//            logInfo("**********************************Qingmin*****************************************futures size： " + futures.isEmpty)
//
//            println("**********************************Qingmin*****************************************17 finally 2")
//            logInfo("**********************************Qingmin*****************************************17 finally 2")
//            // Wait on any pending requests
//            val messageIds = ApiFutures.allAsList(futures).get()
//            import scala.collection.JavaConversions._
//            for (messageId <- messageIds.toList) {
//              // System.out.println("TOPIC_ID: " + outputTopic + ", Send Confirmed Timestamp: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date) + ", MESSAGE_ID_IN_INPUT_TOPIC: " + messageId)
//              logInfo("**********************************Qingmin*****************************************18 -> TOPIC_ID: " + outputTopic + ", Send Confirmed Timestamp: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date) + ", MESSAGE_ID_IN_OUTPUT_TOPIC: " + messageId)
//            }
//          }

      )







  //    messageBTRDD.foreachPartition( msgBTPartition =>
  //       for(msgBT <- msgBTPartition){
  //         logInfo("BT each :"+msgBT.isEmpty)
  //         println("BT each :"+msgBT.isEmpty)
  //         try {
  //
  //           val pubsubMessage = PubsubMessage.newBuilder().setData(msgBT).build()
  //           val future = publisher.publish(pubsubMessage)
  //           futures.add(future)
  //
  //         } finally {
  //           println("finally")
  //           logInfo("finally")
  //           ApiFutures.allAsList(futures)
  //           println("finally 2")
  //           logInfo("finally 2")
  //           ApiFutures.allAsList(futures).get()
  //              // Wait on any pending requests
  //           val messageIds = ApiFutures.allAsList(futures).get()
  //           import scala.collection.JavaConversions._
  //           for (messageId <- messageIds.toList) {
  //              // System.out.println("TOPIC_ID: " + outputTopic + ", Send Confirmed Timestamp: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date) + ", MESSAGE_ID_IN_INPUT_TOPIC: " + messageId)
  //             logInfo("TOPIC_ID: " + outputTopic + ", Send Confirmed Timestamp: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date) + ", MESSAGE_ID_IN_OUTPUT_TOPIC: " + messageId)
  //           }
  //         }
  //       }
  //    )
    )

                    //    .foreachRDD( messageBTRDD =>
                    //      messageBTRDD.foreachPartition( msgBTPartition =>
                    //
                    //           for(msgBT <- msgBTPartition){
                    //             println(msgBT.isEmpty)
                    //             val pubsubMessage = PubsubMessage.newBuilder().setData(msgBT).build()
                    //             publisher.publish(pubsubMessage)
                    //           }
                    //
                    //         )
                    //    )

  ssc.start()             // Start the computation
  ssc.awaitTermination()


  while(true){
    Thread.sleep(300000)
    ssc.stop(true, true)

  }

  print("123")



}
