package priv.rabbitmq.test.delay.queue;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.junit.Test;
import priv.rabbitmq.test.PrepareTest;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static priv.rabbitmq.test.delay.queue.OrderDelayQueueDefinitionTest.*;

/**
 * @author yuqiang lin
 * @description 消费过期的订单，取消订单
 * @email 1098387108@qq.com
 * @date 2019/9/24 6:45 PM
 */
public class OrderCancelConsumerTest extends PrepareTest {
    private static final int CANCEL_ORDER_COUNT = OrderGenerateProducerTest.ORDER_COUNT;

    /**
     * 消费死信队列中的过期订单队列 {@link OrderDelayQueueDefinitionTest#ORDER_CANCEL_QUEUE} 的消息
     *
     * @throws IOException          IO异常
     * @throws InterruptedException 线程中断异常
     */
    @Test
    public void consumeCancelOrders() throws IOException, InterruptedException {
        // 等待所有过期消息都被消费后再退出主线程
        final CountDownLatch consumedCDL = new CountDownLatch(CANCEL_ORDER_COUNT);

        // 消费过期订单
        channel.basicConsume(ORDER_CANCEL_QUEUE, false, new OrderCancelConsumer(consumedCDL, channel));

        // 等待消费结束
        consumedCDL.await();

        System.out.println(CANCEL_ORDER_COUNT + " orders has canceled.");
    }

    /**
     * 用于处理过期订单消息
     */
    private class OrderCancelConsumer extends DefaultConsumer {
        /**
         * 每处理一个过期订单，就-1
         */
        private CountDownLatch consumedCDL;

        private OrderCancelConsumer(CountDownLatch consumedCDL, Channel channel) {
            super(channel);
            this.consumedCDL = consumedCDL;
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            // TODO 处理过期消息，比如修改数据库订单状态为"已取消"等一些操作
            String orderId = new String(body);

            long createdTime = Long.parseLong(orderId);
            long currentTime = System.currentTimeMillis();

            System.out.println("cancel order, orderId: " + new String(body) + ", expiredMillis: " + (currentTime - createdTime));
            // 确认已消费
            channel.basicAck(envelope.getDeliveryTag(), false);
            // 处理数-1
            consumedCDL.countDown();
        }
    }
}
