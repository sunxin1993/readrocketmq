package org.apache.rocketmq.test;


/**
 * Created by yyp on 15-12-18.
 */
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

public class ProducerEasy {
    public static void main(String[] args) {
        DefaultMQProducer producer = new DefaultMQProducer("Producer");
        //设置nameserver地址 可以多个
        producer.setNamesrvAddr("192.168.0.1:9876");
        try {
            producer.start();
            int i = 0;

            while (true) {
                i++;
                Message msg = new Message("PushTopic",
                        "push",
                        "1",
                        ("Just for yyp" + i).getBytes());
                SendResult result = producer.send(msg);

       /* msg = new Message("PushTopic",
    "push",
    "2",
    "Just for test.".getBytes());

    result = producer.send(msg);
    System.out.println("id:" + result.getMsgId() +
    " result:" + result.getSendStatus());*/

                msg = new Message("PullTopic",
                        "pull",
                        "1",
                        "Just for test.".getBytes());
                result = producer.send(msg);
                System.out.println("id:" + result.getMsgId() +
                        " result:" + result.getSendStatus());
                if (i % 10 == 0) {
                    Thread.sleep(20000);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // producer.shutdown();
        }
    }
}