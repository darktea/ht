# 一, 说明

本项目目的：测试各种 http client 性能。目前已有的测试：

* apache http client (AHC) 3.0.1
* apache http client (AHC) 4.5.3
* apache http async client (HttpAsyncClient) 4.1.3

# 二, 运行

要跑各种 http client 的测试，需要先有一个 http server。所以，运行步骤分两步：

* 启动 http server
* 测试各种 http client

## 1. 启动 http server

### how to build

```
mvn clean package
```

### how to run

启动一个 http 服务：

```
java -jar ht-1.0-SNAPSHOT.jar --port=8072
```

* port 端口号

访问示例（获取一个长度为 1024 的随机 buffer）：

```
curl 'http://127.0.0.1:8072/rnd?c=1024'
```

## 2. 启动 http client

（待续）