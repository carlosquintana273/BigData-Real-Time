package KafkaProducersClient

import java.util.Properties

import Utils.BaseFunctions
import com.google.gson.JsonObject
import com.mashape.unirest.http.Unirest
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.KafkaFuture.BaseFunction
import org.apache.kafka.common.serialization.StringSerializer

object ProducerPowerBi extends App {
  println("VM arguemtns :")
  println("-Dbrokers=kafka1:19092,kafka2:19093,kafka3:19094 -DinputTopic=mx-transaction-input")
  println("VM arguemtns :")
  println("-Dbrokers=kafka1:19092,kafka2:19093,kafka3:19094 -Dsleep=1 -Dthreads=2 -DlimitMessages=100 -DbroadPath=data-streams.txt -DinputTopic=mx-transaction-input")
  private val brokers = sys.props.get("brokers").get
  println("brokers : " + brokers)
  private val inputTopic = sys.props.get("inputTopic").get
  println("inputTopic : " + inputTopic)
  private val limitMessages = sys.props.get("limitMessages").get
  println("limitMessages : " + limitMessages)
  private val broadPath = sys.props.get("broadPath").get
  println("broadPath : " + broadPath)
  private val sleep = sys.props.get("sleep").get
  println("sleep : " + sleep + " expressed in milliseconds")

  private def configuration: Properties = {
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getCanonicalName)
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getCanonicalName)
    //    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[IntegerSerializer].getCanonicalName)
    //    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[LongSerializer].getCanonicalName)
    props
  }

    val producer = new KafkaProducer[String, String](configuration)

    val reader = new ReaderSource
    val broadList: Array[String] = reader.readBroad(broadPath)
    var limit = 0
    val until = limitMessages.toInt
    broadList.foreach(println(_))

    while (limit < until) {
      broadList.foreach {
        message => {
          limit += 1
          println(limit)
          Thread.sleep(sleep.toInt)
          println(message)
//          val mes: JsonObject = BaseFunctions.getJson(message)
          Unirest
            .post("https://api.powerbi.com/beta/c4a66c34-2bb7-451f-8be1-b2c26a430158/datasets/d7888ac7-012d-4861-a3bd-c5c75f4e9ccd/rows?key=78W8lYzTYK6wT26Oa5HeZqVbV4Pb%2Fl6LxEtl0ixyOL88Xjpctpf%2Be83oQKCXi4MPCRE7paufv0wynsN1jLBC%2Fw%3D%3D")
            .header("Content-Type", "application/json")
            .body(message)
            .asJsonAsync()

          val record: ProducerRecord[String, String] = new ProducerRecord[String, String](inputTopic, message)
          producer.send(record)
        }
      }
    }
    producer.close()
  }