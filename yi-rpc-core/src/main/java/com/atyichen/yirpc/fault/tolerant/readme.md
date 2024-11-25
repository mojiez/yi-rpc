# 容错机制
Fail-Fast 快速失败、Fail-Safe 静默处理。

故障转移：

一次调用失败后， 切换一个其他节点再次进行调用

快速失败：

系统出现调用错误时， 立即报错， 交给外层调用方处理

静默处理：

系统出现部分非重要功能的异常时， 直接忽略， 不做任何处理

![img.png](img.png)