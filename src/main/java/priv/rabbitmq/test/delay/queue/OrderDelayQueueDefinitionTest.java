package priv.rabbitmq.test.delay.queue;

import com.google.common.collect.Maps;
import com.rabbitmq.client.BuiltinExchangeType;
import org.junit.Test;
import priv.rabbitmq.test.PrepareTest;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * @author yuqiang lin
 * @description 定义延迟队列，可以用来实现订单自动取消
 * 1.定义 订单交换器(orderExchange)-{@link #ORDER_EXCHANGE} 和订单队列(orderQueue)-{@link #ORDER_QUEUE}，
 * 其中orderQueue设置消息过期时间-{@code x-message-ttl}(ttl)，设置死信交换器-{@code x-dead-letter-exchange}(dlx)
 * 2.定义 订单取消交换器(orderCancelExchange)-{@link #ORDER_CANCEL_EXCHANGE}充当dlx，同时绑定死信队列(orderCancelQueue)-{@link #ORDER_CANCEL_QUEUE}
 * <p>
 * 订单消息流转过程：
 * 1.{@link OrderGenerateProducerTest}发送订单消息到orderExchange中；
 * 2.订单消息过期后，会自动路由到orderCancelQueue中，会有专门的消费者去解决过期的订单
 * @email 1098387108@qq.com
 * @date 2019/9/24 6:29 PM
 */
public class OrderDelayQueueDefinitionTest extends PrepareTest {
    private static final int EXPIRED_MILLIS = 10 * 1000;

    static final String ORDER_EXCHANGE = "priv.rabbitmq.test.order.exchange";
    private static final String ORDER_CANCEL_EXCHANGE = "priv.rabbitmq.test.order.cancel.exchange";

    private static final String ORDER_QUEUE = "priv.rabbitmq.test.order.queue";
    static final String ORDER_CANCEL_QUEUE = "priv.rabbitmq.test.order.cancel.queue";

    /**
     * 定义订单交换器和队列(dlx + ttl)
     *
     * @throws IOException IO异常
     */
    @Test
    public void defineRelations() throws IOException {
        // 订单交换器
        channel.exchangeDeclare(ORDER_EXCHANGE, BuiltinExchangeType.FANOUT, true, false, Collections.emptyMap());
        // dlx + ttl
        Map<String, Object> properties = Maps.newHashMap();
        properties.put("x-dead-letter-exchange", ORDER_CANCEL_EXCHANGE);
        properties.put("x-message-ttl", EXPIRED_MILLIS);
        // 订单队列
        channel.queueDeclare(ORDER_QUEUE, true, false, false, properties);
        // 绑定
        channel.queueBind(ORDER_QUEUE, ORDER_EXCHANGE, "", Collections.emptyMap());

        // 取消订单交换器
        channel.exchangeDeclare(ORDER_CANCEL_EXCHANGE, BuiltinExchangeType.FANOUT, true, false, Collections.emptyMap());
        // 取消订单队列
        channel.queueDeclare(ORDER_CANCEL_QUEUE, true, false, false, Collections.emptyMap());
        // 绑定
        channel.queueBind(ORDER_CANCEL_QUEUE, ORDER_CANCEL_EXCHANGE, "", Collections.emptyMap());
    }
}
