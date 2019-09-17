package priv.rabbitmq.test;

/**
 * @author yuqiang lin
 * @description 通用的一些参数，比如链接啥的
 * @email 1098387108@qq.com
 * @date 2019/9/17 2:56 PM
 */
final class CommonParams {

    // ----------------连接相关----------------

    static final String HOST = "10.19.26.104";
    static final Integer PORT = 5672;
    static final String USER = "admin";
    static final String PASSWORD = "admin";

    // ----------------队列----------------

    static final String COMMON_QUEUE = "priv.rabbitmq.test.common.queue";

    // ----------------交换器----------------

    static final String COMMON_EXCHANGE = "priv.rabbitmq.test.common.exchange";

    // ----------------绑定键----------------

    static final String COMMON_BINDING_KEY = "priv.rabbitmq.test.common.key";
}
