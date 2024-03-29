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


object sparkStreammingExampleMac extends App with Logging {

  logInfo("sparkStreammingExample I am comming")

  val projectID = "pubsub-test-project-16951"
  logInfo("ProjectID: "+projectID)
  val subscription = "pubsub-project16951-subscription1"
  logInfo("Subscription: "+subscription)
  //val credentialsJsonFilePath = "/Users/chenqingmin/Codes/Github_Tools_Workspace/GCPSparkStreammingIntegrateWithPubSub/src/main/resources/AllServicesKey.json"    //local path
  val credentialsJsonFilePath = "/opt/spark/myselfCert/AllServicesKey.json"
  logInfo("CredentialsJsonFilePath: "+credentialsJsonFilePath)
  val sparkGCPCredentials = SparkGCPCredentials.builder.jsonServiceAccount(credentialsJsonFilePath).build()
  logInfo("SparkGCPCredentials is ready")
  //val sparkSession : SparkSession = SparkSession.builder.master("local[4]").appName("DataProc Subsub SparkStreamming").config("spark.kubernetes.authenticate.driver.serviceAccountName","sparkonk8s").config("spark.kubernetes.namespace","spark-job-test").getOrCreate()
  //val sparkSession : SparkSession = SparkSession.builder.master("local[4]").appName("DataProc Subsub SparkStreamming").getOrCreate()
  //val sparkSession : SparkSession = SparkSession.builder.appName("PubSub SparkStreamming On K8s").config("spark.kubernetes.authenticate.driver.serviceAccountName","sparkonk8s").config("spark.kubernetes.namespace","spark-job-test").getOrCreate()
  val sparkSession : SparkSession = SparkSession.builder.appName("PubSub SparkStreamming On K8s").getOrCreate()
  logInfo("sparkSession is ready")
  val outputTopic = "pubsub-project16951-output-topic2"
  logInfo("OutputTopic: "+outputTopic)
  val outputStandardTopicName = ProjectTopicName.of(projectID, outputTopic)
  logInfo("outputStandardTopicName class is ready")

  val sc = sparkSession.sparkContext
  logInfo("SparkContext is ready")
  val ssc = new StreamingContext(sc, Seconds(20))
  logInfo("SparkStreammingContext is ready")
//  PubsubUtils.createStream(,)

  val futures = new util.ArrayList[ApiFuture[String]]
  logInfo("futures is ready")
  val credentialsProvider = FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(new FileInputStream(credentialsJsonFilePath)))
  logInfo("credentialsProvider getAuthenticationType: "+credentialsProvider.getCredentials.getAuthenticationType.toString)
  val publisher = Publisher.newBuilder(outputStandardTopicName).setCredentialsProvider(credentialsProvider).build
  logInfo("publisher is ready")

  val pubsubReceiverInputDStream = PubsubUtils.createStream(ssc, projectID, None, subscription, sparkGCPCredentials, StorageLevel.MEMORY_AND_DISK_SER_2)
  logInfo("pubsubReceiverInputDStream is ready")
  pubsubReceiverInputDStream.map(message =>
    new String(message.getData(), StandardCharsets.UTF_8)
  )
//    .map( messageStr =>
//    ByteString.copyFromUtf8(messageStr)
//  ).foreachRDD( messageBT =>
//    //  PubsubMessage.newBuilder().setData(messageBT).build()
//    println(messageBT)
//  )
    .map( messageStr =>
       ByteString.copyFromUtf8(messageStr)
    )

//    .foreachRDD( messageBTRDD =>
//      println(messageBTRDD.isEmpty())
//    )

      .foreachRDD( messageBTRDD =>
        messageBTRDD.foreachPartition( msgBTPartition =>
             for(msgBT <- msgBTPartition){
               logInfo("BT each :"+msgBT.isEmpty)
               //println("BT each :"+msgBT.isEmpty)
               try {

                 val pubsubMessage = PubsubMessage.newBuilder().setData(msgBT).build()
                 val future = publisher.publish(pubsubMessage)
                 futures.add(future)

               } finally {
                 //println("finally")
                 logInfo("finally")
                 ApiFutures.allAsList(futures)
                 //println("finally 2")
                 logInfo("finally 2")
                 ApiFutures.allAsList(futures).get()
                 // Wait on any pending requests
                 val messageIds = ApiFutures.allAsList(futures).get()
                 import scala.collection.JavaConversions._
                 for (messageId <- messageIds.toList) {
                   //System.out.println("TOPIC_ID: " + outputTopic + ", Send Confirmed Timestamp: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date) + ", MESSAGE_ID_IN_INPUT_TOPIC: " + messageId)
                   logInfo("TOPIC_ID: " + outputTopic + ", Send Confirmed Timestamp: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date) + ", MESSAGE_ID_IN_OUTPUT_TOPIC: " + messageId)
                 }
//                 if (publisher != null) { // When finished with the publisher, shutdown to free up resources.
//                   publisher.shutdown()
//                 }
               }
             }
           )
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
  logInfo("sparkStreammingExample Ended")



}
