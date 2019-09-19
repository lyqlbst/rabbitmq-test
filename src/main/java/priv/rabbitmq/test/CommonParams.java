package priv.rabbitmq.test;

import com.google.common.collect.Maps;
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

    // ----------------交换器----------------

    static final String DIRECT_EXCHANGE = "priv.rabbitmq.test.direct.exchange";

    static final String FANOUT_EXCHANGE = "priv.rabbitmq.test.fanout.exchange";

    static final String TOPIC_EXCHANGE = "priv.rabbitmq.test.topic.exchange";

    static final String HEADERS_EXCHANGE = "priv.rabbitmq.test.headers.exchange";

    // ----------------绑定键----------------

    static final String DIRECT_BINDING_KEY = "priv.rabbitmq.test.direct.key";

    static final String FANOUT_BINDING_KEY = "priv.rabbitmq.test.fanout.anyKey";

    static final String TOPIC_ANY_WORDS_BINDING_KEY = "priv.rabbitmq.test.topic.#.key";
    static final String TOPIC_ONE_WORD_BINDING_KEY = "priv.rabbitmq.test.topic.*.key";

    /**
     *
     * @return  headers模式下的routingKey，无意义
     */
    static String getHeadersRoutingKey(){
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
     * 维护一个映射关系，四种模式下的队列、交换器、绑定键、匹配模式
     */
    @Getter
    @AllArgsConstructor
    enum RelationsEnum {
        DIRECT(DIRECT_QUEUE
                , DIRECT_EXCHANGE
                , DIRECT_BINDING_KEY
                , BuiltinExchangeType.DIRECT
                , Collections.emptyMap()),

        FANOUT(FANOUT_QUEUE
                , FANOUT_EXCHANGE
                , FANOUT_BINDING_KEY
                , BuiltinExchangeType.FANOUT
                , Collections.emptyMap()),

        TOPIC_ONE_WORD(TOPIC_ONE_WORD_QUEUE
                , TOPIC_EXCHANGE
                , TOPIC_ONE_WORD_BINDING_KEY
                , BuiltinExchangeType.TOPIC
                , Collections.emptyMap()),

        TOPIC_ANY_WORDS(TOPIC_ANY_WORDS_QUEUE
                , TOPIC_EXCHANGE
                , TOPIC_ANY_WORDS_BINDING_KEY
                , BuiltinExchangeType.TOPIC
                , Collections.emptyMap()),

        ALL_HEADERS(HEADERS_ALL_QUEUE
                , HEADERS_EXCHANGE
                , getHeadersRoutingKey()
                , BuiltinExchangeType.HEADERS
                , getAllHeadersProperties()),

        ANY_HEADERS(HEADERS_ANY_QUEUE
                , HEADERS_EXCHANGE
                , getHeadersRoutingKey()
                , BuiltinExchangeType.HEADERS
                , getAnyHeadersProperties());

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
         * headers，若有的话
         */
        private Map<String, Object> properties;
    }
}
