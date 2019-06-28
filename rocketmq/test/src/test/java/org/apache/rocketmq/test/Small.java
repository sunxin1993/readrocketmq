package org.apache.rocketmq.test;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import sun.nio.cs.ext.MacArabic;

import java.util.Map;
import java.util.TreeMap;

/**
 * @create: 2019-06-28 10:20
 * @description:
 **/
public class Small {

    @Test

    public void test1(){
        Map<Integer,String> map= new TreeMap<>();
        map.put(10, "10");
        map.put(1, "1");
        map.put(9, "9");
        System.out.println(JSON.toJSONString(map));
    }
}
