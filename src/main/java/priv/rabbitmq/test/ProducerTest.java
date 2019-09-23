package priv.rabbitmq.test;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.MessageProperties;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

        channel.basicPublish(DIRECT_EXCHANGE, WRONG_BINDING_KEY, MessageProperties.TEXT_PLAIN, message.getBytes());

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

        channel.basicPublish(FANOUT_EXCHANGE, WRONG_BINDING_KEY, MessageProperties.TEXT_PLAIN, message.getBytes());

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

        channel.basicPublish(HEADERS_EXCHANGE, getMeaninglessRoutingKey(), basicProperties, message.getBytes());

        message = "any headers!";

        // 注意，如果这里的headers不仅包含交换器中指定的headers的所有值，还有额外的key-value，也会按匹配成功算
        Map<String, Object> anyHeaders = getCommonProperties();
        anyHeaders.put("age", "20");

        basicProperties = new AMQP.BasicProperties.Builder()
                .headers(anyHeaders)
                .build();

        channel.basicPublish(HEADERS_EXCHANGE, getMeaninglessRoutingKey(), basicProperties, message.getBytes());

        System.out.println("headers mode test over!");
    }

    /**
     * 测试发布消息的immediate
     * 当交换器路由到的队列上不存在任何消费者时，会触发returnListener监听
     * 注：这个参数一般不使用，会影响性能，用 DLX + TTL 代替
     *
     * @throws IOException          IO异常
     * @throws InterruptedException 线程中断
     */
    @Test
    public void testImmediate() throws IOException, InterruptedException {
        String message = "no consumer.";

        // 监听路由失败的消息
        channel.addReturnListener((replyCode, replyText, exchange, routingKey, properties, body) -> System.out.println("replyCode: " + replyCode
                + "\nreplyText: " + replyText
                + "\nexchange: " + exchange
                + "\nroutingKey: " + routingKey
                + "\nmessage: " + new String(body)));

        // 设置mandatory为true，则如果消息路由失败，会返回错误信息
        channel.basicPublish(DIRECT_EXCHANGE, DIRECT_BINDING_KEY, false, true, EMPTY_PROPERTIES, message.getBytes());

        // 等待listener触发
        TimeUnit.SECONDS.sleep(1);

        System.out.println("immediate mode test over!");
    }

    /**
     * 测试发布消息的mandatory
     * 当消息无法路由到任何一个队列上时，则会触发returnListener监听
     * 注意：前提是未设置备份队列，如果设置了，则会走备份队列的逻辑，不会触发returnListener
     *
     * @throws IOException          IO异常
     * @throws InterruptedException 线程中断
     */
    @Test
    public void testMandatory() throws IOException, InterruptedException {
        String message = "routing failed.";

        // 监听路由失败的消息
        channel.addReturnListener((replyCode, replyText, exchange, routingKey, properties, body) -> System.out.println("replyCode: " + replyCode
                + "\nreplyText: " + replyText
                + "\nexchange: " + exchange
                + "\nroutingKey: " + routingKey
                + "\nmessage: " + new String(body)));

        // 设置mandatory为true，则如果消息路由失败，会返回错误信息
        channel.basicPublish(TOPIC_EXCHANGE, WRONG_BINDING_KEY, true, EMPTY_PROPERTIES, message.getBytes());

        // 若交换器设置了备份队列，则不会返回路由失败的消息，即不会触发listener
        channel.basicPublish(DIRECT_EXCHANGE, WRONG_BINDING_KEY, true, EMPTY_PROPERTIES, message.getBytes());

        // 等待listener触发
        TimeUnit.SECONDS.sleep(1);

        System.out.println("mandatory mode test over!");
    }

    /**
     * 测试AE（备份交换器）
     * 若一个交换器绑定了备份交换器，当消息无法路由到任何一个队列上时，则会路由到备份交换器上
     * 注意：一般声明的备份交换器为FANOUT类型，尽量保证消息能够被保存下来
     *
     * @throws IOException IO异常
     */
    @Test
    public void testAlternateQueue() throws IOException {
        String message = "routing failed.";

        // 若交换器设置了备份队列，则消息会进入备份队列
        channel.basicPublish(DIRECT_EXCHANGE, WRONG_BINDING_KEY, false, EMPTY_PROPERTIES, message.getBytes());

        System.out.println("alternate-exchange mode test over!");
    }

    /**
     * 测试DLX（死信队列），TTL（消息过期）
     * 注意：进入死信队列有几种情况：1.消息过期；2.队列长度超出限制；3.消息被拒绝
     *
     * @throws IOException IO异常
     */
    @Test
    public void testDeadLetterQueue() throws IOException {
        String message = "expired message.";

        // 发送5条消息，因为队列长度是3，所以会有2条直接进入死信队列，另外3条会在5秒后进入死信队列
        for (int i = 0; i < 5; i++) {
            channel.basicPublish(FANOUT_EXCHANGE, getMeaninglessRoutingKey(), MessageProperties.TEXT_PLAIN, (message + i).getBytes());
        }

        System.out.println("dead-letter-exchange mode test over!");
    }
}
