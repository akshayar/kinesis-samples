## Kinesis Producer Library Sample
The objective of this project to showcase writing a HTTP POST web service which received data as POST paylaod and sends that to Kinesis stream using Kinesis Producer Library. 
This is a sample code and not a production grade code. 

### Build
mvn clean install -DskipTests

### Run From IDE

### Run From Maven
mvn spring-boot:run

### Web Services
1. Heatlh Check
```html
 GET  http://localhost:8080/health-check
```

2. Push Data
```html
   POST http://localhost:8080/publish-kpl
   POST data:
   String data
```

```json
POST http://localhost:8080/publish-kpl
POST data:
{"profit":-16.67, "orderTimestamp":1645877911, "traderName":"cbbb Trader", "orderId":"order7031420007", "currentPosition":1, "buyPrice":79.58, "symbol":"cbbb", "quantity":70.73, "buy":true, "price":49.56, "tradeId":7031420007, "timestamp":1645877911, "portfolioId":"port7031420007", "sellPrice":62.91, "description":"cbbb Description of trade", "customerId":"cust7031420007", "traderFirm":"cbbb Trader Firm"}

```

### Load Testing
Refer jMeter Plan at [Jmeter Plan](src/test/jmeter/test-kpl-producer.jmx)

### How to test on desktop
1. Create Kineis Steam and configure [application.properties](src/main/resources/application.properties) , with region , stream name and other parameters. 
2. Ensure that AWS profile is configured on desktop and user has required permission to push to Kineis stream. 
3. Run the program using ```mvn spring-boot:run ```
4. Run `GET http://localhost:8080/health-check` to confirm that the endpoints are working. 
5. Run jMeter test to send data to the `http://localhost:8080/publish-kpl`.
6. Check Kinesis Data Stream metrics to confirm that the data is being pushed. 
7. To validate that the data can be consumed from Lambda, deploy on run [kinesis-consumer-lambda](../../kinesis-consumers/kinesis-consumer-lambda).
