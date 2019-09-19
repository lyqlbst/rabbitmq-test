# rabbitmq-test
测试RabbitMQ如何使用

**ProducerTest：测试了四种路由方式：**
+ `direct`: routingKey必须`完全匹配`

+ 2.`fanout`: 与routingKey`无关`

+ 3.`topic`: 匹配部分，"`*`"匹配`一个单词`，"`#`"匹配`多个单词`"，"`.`"分割"

+ 4.`headers`: "`all`"匹配`所有键值对`，"`any`"匹配`任意键值对`

**ConsumerTest：测试了两种消费方式：**
+ `basicGet`: **拉模式**，每次只拉取队列中的`第一条消息`，吞吐量很低

+ `basicConsume`: **推模式**，每次服务器会推送多条消息，吞吐量较好

+ `Qos`: 限制每次推送过来消息的最大消息数

+ `autoAck`: 若设置为`true`，则会自动确认，而设置为`false`，则需要手动确认

+ `basicAck`中的`multiple`: 若设置为`true`，则会自动确认`该channel`的，`已消费过`的，比该`deliveryTag`小的所有消息
