package priv.rabbitmq.test.tcc;

import com.google.common.collect.Maps;
import com.rabbitmq.client.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.junit.Test;
import priv.rabbitmq.test.PrepareTest;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static priv.rabbitmq.test.tcc.OrderTccQueueDefinitionTest.*;

/**
 * @author yuqiang lin
 * @description 订单生产者
 * @email 1098387108@qq.com
 * @date 2019/9/24 6:45 PM
 */
public class OrderGenerateProducerTest extends PrepareTest {
    /**
     * 测试消息数量
     */
    static final int ORDER_COUNT = 100;

    /**
     * 保存deliverTag和orderId的对应关系，用于确认消息，发送ack用的
     */
    private static final Map<Long, Integer> DELIVER_TAGS = Maps.newHashMapWithExpectedSize(ORDER_COUNT);
    /**
     * 保存订单信息，用于模拟通过orderId查询数据库
     */
    private static final Map<Integer, Order> ORDERS = Maps.newHashMapWithExpectedSize(ORDER_COUNT);
    /**
     * 确定所有的订单都被处理过了
     */
    private static final CountDownLatch OVER_CDL = new CountDownLatch(ORDER_COUNT);
    /**
     * 确保所有的订单都发送到队列中了
     */
    private static final CountDownLatch CONFIRM_CDL = new CountDownLatch(ORDER_COUNT);

    /**
     * 确保数据一致性
     */
    private static final ReentrantLock LOCK = new ReentrantLock();
    /**
     * 记录不同状态下的定点数量
     */
    private static final Counter COUNTER = new Counter();

    /**
     * 创建订单（保存到mysql），并发送orderId到mq
     *
     * @throws InterruptedException 线程中断
     * @throws IOException          IO异常
     */
    @Test
    public void generateOrders() throws InterruptedException, IOException {
        // 注册确认消息的监听
        registerConfirmListener();
        // 消费支付成功消息
        consumePaySuccessMessage();
        // 消费支付失败消息
        consumePayFailedMessage();
        // 生成测试订单，发送到到支付队列
        generateTestOrders();

        // 等待发送消息结束
        CONFIRM_CDL.await();
        // 等待消费支付结果结束
        OVER_CDL.await();

        System.out.println("最终的订单状态：" + ORDERS.values());
        System.out.println(COUNTER);
    }

    /**
     * 生成测试订单，发送到到支付队列
     *
     * @throws InterruptedException 线程中断
     * @throws IOException          IO异常
     */
    private void generateTestOrders() throws InterruptedException, IOException {
        // 生成测试订单，内容即是订单id
        for (int i = 0; i < ORDER_COUNT; i++) {
            // 随机睡一段时间
            TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(0, 5));
            // TODO 生成订单，并将订单id发动到mq
            Order order = OrderGenerator.generate(i + 1);
            // save db
            try {
                LOCK.lock();
                // TODO 保存到DB
                ORDERS.put(order.getOrderId(), order);
            } finally {
                LOCK.unlock();
            }

            /*
             * 这里要开启消息确认模式，减少消息丢失的概率
             * 不采用事务模式（transaction），因为事务的并发能力没有确认模式（confirm）高
             */
            channel.confirmSelect();
            // 获取下一次发送消息的表示
            long deliverTag = channel.getNextPublishSeqNo();
            // 发送消息到mq
            channel.basicPublish(PAY_EXCHANGE, "", MessageProperties.TEXT_PLAIN, order.getOrderId().toString().getBytes());
            // 该方法是异步的，所以需要记录deliverTag
            try {
                LOCK.lock();
                // TODO 保存orderId和deliverTag的关系
                DELIVER_TAGS.put(deliverTag, order.getOrderId());
                System.out.println("订单发送到mq: " + order);
            } finally {
                LOCK.unlock();
            }
        }
    }

    /**
     * 监听成功支付的订单，修改数据状态
     *
     * @throws IOException IO异常
     */
    private void consumePaySuccessMessage() throws IOException {
        Channel channel = connection.createChannel();
        channel.basicQos(10);
        channel.basicConsume(PAY_SUCCESS_QUEUE, false, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                Integer orderId = Integer.parseInt(new String(body));
                try {
                    LOCK.lock();

                    // TODO 模拟修改数据库
                    Order order = ORDERS.get(orderId);
                    order.setState(OrderState.PAYED);
                    System.out.println("订单付款成功: " + order);

                    channel.basicAck(envelope.getDeliveryTag(), false);
                    OVER_CDL.countDown();

                    COUNTER.incPayedCount();
                } finally {
                    LOCK.unlock();
                }
            }
        });
    }

    /**
     * 监听支付的失败订单，修改数据状态
     *
     * @throws IOException IO异常
     */
    private void consumePayFailedMessage() throws IOException {
        Channel channel = connection.createChannel();
        channel.basicQos(10);
        channel.basicConsume(PAY_FAILED_QUEUE, false, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                Integer orderId = Integer.parseInt(new String(body));
                try {
                    LOCK.lock();

                    // TODO 模拟修改数据库
                    Order order = ORDERS.get(orderId);
                    order.setState(OrderState.PAY_FAILED);
                    System.out.println("订单付款失败: " + order);

                    channel.basicAck(envelope.getDeliveryTag(), false);
                    OVER_CDL.countDown();

                    COUNTER.incPayFailedCount();
                } finally {
                    LOCK.unlock();
                }
            }
        });
    }

    /**
     * 注册确认消息的监听，来保存消息确实发送到了mq
     */
    private void registerConfirmListener() {
        channel.addConfirmListener(new ConfirmListener() {
            @Override
            public void handleAck(long deliveryTag, boolean multiple) {
                try {
                    LOCK.lock();

                    Integer orderId = DELIVER_TAGS.get(deliveryTag);
                    Order order = ORDERS.get(orderId);
                    // TODO 模拟修改数据库
                    order.setState(OrderState.INITIALED);

                    System.out.println("订单发送到mq成功: " + order);
                    CONFIRM_CDL.countDown();
                } finally {
                    LOCK.unlock();
                }
            }

            @Override
            public void handleNack(long deliveryTag, boolean multiple) {
                try {
                    LOCK.lock();

                    Integer orderId = DELIVER_TAGS.get(deliveryTag);
                    Order order = ORDERS.get(orderId);
                    // TODO 模拟修改数据库
                    order.setState(OrderState.INITIAL_FAILED);

                    System.out.println("订单发送到mq失败: " + order);
                    CONFIRM_CDL.countDown();
                } finally {
                    LOCK.unlock();
                }
            }
        });
    }
}
