package priv.rabbitmq.test;

import com.rabbitmq.client.ConnectionFactory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static priv.rabbitmq.test.CommonParams.*;

/**
 * @author yuqiang lin
 * @description 用于创建连接
 * @email 1098387107@@qq.com
 * @date 2019/9/18 6:06 PM
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AMQPConnectionFactory {
    /**
     * 单例
     */
    INSTANCE(newInstance());

    private ConnectionFactory instance;

    /**
     * @return 一个新的连接
     */
    private static ConnectionFactory newInstance() {
        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost(HOST);
        factory.setPort(PORT);

        factory.setUsername(USER);
        factory.setPassword(P);

        return factory;
    }
}
