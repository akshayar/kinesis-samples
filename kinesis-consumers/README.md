# Kinesis Consumers

## KCL Consumer
1. Consumers created using KCL libraries. 
### Getting Started

```
//Build 
mvn clean install -DskipTests
// Deploy and start consumer
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=<port>"
//Shutdown consumer
curl -X post http://localhost:<port>/actuator/shutdown

```


### FAQ

1. Will KCL subscribe to all available shards and distribute processing ? **Yes**
2. Can I have more subscribers than number of shards? **Yes, however that can be used for HA requirements.**
3. How can I setup auto-scaling to spawn more processors automatically? **Monitor CPU or Iteration Age, create alarm on those. If alarm triggers, add another subscriber.**
4. What consumer metrics are available ? Are they available by default or they have to be enabled ? **Metrics are enabled by default. One can add additional dimension.**
5. How to find number of re-tries ? ** ReadProvisionedThroughputExceeded and WriteProvisionedThroughputExceeded **
6. How to find that consumers is not able to cope up with system load and more consumers are required ?
7. 

## KCL Enhanced Fanout Consumer
1. Consumers created using KCL libraries. 

### Getting Started

```
//Build 
mvn clean install -DskipTests
// Deploy and start consumer
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=<port>"
//Shutdown consumer
curl -X post http://localhost:<port>/actuator/shutdown

```

## Lambda Consumer
