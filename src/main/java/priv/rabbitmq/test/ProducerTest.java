package priv.rabbitmq.test;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.MessageProperties;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static priv.rabbitmq.test.CommonParams.*;

/**
 * @author yuqiang lin
 * @description 测试生产者，消息路由情况
 * @email 1098387108@qq.com
 * @date 2019/9/17 2:50 PM
 */
public class ProducerTest extends PrepareTest {
    /**
     * 测试direct模式
     * routingKey和bindingKey必须全匹配才会成功
     *
     * @throws IOException IO异常
     */
    @Test
    public void testDirect() throws IOException {
        String message = "direct!";

        channel.basicPublish(DIRECT_EXCHANGE, DIRECT_BINDING_KEY, MessageProperties.TEXT_PLAIN, message.getBytes());

        channel.basicPublish(DIRECT_EXCHANGE, DIRECT_BINDING_KEY + ".wrong", MessageProperties.TEXT_PLAIN, message.getBytes());

        System.out.println("direct mode test over!");
    }

    /**
     * 测试fanout模式
     * 与routingKey无关
     *
     * @throws IOException IO异常
     */
    @Test
    public void testFanout() throws IOException {
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
     * @throws IOException IO异常
     */
    @Test
    public void testTopic() throws IOException {
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
     * @throws IOException IO异常
     */
    @Test
    public void testHeaders() throws IOException {
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
}
