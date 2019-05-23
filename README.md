# fastjgame
fastjgame 为 fast java game framework的缩写，如名字一样，该项目的目标是一个高性能，高稳定性的游戏架构。
它将是一个分布式多进程架构，游戏逻辑线程为单线程。 游戏逻辑运行在Disruptor消费者线程中，其它线程都为辅助线程，整体为多生产者单消费者模式。  

高性能从网络层开始：  
IO框架为Netty,HttpClient为OkHttp3;   
支持断线重连，支持websocket和tcp同时接入。  
支持google protoBuf，json以及自定义消息序列化方式。   
支持自定义消息映射;  
目前来说除了同步rpc调用那块为了支持重连，设计的不太完美以外，整体来说暂时应该不会改动。较之我工作中的项目而言，消除了许多不必要的同步。  

游戏的玩法架构已确定：  
游戏世界划分为多个区域，每个进程（频道）承载一个或多个区域，支持跨运营平台，跨服玩法。  
在game-core包的doc文件夹下可以看见服务器架构、场景区域划分等图。  
待完善的是负载均衡和宕机恢复。  

游戏服务器的节点发现以完成：
基于zookeeper实现，同时zookeeper作为配置中心，以及分布式锁.  

数据库引入：MongoDB  
MongoDB是NOSQL数据库，个人感觉其对程序员非常友好，而且数据扩展容易，性能也好。

### 说下自己为什么开源
1. 从校园步入社会已经三年了，这三年里，参与了两个项目。接触了太多的新东西，netty disruptor zookeeper... 
   我现在的状态是：学了很多，理论知识还可以，但是缺乏实践，需要写一个完整的项目练手。
2. 个人稍微看过几个他人的游戏项目，个人觉得并不如我们项目的架构，此外代码质量太差，编程模型不够简单。
3. 我对我们项目的某些设计也有不太认可的地方，有一些自己的想法，想写一个自己期望的框架。
4. 我个人非常看重游戏的创意，而这些创意很多时候在中小型游戏公司产生，而往往缺乏技术支撑的却是他们。
5. 最后，感谢之前的leader **李诗乐** 允许我将项目中学到的东西开源。


(Markdown语法不是很熟悉，排版什么的后期有空再优化~)
