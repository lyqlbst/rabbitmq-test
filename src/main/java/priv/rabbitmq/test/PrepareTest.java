package priv.rabbitmq.test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeoutException;

/**
 * @author yuqiang lin
 * @description 管理资源
 * @email linyuqiang@guazi.com
 * @date 2019/9/19 2:37 PM
 */
public class PrepareTest {
    private Connection connection;
    Channel channel;

    @Before
    public void createConnectionAndChannel() throws IOException, TimeoutException {
        // 单例获取连接工厂
        ConnectionFactory factory = AMQPConnectionFactory.INSTANCE.getInstance();
        connection = factory.newConnection();
        System.out.println("...amqp connection created success.");

        // 一个连接可以创建多个信道
        channel = connection.createChannel();
        System.out.println("...amqp channel created success.");

        System.out.println("--------------------------------\n");
    }

    @After
    public void closeChannelAndConnection() throws IOException, TimeoutException {
        System.out.println("\n--------------------------------");

        channel.close();
        System.out.println("...amqp channel closed success.");

        connection.close();
        System.out.println("...amqp connection closed success.");
    }

    /**
     * 声明队列、交换器、绑定键、路由模式等
     *
     * @throws IOException IO异常
     */
    @Test
    public void createRelations() throws IOException {
        for (CommonParams.RelationsEnum relation : CommonParams.RelationsEnum.values()) {
            // 声明队列，测试暂不需要持久化
            channel.queueDeclare(relation.getQueue(), false, false, false, Collections.emptyMap());

            // 声明不同类型的交换器，其他属性忽略
            channel.exchangeDeclare(relation.getExchange(), relation.getExchangeType());

            // 绑定队列或交换器，也可以绑定多个
            channel.queueBind(relation.getQueue(), relation.getExchange(), relation.getBindingKey(), relation.getProperties());
        }
        System.out.println("queues, exchanges, bindingTypes has been initialed...");
    }

    /**
     * 清空所有队列中的消息
     *
     * @throws IOException IO异常
     */
    @Test
    public void purgeQueues() throws IOException {
        for (CommonParams.RelationsEnum relation : CommonParams.RelationsEnum.values()) {
            String queue = relation.getQueue();
            try {
                // 若队列不存在，则无清空
                channel.queueDeclarePassive(queue);
            } catch (IOException ignored) {
                System.out.println("queue don't exists.");
                // 若报错，channel会自动关闭，需重新开启
                channel = connection.createChannel();
                continue;
            }
            channel.queuePurge(queue);
            System.out.println("queue \"" + queue + "\" has been purged.");
        }
    }
}
