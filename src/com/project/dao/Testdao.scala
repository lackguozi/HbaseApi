package com.project.dao

/**
  * Created by luckguozi on 2019/4/7.
  */
object Testdao {
  def main(args: Array[String]): Unit = {
    HbaseDao.put("hbase_test", "005", "info", "name", "ming")
    print("ceshihengong")
  }
}
