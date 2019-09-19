package priv.rabbitmq.test;

import com.google.common.collect.Lists;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static priv.rabbitmq.test.CommonParams.*;

/**
 * @author yuqiang lin
 * @description 测试消费者，消费方式
 * @email 1098387108@qq.com
 * @date 2019/9/19 3:10 PM
 */
public class ConsumerTest extends PrepareTest {

    /**
     * 测试拉模式，一次只能拉取一条（第一条），效率较低
     *
     * @throws IOException IO异常
     */
    @Test
    public void basicGet() throws IOException {
        // 若不设置自动确认，则队列中的消息不会自动删除
        GetResponse response = channel.basicGet(DIRECT_QUEUE, false);
        System.out.println("get message: " + getBody(response));

        channel.basicGet(DIRECT_QUEUE, true);
        System.out.println("get message: " + getBody(response));
    }

    /**
     * 测试推模式，一次可以推多条，效率高
     * Qos限制一次推过来的消息数
     *
     * @throws IOException          IO异常
     * @throws InterruptedException 中断异常
     */
    @Test
    public void basicConsume() throws IOException, InterruptedException {
        // 若设置了该属性，则会限制一次推送过来的消息数
        channel.basicQos(2);
        // 若未设置自动确认，则队列中的消息不会清空
        channel.basicConsume(DIRECT_QUEUE, false, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                System.out.println("consumed message: " + getBody(body));

            }
        });
        // 可能有多条消息一次都推过来
        TimeUnit.SECONDS.sleep(1);
    }

    /**
     * 测试确认机制
     * 若basicAck中的multiple设置为true，则会确认比该deliveryTag小的所有消息，
     * 但是必须是该channel中消费过的消息
     *
     * @throws IOException IO异常
     */
    @Test
    public void basicAck() throws IOException, InterruptedException {
        int count = 3;
        List<Long> deliveryTags = Lists.newArrayListWithCapacity(count);

        channel.basicQos(1);
        // 消费消息，但不确认
        for (int i = 0; i < count; i++) {
            channel.basicConsume(DIRECT_QUEUE, false, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                    deliveryTags.add(envelope.getDeliveryTag());
                    System.out.println("consumed message: " + getBody(body));
                }
            });
        }

        // 等待推送完成
        TimeUnit.SECONDS.sleep(1);

        // 找到最大的deliveryTag
        Optional<Long> maxDeliveryTagOpt = deliveryTags.stream().max(Comparator.naturalOrder());

        if (!maxDeliveryTagOpt.isPresent()) {
            System.out.println("no message consumed.");
            return;
        }

        // 若multiple设置为true，则会确认比该deliveryTag小的所有消息
        channel.basicAck(maxDeliveryTagOpt.get(), true);
    }

    /**
     * 获取body，转为字符串
     *
     * @param response 返回体
     * @return 字符串body
     */
    private String getBody(GetResponse response) {
        if (Objects.isNull(response)) {
            return "empty response.";
        }
        return getBody(response.getBody());
    }

    /**
     * 获取body，转为字符串
     *
     * @param bytes 字节流
     * @return 字符串body
     */
    private String getBody(byte[] bytes) {
        return new String(bytes);
    }
}
