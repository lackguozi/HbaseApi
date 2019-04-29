package com.project.dao

/**
  * Created by luckguozi on 2019/4/6.
  */
import com.project.domain.Hbaseinfo
import com.tian.utils.Hbase
import org.apache.hadoop.hbase.client.{Get, Put, Scan}
import org.apache.hadoop.hbase.util.Bytes

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
object HbaseDao {
  val tableName = "hbase_test"
  val cf = "info"
  val qualifer = "name"

  /**
    * @description 保存
    * @param list 集合
    */
  def save(list: ListBuffer[Hbaseinfo]): Unit = {

    val table = Hbase.getInstance().getTable(tableName)

    for(ele <- list) {
      table.incrementColumnValue(Bytes.toBytes(ele.day_course),
        Bytes.toBytes(cf),
        Bytes.toBytes(qualifer),
        ele.click_count)
    }
  }
  //根据rowkey查询值
  def count(day_course: String):Long = {
    val table = Hbase.getInstance().getTable(tableName)
    val get = new Get(Bytes.toBytes(day_course))
    val value = table.get(get).getValue(cf.getBytes, qualifer.getBytes)
    if(value == null) {
      0L
    }else{
      Bytes.toLong(value)
    }
  }
  //插入数据
  def put(tableName: String, rowKey: String, family: String, qualifier: String, value: String) {
    val table = Hbase.getInstance().getTable(tableName)
    val put = new Put(Bytes.toBytes(rowKey))
    put.addColumn(Bytes.toBytes(family), Bytes.toBytes(qualifier), Bytes.toBytes(value))
    table.put(put)
  }
  //获得所有行值
  def getAllRow(tableName: String): Array[String] = {
    val table = Hbase.getInstance().getTable(tableName)
    val resultScaner = table.getScanner(new Scan())
    val resIter = resultScaner.iterator()
    var resArr = new ArrayBuffer[String]()
    while (resIter.hasNext) {
      val res = resIter.next()
      if (res != null && !res.isEmpty) {
        resArr += Bytes.toString(res.getRow)
      }
    }
    resArr.toArray
  }

}
