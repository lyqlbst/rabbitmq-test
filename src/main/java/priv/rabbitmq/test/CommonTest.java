package priv.rabbitmq.test;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeoutException;

import static priv.rabbitmq.test.CommonParams.*;

/**
 * @author yuqiang lin
 * @description 一个测试类
 * @email 1098387108@qq.com
 * @date 2019/9/17 2:50 PM
 */
public class CommonTest {

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);

        factory.setUsername(USER);
        factory.setPassword(PASSWORD);

        try (Connection connection = factory.newConnection()) {
            Channel channel = connection.createChannel();
            // 声明队列
            channel.queueDeclare(COMMON_QUEUE, false, false, false, Collections.emptyMap());
            // 声明交换器，四种模式
            channel.exchangeDeclare(COMMON_EXCHANGE, BuiltinExchangeType.DIRECT);
//            channel.exchangeDeclare(COMMON_EXCHANGE, BuiltinExchangeType.FANOUT);
//            channel.exchangeDeclare(COMMON_EXCHANGE, BuiltinExchangeType.TOPIC);
            // 绑定
            channel.queueBind(COMMON_QUEUE, COMMON_EXCHANGE, COMMON_BINDING_KEY);
            // 推送消息
            String message = "hello world!";
            channel.basicPublish(COMMON_EXCHANGE, COMMON_BINDING_KEY, MessageProperties.TEXT_PLAIN, message.getBytes());
            // 关闭资源
            channel.close();
        }
    }
}
