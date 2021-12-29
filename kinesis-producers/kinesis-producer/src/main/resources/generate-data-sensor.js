var FakerHelper = Java.type('com.aksh.kinesis.producer.faker.FakerHelper');
var Properties = Java.type('java.util.Properties');
var TimeUnit = Java.type('java.util.concurrent.TimeUnit') ;
var outValue = new Properties();
var faker= FakerHelper.faker;

var temperature = FakerHelper.faker.number().randomDouble(2,100,200) ;

status = "OK";
if (temperature>160){
    status = "ERROR";
}else if (temperature>140){
    status=FakerHelper.faker.options().option("WARNING","ERROR");
}else{
    status = "OK";
}
var d = new Date();
var timestamp = d.toISOString();

outValue.put('sensor_id',FakerHelper.faker.number().numberBetween(1,100));
outValue.put('current_temperature',temperature);
outValue.put('status',status);
outValue.put('event_time',timestamp);
