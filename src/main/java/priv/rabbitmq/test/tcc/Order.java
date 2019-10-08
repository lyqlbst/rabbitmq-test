package priv.rabbitmq.test.tcc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author yuqiang lin
 * @description 订单的信息
 * @email 1098387108@qq.com
 * @date 2019/9/29 4:00 PM
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Order {
    /**
     * 订单id
     */
    private Integer orderId;
    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    /**
     * 用户id
     */
    private Integer userId;
    /**
     * 用户姓名
     */
    private String userName;
    /**
     * 订单状态：
     * 0.未发送到mq
     * 1.发送到mq失败
     * 2.未支付
     * 3.支付成功
     * 4.支付失败
     */
    private OrderState state;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
