package com.project.spark
import com.project.dao.HbaseDao
import com.project.domain.Userrec
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.log4j.{Level, Logger}
import org.apache.spark._
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.spark.mllib.recommendation.{ALS, Rating}
/**
  * Created by luckguozi on 2019/4/14.
  */
object RecFood {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
    val sparkConf = new SparkConf().setAppName("FoodRecommend").setMaster("local[2]").set("spark.network.timeout", "1200")
    val sparkContext = new SparkContext(sparkConf)
    val hbaseConf = HBaseConfiguration.create()
    val hbaseConf1 = HBaseConfiguration.create()
    hbaseConf.set("hbase.zookeeper.quorum", "127.0.0.1")
    hbaseConf.set("hbase.zookeeper.property.clientPort", "2181")
    hbaseConf.set("zookeeper.session.timeout", "6000000")
    hbaseConf1.set("hbase.zookeeper.quorum", "127.0.0.1")
    hbaseConf1.set("hbase.zookeeper.property.clientPort", "2181")
    hbaseConf1.set("zookeeper.session.timeout", "6000000")

    println("\n=====================step 2 load data==========================")
    //加载HBase中的数据
    //读取数据并转化成rdd
    hbaseConf1.set(TableInputFormat.INPUT_TABLE, "user")
    hbaseConf.set(TableInputFormat.INPUT_TABLE, "rating")
    val ratingsData = sparkContext.newAPIHadoopRDD(hbaseConf, classOf[TableInputFormat],
      classOf[ImmutableBytesWritable],
      classOf[Result])
    val userdatas = sparkContext.newAPIHadoopRDD(hbaseConf1, classOf[TableInputFormat],
      classOf[ImmutableBytesWritable],
      classOf[Result])

    val hbaseRatings = ratingsData.map { case (_, res) =>
      val foodId = Bytes.toString(res.getValue(Bytes.toBytes("info"), Bytes.toBytes("fid")))
      val rating = Bytes.toString(res.getValue(Bytes.toBytes("info"), Bytes.toBytes("rating")))
      val userId = Bytes.toString(res.getValue(Bytes.toBytes("info"), Bytes.toBytes("uid")))
      Rating(userId.toInt, foodId.toInt, rating.toDouble)
    }.cache()
    //获取要推荐的用户id和rowkey数据
    val userdata = userdatas.map { case (_, res) =>
      val rowkey = Bytes.toString(res.getRow())
      //val Id = Bytes.toString(res.getValue(Bytes.toBytes("info"), Bytes.toBytes("uid")))
      Userrec(rowkey.toInt)
    }.cache()
    val numTrainRatings = hbaseRatings.count()
    println(s"[DEBUG]get $numTrainRatings train data from hbase")
    val rank = 10
    val lambda = 0.01
    val numIter = 10
    println("\n=====================system initiallizing...==========================")
    println("\n[DEBUG]training model...")
    val firstTrainTime = System.nanoTime()
    val model = ALS.train(hbaseRatings, rank, numIter, lambda)
    val firstTrainEndTime = System.nanoTime() - firstTrainTime
    println("[DEBUG]first training consuming:" + firstTrainEndTime / 1000000000 + "s")

    println("\n[DEBUG]save recommended data to hbase...")
    val firstPutTime = System.nanoTime()

    //为每一个用户产生初始的推荐食物，取top10
    //如果用户在rating中没有数据就会抛出异常
    /*for(i <- 1 to 10){
      val topRatings1 = model.recommendProducts(i, 10)
      for(r <- topRatings1){
        println(r)
      }
    }

     */
    val length = userdata.count()
    println(length)
    var ids  = userdata.take(length.toInt)

      try {
        for (r <- ids){
        var id = r.rowkey
        val topRatings = model.recommendProducts(r.rowkey, 10)
        var recFoods = ""
        for (r <- topRatings) {
          recFoods += r.product + ","
        }
          HbaseDao.put("user", id.toString, "info", "recFoods", recFoods.substring(0, recFoods.length - 1))
      }
      }catch {
        case e:Exception =>{
          print("没有任何评分信息")
          //进一步处理
        }
      }

    val firstPutEndTime = System.nanoTime() - firstPutTime
    println("[DEBUG]finish job consuming:" + firstPutEndTime / 1000000000 + "s")
  }


}
