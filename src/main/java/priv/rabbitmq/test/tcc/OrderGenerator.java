package priv.rabbitmq.test.tcc;

import com.google.common.collect.Maps;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author yuqiang lin
 * @description 生成随机订单
 * @email 1098387108@qq.com
 * @date 2019/9/29 4:06 PM
 */
class OrderGenerator {
    private static final Map<Integer, String> USERS = initUsers();

    /**
     * 定义一些用户
     *
     * @return 用户
     */
    private static Map<Integer, String> initUsers() {
        Map<Integer, String> users = Maps.newHashMap();
        int i = 1;
        users.put(i++, "马云");
        users.put(i++, "小李子");
        users.put(i++, "霍建华");
        users.put(i++, "苍老师");
        users.put(i++, "奥尼尔");
        users.put(i++, "范冰冰");
        users.put(i++, "蔡徐坤");
        users.put(i++, "贝尔格里尔斯");
        users.put(i++, "成龙");
        users.put(i, "王铁柱");
        return users;
    }

    /**
     * 生成随机订单
     * @param orderId   订单id
     * @return  随机订单
     */
    static Order generate(Integer orderId) {
        int userId = ThreadLocalRandom.current().nextInt(1, USERS.size() + 1);
        String userName = USERS.get(userId);

        return Order.builder()
                .orderId(orderId)
                .createdAt(LocalDateTime.now())
                .userId(userId)
                .userName(userName)
                .state(OrderState.INITIAL)
                .build();
    }
}
