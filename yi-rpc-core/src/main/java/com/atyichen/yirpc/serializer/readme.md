# 设计思路
首先实现多种序列化器 JSON Kryo Hessian
然后定义 序列化器名称的常量
定义序列器工厂 来创建和获取序列化器对象

## 序列化器工厂
序列化器对象是可以复用的， 没必要执行序列化操作前都创建一个新的对象
使用 **工厂模式 + 单例模式**来简化创建和获取序列化器对象的操作

动态获取序列化器
需要将之前代码中所有用到序列化器的位置更改为 "使用工厂 + 读取配置" 来获取实现类

## 支持上传用户自定义序列化器
现在实现了用户通过配置文件指定框架已经实现的序列化器
现在要实现 支持用户上传自己实现的序列化器（框架实现动态加载）

实现的思路本质上还是定好 序列化器的名称 和 序列化器的实现类的完整包路径
然后RPC读取名称和路径自动装配

### 定义SPI配置目录
系统内置实现类 的配置目录 + 用户自定义实现类的配置目录
```js
jdk=com.atyichen.yirpc.serializer.JdkSerializer
hessian=com.atyichen.yirpc.serializer.HessianSerializer
json=com.atyichen.yirpc.serializer.JsonSerializer
kryo=com.atyichen.yirpc.serializer.KryoSerializer
```
### 编写SpiLoader加载器
相当于一个工具类， 提供了读取配置并加载实现类的方法

// 1. 同名目录下的文件合并
resources/
├── config/              
├── app.properties    // 来自应用
└── core.properties   // 来自依赖

// 2. 同名文件的处理
resources/
└── config/
└── settings.properties  // 通常应用的文件会覆盖依赖的文件


src/main/resources下的所有文件都会被打包到classpath根目录
包括依赖项目中的资源
资源合并规则
同名目录下的不同文件会合并
同名文件通常后加载的会覆盖先加载的
加载机制
ClassLoader可以加载classpath下任何位置的资源
可以通过不同的方式获取资源（getResource, getResourceAsStream等）
特殊处理
META-INF有特殊用途（服务发现、配置等）
某些框架可能有特定的资源加载规则
注意事项
资源路径区分大小写
注意资源文件的编码
考虑资源文件的覆盖问题