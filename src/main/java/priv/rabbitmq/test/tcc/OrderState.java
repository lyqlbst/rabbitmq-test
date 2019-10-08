package priv.rabbitmq.test.tcc;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yuqiang lin
 * @description 订单状态
 * @email 1098387108@qq.com
 * @date 2019/10/8 5:26 PM
 */
@Getter
@AllArgsConstructor
public enum OrderState {
    /**
     * 未发送到mq
     */
    INITIAL(0),
    /**
     * 发送到mq失败，需要其他策略
     */
    INITIAL_FAILED(1),
    /**
     * 未支付
     */
    INITIALED(2),
    /**
     *支付成功
     */
    PAYED(3),
    /**
     * 支付失败，需要其他策略
     */
    PAY_FAILED(4);

    /**
     * 状态码
     */
    private int code;
}
