package priv.rabbitmq.test.tcc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author yuqiang lin
 * @description 用于计数
 * @email 1098387108@qq.com
 * @date 2019/10/8 6:14 PM
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
final class Counter {
    /**
     * 付款成功的订单
     */
    private int payedCount;
    /**
     * 付款失败的订单
     */
    private int payFailedCount;

    /**
     * +1
     */
    void incPayedCount() {
        payedCount++;
    }

    /**
     * +1
     */
    void incPayFailedCount() {
        payFailedCount++;
    }

    @Override
    public String toString() {
        return "付款成功的订单有[ " + payedCount + " ]个，付款失败的有[ " + payFailedCount + " ]个。";
    }
}
