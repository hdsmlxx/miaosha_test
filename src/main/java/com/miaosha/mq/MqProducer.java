package com.miaosha.mq;

import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class MqProducer {

    private DefaultMQProducer producer;

    /**
     * 使用@Value注解将配置文件中的value注入到属性
     */
    @Value("${mq.nameserver.addr}")
    private String nameAddr;

    @Value("${mq.topicname}")
    private String topicName;

    /**
     * PostConstruct注解表示在该bean初始化之后会执行被注解的方法
     */
    @PostConstruct
    public void init() throws MQClientException {

        //做 producer 的初始化
        producer = new DefaultMQProducer("producer_group");
        producer.setNamesrvAddr(nameAddr);
        producer.start();

    }

    /**
     * 同步库存扣减消息
     *  1定义消息体
     *  2封装消息
     *  3发送消息
     * @param itemId
     * @param amount
     * @return
     */
    public boolean asyncReduceStock(Integer itemId, Integer amount) {
        Map<String, Object> bodyMap = new HashMap<>(16);
        bodyMap.put("itemId", itemId);
        bodyMap.put("amount", amount);
        Message message = new Message(topicName, "increase",
                JSON.toJSON(bodyMap).toString().getBytes(Charset.forName("UTF-8")));
        try {
            producer.send(message);
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        } catch (RemotingException e) {
            e.printStackTrace();
            return false;
        } catch (MQBrokerException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
