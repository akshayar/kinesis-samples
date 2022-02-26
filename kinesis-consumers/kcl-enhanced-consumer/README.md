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

