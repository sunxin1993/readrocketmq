package org.apache.rocketmq.test.tls;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * @create: 2019-06-28 11:20
 * @description:
 **/
public class TransactionProducer {

    public static void main(String[] args) throws MQClientException, InterruptedException {
        //01 new 一个有事务基因的生产者
        TransactionMQProducer producer = new TransactionMQProducer("ProducerGroup");
        //02 注册
        producer.setNamesrvAddr("127.0.0.1:9876");
        //03 开启
        producer.start();
        /**
         * 04 生产者设置事务监听器，匿名内部类new一个事务监听器，
         * 重写“执行本地事务”和“检查本地事务”两个方法，返回值都为
         * “本地事务状态”
         */
        producer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
                String tag = msg.getTags();
                if(tag.equals("Transaction1")) {
                    System.out.println("这里处理业务逻辑,比如操作数据库，失败情况下进行回滚");
                    //如果失败，再次给MQ发送消息
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
                return LocalTransactionState.COMMIT_MESSAGE;
            }
            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt msg) {
                System.out.println("state -- "+new String(msg.getBody()));
                return LocalTransactionState.COMMIT_MESSAGE;
            }
        });
        for(int i=0;i<2;i++) {
            try {
                // 05 准备要发送的message，名字，标签，内容
                Message msg = new Message("TopicTransaction","Transaction" + i,("Hello RocketMQ "+i).getBytes("UTF-8"));
                // 06 用发送事务特有的方法发送消息，而不是简单的producer.send(msg);
                SendResult sendResult = producer.sendMessageInTransaction(msg, null);
                System.out.println(msg.getBody());
            } catch (Exception e) {
                e.printStackTrace();
                Thread.sleep(1000);
            }
        }
        // 07 关闭
        producer.shutdown();
    }
}
