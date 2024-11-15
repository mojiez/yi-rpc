package com.atyichen.yirpc.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import com.atyichen.yirpc.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mojie
 * @date 2024/11/14 21:42
 * @description: SPI 加载器
 */
@Slf4j
public class SpiLoader {
    /**
     * 存储已加载的类  接口名 => (key  =>  实现类)
     * 线程安全
     */
    private static Map<String, Map<String, Class<?>>> loaderMap = new ConcurrentHashMap<>();

    /**
     * 对象实例缓存(避免重复 new)
     * 类路径 => 对象实例
     * 单例模式
     */
    private static Map<String, Object> instanceCache = new ConcurrentHashMap<>();

    /**
     * 系统SPI目录
     *
     */
    /*

    项目结构示例：
    my-application/                    # 你的应用项目
        └── src/
            └── main/
                └── resources/
                    └── META-INF/     # 应用项目的META-INF
                        └── rpc/
                            └── system/

    yirpc/                            # RPC框架项目（作为依赖）
        └── src/
            └── main/
                └── resources/
                    └── META-INF/     # 框架的META-INF
                        └── rpc/
                            └── system/
    // 1. 当你的应用引入RPC框架时：
    <dependency>
        <groupId>com.atyichen</groupId>
        <artifactId>yirpc</artifactId>
        <version>1.0.0</version>
    </dependency>

    // 2. 最终打包后的结构：
    your-app.jar
        └── META-INF/
            └── rpc/
                └── system/           # 合并后的目录
                    ├── file1.txt     # 来自你的应用
                    └── file2.txt     # 来自RPC框架
     */
    private static final String RPC_SYSTEM_SPI_DIR = "META-INF/rpc/system/";

    private static final String RPC_CUSTOM_SPI_DIR = "META-INF/rpc/custom/";

    /**
     * 扫描路径
     */
    private static final String[] SCAN_DIRS = new String[]{RPC_SYSTEM_SPI_DIR, RPC_CUSTOM_SPI_DIR};

    /**
     * 动态加载的类列表
     * 用来定义所有需要通过SPI机制动态加载的接口类列表
     * 例子：
     */
    /*
    private static final List<Class<?>> LOAD_CLASS_LIST = Arrays.asList(
        Serializer.class,      // 序列化接口
        LoadBalancer.class,    // 负载均衡接口
        Registry.class         // 注册中心接口
    );
     */
    private static final List<Class<?>> LOAD_CLASS_LIST = Arrays.asList(Serializer.class);

    /**
     * 加载所有需要动态加载的接口实现类
     */
    public static void loadAll() {
        log.info("加载所有SPI");
        for (Class<?> aClass : LOAD_CLASS_LIST) {
            load(aClass);
        }
    }

    /**
     * 加载某个接口的 所有SPI实现类
     */
    public static Map<String, Class<?>> load(Class<?> loadClass) {
        // 返回的是一个 Map
        log.info("记载类型为 {} 的SPI", loadClass.getName());
        // 扫描路径， 用户自定义的SPI优先级高于系统SPI
        // key => 实现类的类对象
        Map<String, Class<?>> keyClassMap = new HashMap<>();
        // todo 这里是用户的配置应该覆盖掉RPC框架的配置（如果同名）
        for (String scanDir : SCAN_DIRS) {
            // 不同的jar包 不同的项目中 可能有多个同名的配置文件：
            /*
            // 在不同的jar包或项目中可能有多个同名配置文件：

            // 框架jar包中的配置文件
            META-INF/rpc/system/com.atyichen.yirpc.serializer.Serializer:
            json=com.atyichen.yirpc.serializer.JsonSerializer
            jdk=com.atyichen.yirpc.serializer.JdkSerializer

            // 应用项目中的配置文件
            META-INF/rpc/system/com.atyichen.yirpc.serializer.Serializer:
            custom=com.myapp.CustomSerializer
             */
            // 这个列表就是代表 这是从不同的jar包 里读取到的同名的配置文件
            List<URL> resources = ResourceUtil.getResources(scanDir + loadClass.getName());
            // 读取每个资源文件
            for (URL resource : resources) {
                try {
                    // 文件读写操作
                    InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] strArray = line.split("=");
                        if (strArray.length > 1) {
                            String key = strArray[0];
                            String className = strArray[1];
                            // 根据完整的包路径就能得到 这个类 的 类对象 使用 Class.forName
                            keyClassMap.put(key, Class.forName(className));
                        }
                    }
                } catch (Exception e) {
                    log.error("spi resource load error", e);
                }
            }
        }
        // 接口名 => (key  =>  实现类类对象)
        loaderMap.put(loadClass.getName(), keyClassMap);
        return keyClassMap;
    }

    /**
     * 获取某个接口的实例
     * 传入接口的类对象  传入实现类的key
     * 返回这个接口的实现类的实例
     * 这样写好像确实是有问题的
     * // 编译前
     * public static <T> T getInstance(Class<?> tClass, String key)
     *
     * // 类型擦除后（实际运行时的代码）
     * public static Object getInstance(Class tClass, String key) {
     *     // ...
     *     return (Object) instanceCache.get(key);
     * }
     *
     * // 使用时编译器自动插入类型转换
     * Serializer serializer = (Serializer) SpiLoader.getInstance(Serializer.class, "json");
     *
     * Class<?> 和 <T> 是没有关联的
     *
     * @param tClass
     * @param key
     * @return
     * @param <T>
     */
    public static <T> T getInstance(Class<?> tClass, String key) {
        String tClassName = tClass.getName();
        Map<String, Class<?>> keyClassMap = loaderMap.get(tClassName);
        if (keyClassMap == null) {
            throw new RuntimeException(String.format("SpiLoader 未加载 %s 类型", tClassName));
        }
        if (!keyClassMap.containsKey(key)) {
            throw new RuntimeException(String.format("没有 key=%s 的实现类的类对象", key));
        }
        Class<?> aClass = keyClassMap.get(key);
        // 通过这个类对象创建 实现类的实例
        // 不需要创建 因为实例是单例的 只需要从实例缓存中获取就好了
        /*
          对象实例缓存(避免重复 new)
          类路径(类的完整包路径) => 对象实例
          单例模式
          private static Map<String, Object> instanceCache = new ConcurrentHashMap<>();
         */
        // 得到的是类的完整包路径 从类对象 获取类的完整包路径， 再通过完整包路径实例化类
        String implClassName = aClass.getName();

        // 这里是单例实现（懒加载）， 如果有这个实现类的实例 就返回 如果没有就创建
        if (!instanceCache.containsKey(implClassName)) {
            try {
                instanceCache.put(implClassName, aClass.newInstance());
            } catch (Exception e) {
                throw new RuntimeException(String.format("%s 类实例化失败", implClassName));
            }
        }
        return (T) instanceCache.get(implClassName);
    }
}
