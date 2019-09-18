package priv.rabbitmq.test;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static priv.rabbitmq.test.CommonParams.*;

/**
 * @author yuqiang lin
 * @description 测试生产者，消息路由情况
 * @email 1098387108@qq.com
 * @date 2019/9/17 2:50 PM
 */
public class ProducerTest {

    public static void main(String[] args) throws IOException, TimeoutException {
        // 单例获取连接工厂
        ConnectionFactory factory = AMQPConnectionFactory.INSTANCE.getInstance();

        try (Connection connection = factory.newConnection()) {

            // 一个连接可以创建多个信道
            Channel channel = connection.createChannel();

            // 声明队列、交换器、绑定键、路由模式等
            initRelations(channel);

            // 先清空队列，以方便查看结果
            clearQueues(channel);

            // 测试不同的路由模式
            testDirect(channel);
            testFanout(channel);
            testTopic(channel);
            testHeaders(channel);

            channel.close();
        }
    }

    /**
     * 清空所有队列中的消息
     *
     * @param channel 信道
     */
    private static void clearQueues(Channel channel) throws IOException {
        for (RelationsEnum relation : RelationsEnum.values()) {
            channel.queuePurge(relation.getQueue());
        }
        System.out.println("queues has bean cleared...");
    }

    /**
     * 测试direct模式
     * routingKey和bindingKey必须全匹配才会成功
     *
     * @param channel 信道
     */
    private static void testDirect(Channel channel) throws IOException {
        String message = "direct!";

        channel.basicPublish(DIRECT_EXCHANGE, DIRECT_BINDING_KEY, MessageProperties.TEXT_PLAIN, message.getBytes());

        channel.basicPublish(DIRECT_EXCHANGE, DIRECT_BINDING_KEY + ".wrong", MessageProperties.TEXT_PLAIN, message.getBytes());

        System.out.println("direct mode test over!");
    }

    /**
     * 测试fanout模式
     * 与routingKey无关
     *
     * @param channel 信道
     */
    private static void testFanout(Channel channel) throws IOException {
        String message = "fount!";

        channel.basicPublish(FANOUT_EXCHANGE, FANOUT_BINDING_KEY, MessageProperties.TEXT_PLAIN, message.getBytes());

        channel.basicPublish(FANOUT_EXCHANGE, "anyOtherKey", MessageProperties.TEXT_PLAIN, message.getBytes());

        System.out.println("fanout mode test over!");

    }

    /**
     * 测试topic模式:
     * "*" 只匹配一个单词；
     * "#" 可以匹配多个单词
     *
     * @param channel 信道
     */
    private static void testTopic(Channel channel) throws IOException {
        String message = "one word!";
        String oneWordKey = TOPIC_ONE_WORD_BINDING_KEY.replace("*", "word");

        channel.basicPublish(TOPIC_EXCHANGE, oneWordKey, MessageProperties.TEXT_PLAIN, message.getBytes());

        message = "multiple words!";
        String multipleWordsKey = TOPIC_ANY_WORDS_BINDING_KEY.replace("#", "multiple.words");

        channel.basicPublish(TOPIC_EXCHANGE, multipleWordsKey, MessageProperties.TEXT_PLAIN, message.getBytes());

        System.out.println("topic mode test over!");

    }

    /**
     * 测试headers模式
     * "all" 发送消息时的headers 必须包含 exchange定义的headers 中的 所有键值对 才会成功
     * "any" 发送消息时的headers 只需包含 exchange定义的headers 中的 任意键值对 就会成功
     *
     * @param channel 信道
     */
    private static void testHeaders(Channel channel) throws IOException {
        String message = "all headers!";

        Map<String, Object> allHeaders = getCommonProperties();

        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties.Builder()
                .headers(allHeaders)
                .build();

        channel.basicPublish(HEADERS_EXCHANGE, getHeadersRoutingKey(), basicProperties, message.getBytes());

        message = "any headers!";

        // 注意，如果这里的headers不仅包含交换器中指定的headers的所有值，还有额外的key-value，也会按匹配成功算
        Map<String, Object> anyHeaders = getCommonProperties();
        anyHeaders.put("age", "20");

        basicProperties = new AMQP.BasicProperties.Builder()
                .headers(anyHeaders)
                .build();

        channel.basicPublish(HEADERS_EXCHANGE, getHeadersRoutingKey(), basicProperties, message.getBytes());

        System.out.println("headers mode test over!");

    }

    /**
     * 初始化队列、交换器，声明一些绑定关系
     *
     * @param channel 信道
     * @throws IOException IO异常
     */
    private static void initRelations(Channel channel) throws IOException {
        for (RelationsEnum relation : RelationsEnum.values()) {
            initRelations(channel, relation);
        }
        System.out.println("queues, exchanges, bindingTypes has been initialed...");
    }

    /**
     * 初始化队列、交换器，声明一些绑定关系
     *
     * @param channel   信道
     * @param relations 关系
     * @throws IOException IO异常
     */
    private static void initRelations(Channel channel, RelationsEnum relations) throws IOException {
        // 声明队列，测试暂不需要持久化
        channel.queueDeclare(relations.getQueue(), false, false, false, Collections.emptyMap());

        // 声明不同类型的交换器，其他属性忽略
        channel.exchangeDeclare(relations.getExchange(), relations.getExchangeType());

        // 绑定队列或交换器，也可以绑定多个
        channel.queueBind(relations.getQueue(), relations.getExchange(), relations.getBindingKey(), relations.getProperties());
    }
}
