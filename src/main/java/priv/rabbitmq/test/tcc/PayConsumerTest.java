package priv.rabbitmq.test.tcc;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import org.junit.Test;
import priv.rabbitmq.test.PrepareTest;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static priv.rabbitmq.test.tcc.OrderTccQueueDefinitionTest.*;

/**
 * @author yuqiang lin
 * @description 支付服务，监听支付队列，处理订单，然后发送消息到相应队列
 * @email 1098387108@qq.com
 * @date 2019/9/29 6:00 PM
 */
public class PayConsumerTest extends PrepareTest {
    /**
     * 用于计算随机数
     */
    private static final List NUMBERS = IntStream
            .range(0, 10)
            .boxed()
            .collect(Collectors.toList());

    /**
     * 确定所有订单都被消费了
     */
    private static final int PAY_COUNT = OrderGenerateProducerTest.ORDER_COUNT;
    /**
     * 记录不同状态下的定点数量
     */
    private static final Counter COUNTER = new Counter();

    @Test
    public void consumeOrders() throws IOException, InterruptedException {
        // 处理完所有的订单后退出
        CountDownLatch payCdl = new CountDownLatch(PAY_COUNT);
        // 一次拉10条数据，提高性能
        channel.basicQos(10);
        // 关闭自动确认，只有处理完了再ack
        channel.basicConsume(PAY_QUEUE, false, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String orderId = new String(body);
                // TODO 处理支付操作
                // 模拟支付操作
                boolean payed = pay();

                long deliveryTag = envelope.getDeliveryTag();
                // 成功ack，失败nack
                if (payed) {
                    channel.basicAck(deliveryTag, false);
                    System.out.println("订单支付成功，orderId: " + orderId);
                    // 发送支付成功的消息
                    channel.basicPublish(PAY_SUCCESS_EXCHANGE, "", MessageProperties.TEXT_PLAIN, orderId.getBytes());
                    COUNTER.incPayedCount();
                } else {
                    // 会自动跳到死信队列
                    channel.basicNack(deliveryTag, false, false);
                    System.out.println("订单支付失败，orderId: " + orderId);
                    COUNTER.incPayFailedCount();
                }

                payCdl.countDown();
            }
        });

        payCdl.await();
        System.out.println("订单支付完成");
        System.out.println(COUNTER);
    }

    /**
     * 模拟支付操作，90%概率成功，10%概率失败
     *
     * @return 是否成功
     */
    private boolean pay() {
        final int size = NUMBERS.size();
        final int target = ThreadLocalRandom.current().nextInt(0, size);

        Collections.shuffle(NUMBERS);
        return !NUMBERS.get(0).equals(target);
    }
}
