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