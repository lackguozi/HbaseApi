package com.project.spark

import com.project.dao.HbaseDao
import com.project.domain.Rateinfo

import org.apache.log4j.{Level, Logger}
import org.apache.spark._
/**
  * Created by luckguozi on 2019/4/6.
  */
object RatingToHbase {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
    val sparkConf = new SparkConf().setAppName("RatingToHbase").setMaster("local")
    val sparkContext = new SparkContext(sparkConf)
    val rating = sparkContext.textFile("hdfs://localhost:9000/train/hadoop/data/data1.txt").map { lines =>
      val fields = lines.split("::")
      Rateinfo(fields(0), fields(1),fields(2))
    }.cache()
    var rowId2 = 1
    rating.foreach { rating =>
      HbaseDao.put("rating", rowId2.toString, "info", "uid", rating.uid)
      HbaseDao.put("rating", rowId2.toString, "info", "fid", rating.fid)
      HbaseDao.put("rating", rowId2.toString, "info", "rating", rating.rating)
      rowId2 = rowId2 + 1
    }
    sparkContext.stop()
  }

}
