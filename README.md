# rabbitmq-test
测试RabbitMQ如何使用

**ProducerTest：**

**测试了四种路由方式：**
+ 1.`direct`: routingKey必须`完全匹配`

+ 2.`fanout`: 与routingKey`无关`

+ 3.`topic`: 匹配部分，"`*`"匹配`一个单词`，"`#`"匹配`多个单词`"，"`.`"分割"

+ 4.`headers`: "`all`"匹配`所有键值对`，"`any`"匹配`任意键值对`

**测试了两种发消息的模式：**
+ `mandatory`: 若设置为`true`，则当消息无法被正确路由，返回错误信息

+ `immediate`: 若设置为`true`，则当当前路由的队列上不存在任何消费者时，将发送失败

**测试了特殊功能：**
+ `alternate-exchange`(AE): 若指定了备份交换器，则当消息无法被正确路由时，将会将消息发送到备份交换器上，
一般来说，备份交换器的路由模式最好指定为`fanout`，这样能尽量保证路由失败的消息会被正确处理，
**注意，当`mandatory`也设置为 `true`的时候，以备份队列为准，mandatory会失效**

+ `x-dead-letter-exchange`(DLX): 若指定了死信队列，则当前消息出现以下几种情况时，会被路由到死信队列上，
1.消息过期；2.队列长度限制；3.消息被拒绝

**ConsumerTest：**

**测试了两种消费方式：**
+ `basicGet`: **拉模式**，每次只拉取队列中的`第一条消息`，吞吐量很低

+ `basicConsume`: **推模式**，每次服务器会推送多条消息，吞吐量较好

+ `Qos`: 限制每次推送过来消息的最大消息数

+ `autoAck`: 若设置为`true`，则会自动确认，而设置为`false`，则需要手动确认

+ `basicAck`中的`multiple`: 若设置为`true`，则会自动确认`该channel`的，`已消费过`的，比该`deliveryTag`小的所有消息
