package com.project.spark

import com.project.dao.HbaseDao
import org.apache.log4j.{Level, Logger}
import org.apache.spark._
/**
  * Created by luckguozi on 2019/4/6.
  * 用户数据写入到hbase
  */
object UserToHbase {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

    val conf = new SparkConf().setMaster("local").setAppName("UserToHbase")
    val sc = new SparkContext(conf)


    for (i <- 1 to 800){
      val m =i.toString
      val pwd ="bs2019"
      HbaseDao.put("user",i.toString,"info","uid",m)
      HbaseDao.put("user",i.toString,"info","pwd",pwd)
    }
    //sparkContext.stop()
  }

}
