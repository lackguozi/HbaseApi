package com.tian.utils;


/**
 * Created by luckguozi on 2019/4/6.
 */
public class Ttst {
    public static void main(String[] args)  {
        Hbase test= Hbase.getInstance();
        
        test.put("hbase_test", "002", "info", "name", "john");
        System.out.println("charuchengong");



    }
}
