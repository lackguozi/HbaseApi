package com.project.spark

import com.project.domain.Foodinfo
import com.project.dao.HbaseDao
import org.apache.log4j.{Level, Logger}
import org.apache.spark._
import org.apache.hadoop.hbase.util.Bytes
/**
  * Created by luckguozi on 2019/4/6.
  * 餐饮数据写入到hbase，
  */
object FoodToHbase {
  def main(args:Array[String])={

    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
    val sparkConf = new SparkConf().setAppName("FoodToHbase").setMaster("local")
    val sparkContext = new SparkContext(sparkConf)
    //最好不要使用txt来存储数据，会发生很多的编码问题
    val food = sparkContext.textFile("hdfs://localhost:9000/train/hadoop/data/foodu.txt").map { lines =>
      val fields = lines.split("::")
      Foodinfo(fields(0), fields(1),fields(2))
    }.cache()
    var i =1
    food.foreach{
      food=>
        HbaseDao.put("food", i.toString, "info", "fid", food.fid)
        HbaseDao.put("food", i.toString, "info", "name",food.name)
        HbaseDao.put("food", i.toString, "info", "url", food.url)
        i+=1

    }
    sparkContext.stop()
  }

}
