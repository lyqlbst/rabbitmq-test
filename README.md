# rabbitmq-test
测试RabbitMQ如何使用

**ProducerTest：测试了四种路由方式：**
+ **`direct`: routingKey必须`完全匹配`**

+ **2.`fanout`: 与routingKey`无关`**

+ **3.`topic`: 匹配部分，"`*`"匹配`一个单词`，"`#`"匹配`多个单词`"，"`.`"分割"**

+ **4.`headers`: "`all`"匹配`所有键值对`，"`any`"匹配`任意键值对`**
