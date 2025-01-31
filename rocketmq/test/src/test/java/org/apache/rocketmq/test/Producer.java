package org.apache.rocketmq.test;

import io.netty.util.internal.StringUtil;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import java.util.concurrent.TimeUnit;

/**
 * @create: 2019-06-21 11:14
 * @description:
 **/
public class Producer {

    public static void main(String[] args) throws MQClientException,
            InterruptedException{
        /**
         * 一个应用创建一个Producer，由应用来维护此对象，可以设置为全局对象或者单例<br>
         * 注意：ProducerGroupName需要由应用来保证唯一<br>
         * ProducerGroup这个概念发送普通的消息时，作用不大，但是发送分布式事务消息时，比较关键，
         * 因为服务器会回查这个Group下的任意一个Producer
         */
        final DefaultMQProducer producer = new DefaultMQProducer("ProducerGroupName");
        producer.setNamesrvAddr("localhost:9876");
        producer.setInstanceName("Producer");
        /**
         * Producer对象在使用之前必须要调用start初始化，初始化一次即可<br>
         * 注意：切记不可以在每次发送消息时，都调用start方法
         */
        producer.start();

        /**
         * 下面这段代码表明一个Producer对象可以发送多个topic，多个tag的消息。
         * 注意：send方法是同步调用，只要不抛异常就标识成功。但是发送成功也可会有多种状态，<br>
         * 例如消息写入Master成功，但是Slave不成功，这种情况消息属于成功，但是对于个别应用如果对消息可靠性要求极高，<br>
         * 需要对这种情况做处理。另外，消息可能会存在发送失败的情况，失败重试由应用来处理。
         */
        for(int m =0;m<1;m++){
            for (int i = 0; i < 100000; i++){
                try {
                    {
                        Message msg = new Message("TopicTest2",// topic
                                "push",// tag
                                "OrderID00"+i,// key
                                ("Hello MetaQ"+i).getBytes());// body
                        SendResult sendResult = producer.send(msg);
                        System.out.print(i);
                        System.out.print(sendResult);
                        System.out.print(",");
                        System.out.print((msg.getBody()));
                        System.out.println(",");
                    }
//    	            {
//    	                Message msg = new Message("TopicTest2",// topic
//    	                      "TagB",// tag
//    	                      "OrderID0034",// key
//    	                      ("Hello MetaQB").getBytes());// body
//    	                SendResult sendResult = producer.send(msg);
//    	                System.out.println(sendResult);
//    	            }
                    //
//    	            {
//    	                Message msg = new Message("TopicTest3",// topic
//    	                      "TagC",// tag
//    	                      "OrderID061",// key
//    	                      ("Hello MetaQC").getBytes());// body
//    	                SendResult sendResult = producer.send(msg);
//    	                System.out.println(sendResult);
//    	            }
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }catch(Exception e) {
                    e.printStackTrace();
                }
//    	         TimeUnit.MILLISECONDS.sleep(10);
            }
        }
        /**
         * 应用退出时，要调用shutdown来清理资源，关闭网络连接，从MetaQ服务器上注销自己
         * 注意：我们建议应用在JBOSS、Tomcat等容器的退出钩子里调用shutdown方法
         */
//    producer.shutdown();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                producer.shutdown();
            }
        }));
        System.exit(0);
    }
}
