## Setup
1. Create KDS . Update the name of stream (streamName) and region(region) in application.properties. 

## JSON Data Generation
1. Modify application.properties with `DataGenerationStrategy.type=JSON`
2. To generate data create a JS script similar to  `src/main/resources/generate-data.js` . Create yor logic to create data as properties object. 
3. Give the path to JS script file in application.properties as  
`faker.fakeGeneratorJSScript=src/main/resources/generate-data-sensor.js`
  


## AVRO Data Generation
1. Modify application.properties with `DataGenerationStrategy.type=AVRO`
2. To generate data create a JS script similar to  `src/main/resources/generate-data.js` . Create yor logic to create data as properties object.
3. Give the path to JS script file in application.properties as  
   `faker.fakeGeneratorJSScript=src/main/resources/generate-data-sensor.js`

## Running Application from IDE
1. Import the project as Maven project in Eclipse/Intellij. 
2. Run `src/main/java/com/aksh/kinesis/producer/KinesisProducerMain.java` as main Java class. 

## Running Application from Command line
1. Build using `mvn clean install -DskipTests` from the project root. 

## Running as Docker application