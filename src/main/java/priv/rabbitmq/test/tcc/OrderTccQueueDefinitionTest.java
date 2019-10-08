package priv.rabbitmq.test.tcc;

import com.google.common.collect.Maps;
import com.rabbitmq.client.BuiltinExchangeType;
import org.junit.Test;
import priv.rabbitmq.test.PrepareTest;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * @author yuqiang lin
 * @description 在分布式（微服务）的场景下，RabbitMQ可以实现基于BASE理论的数据最终一致性效果，
 * <p>
 * 假如有一个简单的场景，A服务（订单）需要调用B服务（支付）的业务，然后将结果保存到数据库中，那么就会存在一个问题：
 * 如果A服务的方法执行成功，而在调用B服务时出现问题（比如阻塞或是失败），则整个业务就失败了，这是不希望发生的。
 * <p>
 * 然而可以利用rabbitmq来传递消息，允许一段时间的数据不一致，但会达到最终一致性，来确服务的稳定性
 * <p>
 * 定义三个队列（持久化），分别是：
 * 1.任务队列{@link #PAY_QUEUE}（A服务将orderId发送到该队列，B服务消费该队列）；
 * 2.消费成功队列{@link #PAY_SUCCESS_QUEUE}（若B服务消费成功，则将成功的消息发送到该队列，A服务改变订单的支付状态为成功）；
 * 3.消费失败队列{@link #PAY_FAILED_QUEUE}（若B服务消费失败，则将失败的消息发动到该队列，A服务改变订单的支付状态为失败）
 * @email 1098387108@qq.com
 * @date 2019/9/29 3:14 PM
 */
public class OrderTccQueueDefinitionTest extends PrepareTest {
    static final String PAY_EXCHANGE = "priv.rabbitmq.test.pay.exchange";
    static final String PAY_SUCCESS_EXCHANGE = "priv.rabbitmq.test.pay.success.exchange";
    static final String PAY_FAILED_EXCHANGE = "priv.rabbitmq.test.pay.failed.exchange";

    static final String PAY_QUEUE = "priv.rabbitmq.test.pay.queue";
    static final String PAY_SUCCESS_QUEUE = "priv.rabbitmq.test.pay.success.queue";
    static final String PAY_FAILED_QUEUE = "priv.rabbitmq.test.pay.failed.queue";

    @Test
    public void defineRelations() throws IOException {
        // 定义支付队列，订单服务会发送需要支付的orderId到该队列，支付服务会订阅该队列，执行支付流程
        // 死信交换器，支付失败后拒绝消息
        Map<String, Object> properties = Maps.newHashMap();
        properties.put("x-dead-letter-exchange", PAY_FAILED_EXCHANGE);
        channel.queueDeclare(PAY_QUEUE, true, false, false, properties);
        channel.exchangeDeclare(PAY_EXCHANGE, BuiltinExchangeType.FANOUT, true, false, false, Collections.emptyMap());
        channel.queueBind(PAY_QUEUE, PAY_EXCHANGE, "", Collections.emptyMap());

        // 定义支付成功队列，支付服务执行成功后，将该orderId发送到该队列，订单服务会订阅该队列，修改订单状态为支付成功，然后调用其他接口，如发货等等
        channel.queueDeclare(PAY_SUCCESS_QUEUE, true, false, false, Collections.emptyMap());
        channel.exchangeDeclare(PAY_SUCCESS_EXCHANGE, BuiltinExchangeType.FANOUT, true, false, false, Collections.emptyMap());
        channel.queueBind(PAY_SUCCESS_QUEUE, PAY_SUCCESS_EXCHANGE, "", Collections.emptyMap());

        // 定义支付失败队列，支付服务执行失败后，将该orderId发送到该队列，订单服务订阅该队列，修改订单状态为支付失败，可以执行相应操作，比如发送支付失败的消息等等
        channel.queueDeclare(PAY_FAILED_QUEUE, true, false, false, Collections.emptyMap());
        channel.exchangeDeclare(PAY_FAILED_EXCHANGE, BuiltinExchangeType.FANOUT, true, false, false, Collections.emptyMap());
        channel.queueBind(PAY_FAILED_QUEUE, PAY_FAILED_EXCHANGE, "", Collections.emptyMap());
    }
}
