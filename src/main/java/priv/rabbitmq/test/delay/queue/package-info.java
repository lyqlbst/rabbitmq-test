/**
 * @description 测试延迟队列的实现：DLX + TTL，其中：
 * {@link priv.rabbitmq.test.delay.queue.OrderDelayQueueDefinitionTest} 用于声明交换器及队列（DLX + TTL策略）
 * {@link priv.rabbitmq.test.delay.queue.OrderGenerateProducerTest} 用于产生订单信息
 * {@link priv.rabbitmq.test.delay.queue.OrderGenerateProducerTest} 用于处理超时订单
 * @author yuqiang lin
 * @email 1098387108@qq.com
 * @date 2019/9/24 6:27 PM
 */
package priv.rabbitmq.test.delay.queue;