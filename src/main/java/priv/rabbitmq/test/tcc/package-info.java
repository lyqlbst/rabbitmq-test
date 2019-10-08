/**
 * @description 该包模拟了一个简单的分布式事务场景：下单-->支付，模拟tcc实现
 * <p>
 * {@link priv.rabbitmq.test.tcc.OrderTccQueueDefinitionTest} 该类定义了实现该场景所需的交换器和队列<br/>
 * <ul>
 * <li>包括一个支付队列：{@code pay.queue} 该队列是持久化的，并且是一个死信队列{@code DLX}</>
 * <li>包括一个支付成功队列：{@code pay.success.queue} 该队列是持久化的</>
 * <li>包括一个支付失败队列：{@code pay.failed.queue} 该队列是持久化的</>
 * </ul>
 * {@link priv.rabbitmq.test.tcc.OrderGenerateProducerTest} 该类是订单的生产者，将订单id发送到支付队列 {@code pay.queue}，
 * 同时订阅支付成功队列 {@code pay.success.queue} 和支付失败队列 {@code pay.failed.queue}，改变订单相应的状态。
 * <p>
 * {@link priv.rabbitmq.test.tcc.PayConsumerTest} 该类是支付队列的消费者，
 * 消费支付队列中 {code pay.success.queue} 的订单id，模拟一定的支付失败的情况，
 * 若支付成功，则将消息发送到支付成功队列：{@code pay.success.queue}，
 * 若支付失败，则将消息发送到支付失败队列：{@code pay.failed.queue}
 * <p>
 * {@link priv.rabbitmq.test.tcc.OrderGenerator} 生成测试订单
 * {@link priv.rabbitmq.test.tcc.Order} 订单信息数据实体(DO)
 * {@link priv.rabbitmq.test.tcc.OrderState} 支付状态
 * {@link priv.rabbitmq.test.tcc.Counter} 一个计数器，记录支付成功|失败的订单数量
 * <p>
 * 总的流程就是这样的：
 * <pre>
 *                   发送需要支付的订单id        支付队列(DLX)              执行支付逻辑
 *                       publish            ·--------------·             consume
 *              ·----------------------->   |  |  |  |  |  |    ------------------------·
 *              |                           ·--------------·                            |
 *              |                                                                       |
 *              |                                                                       V
 *           订单服务       变更订单状态为PAYED   支付成功队列           支付成功           支付服务
 *      ·---------------·      consume      ·--------------·        publish     ·---------------·
 *      |    service    |  <-------------   |  |  |  |  |  |    <-------------  |    service    |
 *      ·---------------·                   ·--------------·                    ·---------------·
 *              ^                                                                       |
 *              |                                                                       |
 *              |  变更订单状态为PAY_FAILED       支付失败队列           支付失败             |
 *              |         consume           ·--------------·        publish             |
 *              ·------------------------   |  |  |  |  |  |    <-----------------------·
 *                                          ·--------------·
 * </pre>
 * @author yuqiang lin
 * @email 1098387108@qq.com
 * @date 2019/10/8 6:20 PM
 */
package priv.rabbitmq.test.tcc;