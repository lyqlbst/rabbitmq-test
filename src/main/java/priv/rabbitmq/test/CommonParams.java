package priv.rabbitmq.test;

import com.google.common.collect.Maps;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    static final String P = "admin";

    // ----------------队列----------------

    static final String DIRECT_QUEUE = "priv.rabbitmq.test.direct.queue";

    private static final String FANOUT_QUEUE = "priv.rabbitmq.test.fanout.queue";

    private static final String TOPIC_ONE_WORD_QUEUE = "priv.rabbitmq.test.topic.one.queue";
    private static final String TOPIC_ANY_WORDS_QUEUE = "priv.rabbitmq.test.topic.any.queue";

    private static final String HEADERS_ALL_QUEUE = "priv.rabbitmq.test.headers.all.queue";
    private static final String HEADERS_ANY_QUEUE = "priv.rabbitmq.test.headers.any.queue";

    private static final String BAK_QUEUE = "priv.rabbitmq.test.bak.queue";

    private static final String DEAD_QUEUE = "priv.rabbitmq.test.dead.queue";

    // ----------------交换器----------------

    static final String DIRECT_EXCHANGE = "priv.rabbitmq.test.direct.exchange";

    static final String FANOUT_EXCHANGE = "priv.rabbitmq.test.fanout.exchange";

    static final String TOPIC_EXCHANGE = "priv.rabbitmq.test.topic.exchange";

    static final String HEADERS_EXCHANGE = "priv.rabbitmq.test.headers.exchange";

    private static final String BAK_EXCHANGE = "priv.rabbitmq.test.bak.exchange";

    private static final String DEAD_EXCHANGE = "priv.rabbitmq.test.dead.exchange";

    // ----------------绑定键----------------

    static final String DIRECT_BINDING_KEY = "priv.rabbitmq.test.direct.key";

    static final String FANOUT_BINDING_KEY = "priv.rabbitmq.test.fanout.anyKey";

    static final String TOPIC_ANY_WORDS_BINDING_KEY = "priv.rabbitmq.test.topic.#.key";
    static final String TOPIC_ONE_WORD_BINDING_KEY = "priv.rabbitmq.test.topic.*.key";

    static final String WRONG_BINDING_KEY = "priv.rabbitmq.test.wrong.key";

    // ----------------其他----------------

    static final AMQP.BasicProperties EMPTY_PROPERTIES = new AMQP.BasicProperties.Builder().build();

    /**
     * @return 随机一个routingKey，无意义
     */
    static String getMeaninglessRoutingKey() {
        return UUID.randomUUID().toString();
    }

    /**
     * @return basic headers
     */
    static Map<String, Object> getCommonProperties() {
        HashMap<String, Object> properties = Maps.newHashMap();
        properties.put("name", "linyuqiang");
        properties.put("age", "18");
        return properties;
    }

    /**
     * @return 全匹配headers规则
     */
    private static Map<String, Object> getAllHeadersProperties() {
        Map<String, Object> properties = getCommonProperties();
        properties.put("x-match", "all");
        return properties;
    }

    /**
     * @return 任意匹配headers规则
     */
    private static Map<String, Object> getAnyHeadersProperties() {
        Map<String, Object> properties = getCommonProperties();
        properties.put("x-match", "any");
        return properties;
    }

    /**
     * 构建一个备份队列参数
     *
     * @return 备份队列所需参数
     */
    private static Map<String, Object> getBakExchangeProperties() {
        Map<String, Object> properties = Maps.newHashMap();
        properties.put("alternate-exchange", BAK_EXCHANGE);
        return properties;
    }

    /**
     * 构建一个死信队列参数
     *
     * @return 备份队列所需参数
     */
    private static Map<String, Object> getDeadLetterExchangeProperties() {
        Map<String, Object> properties = Maps.newHashMap();
        properties.put("x-dead-letter-exchange", DEAD_EXCHANGE);
        // 设置过期时间，方便测试
        properties.put("x-message-ttl", 5000);
        // 设置队列最大长度，方便测试
        properties.put("x-max-length", 3);
        return properties;
    }

    /**
     * 维护一个映射关系，四种模式下的队列、交换器、绑定键、匹配模式
     */
    @Getter
    @AllArgsConstructor
    enum RelationsEnum {
        DIRECT(DIRECT_QUEUE
                , DIRECT_EXCHANGE
                , DIRECT_BINDING_KEY
                , BuiltinExchangeType.DIRECT
                , Collections.emptyMap()
                , getBakExchangeProperties()
                , Collections.emptyMap()),

        FANOUT(FANOUT_QUEUE
                , FANOUT_EXCHANGE
                , FANOUT_BINDING_KEY
                , BuiltinExchangeType.FANOUT
                , getDeadLetterExchangeProperties()
                , Collections.emptyMap()
                , Collections.emptyMap()),

        TOPIC_ONE_WORD(TOPIC_ONE_WORD_QUEUE
                , TOPIC_EXCHANGE
                , TOPIC_ONE_WORD_BINDING_KEY
                , BuiltinExchangeType.TOPIC
                , Collections.emptyMap()
                , Collections.emptyMap()
                , Collections.emptyMap()),

        TOPIC_ANY_WORDS(TOPIC_ANY_WORDS_QUEUE
                , TOPIC_EXCHANGE
                , TOPIC_ANY_WORDS_BINDING_KEY
                , BuiltinExchangeType.TOPIC
                , Collections.emptyMap()
                , Collections.emptyMap()
                , Collections.emptyMap()),

        ALL_HEADERS(HEADERS_ALL_QUEUE
                , HEADERS_EXCHANGE
                , getMeaninglessRoutingKey()
                , BuiltinExchangeType.HEADERS
                , Collections.emptyMap()
                , Collections.emptyMap()
                , getAllHeadersProperties()),

        ANY_HEADERS(HEADERS_ANY_QUEUE
                , HEADERS_EXCHANGE
                , getMeaninglessRoutingKey()
                , BuiltinExchangeType.HEADERS
                , Collections.emptyMap()
                , Collections.emptyMap()
                , getAnyHeadersProperties()),

        BAK(BAK_QUEUE
                , BAK_EXCHANGE
                , getMeaninglessRoutingKey()
                , BuiltinExchangeType.FANOUT
                , Collections.emptyMap()
                , Collections.emptyMap()
                , Collections.emptyMap()),

        DEAD(DEAD_QUEUE
                , DEAD_EXCHANGE
                , getMeaninglessRoutingKey()
                , BuiltinExchangeType.FANOUT
                , Collections.emptyMap()
                , Collections.emptyMap()
                , Collections.emptyMap());

        /**
         * 队列名
         */
        private String queue;
        /**
         * 交换器名
         */
        private String exchange;
        /**
         * 绑定键名
         */
        private String bindingKey;
        /**
         * 路由规则
         */
        private BuiltinExchangeType exchangeType;
        /**
         * 队列参数
         */
        private Map<String, Object> queueProperties;
        /**
         * 交换机参数
         */
        private Map<String, Object> exchangeProperties;
        /**
         * 绑定参数
         */
        private Map<String, Object> bindingProperties;
    }
}
