package priv.rabbitmq.test.delay.queue;

import com.rabbitmq.client.MessageProperties;
import org.junit.Test;
import priv.rabbitmq.test.PrepareTest;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static priv.rabbitmq.test.delay.queue.OrderDelayQueueDefinitionTest.*;

/**
 * @author yuqiang lin
 * @description 订单生产者
 * @email 1098387108@qq.com
 * @date 2019/9/24 6:45 PM
 */
public class OrderGenerateProducerTest extends PrepareTest {
    static final int ORDER_COUNT = 50;

    /**
     * 生成订单
     *
     * @throws InterruptedException 线程中断异常
     * @throws IOException          IO异常
     */
    @Test
    public void generateOrders() throws InterruptedException, IOException {
        // 生成测试订单，内容即是订单id
        for (int i = 0; i < ORDER_COUNT; i++) {
            // 随机睡一段时间
            TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(100, 500));

            // TODO 生成订单，并将订单id发动到mq
            String orderId = System.currentTimeMillis() + "";

            channel.basicPublish(ORDER_EXCHANGE, "", MessageProperties.TEXT_PLAIN, orderId.getBytes());

            System.out.println("generate order, orderId: " + orderId);
        }

        System.out.println(ORDER_COUNT + " orders has generated.");
    }
}
