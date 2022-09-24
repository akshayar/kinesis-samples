var FakerHelper = Java.type('com.aksh.kinesis.producer.faker.FakerHelper');
var Properties = Java.type('java.util.Properties');
var TimeUnit = Java.type('java.util.concurrent.TimeUnit') ;
var outValue = new Properties();
var faker= FakerHelper.faker;
var dateTimeRandom=faker.date().past(2, TimeUnit.HOURS);
var epochMilliseconds = dateTimeRandom.getTime();
var epochSeconds= FakerHelper.getEpochInSeconds(dateTimeRandom);
var buyPrice = faker.number().randomDouble(2,10,100) ;
var sellPrice = faker.number().randomDouble(2,10,100) ;
var symbol = faker.options().option("AAPL","INFY","AMZN","GOOG","IBM") ;
var tradeId=faker.numerify('##########') ;
outValue.put('tradeId',tradeId);
outValue.put('orderId',"order"+tradeId);
outValue.put('customerId',"cust"+tradeId);
outValue.put('portfolioId',"port"+tradeId);
outValue.put( 'buy',faker.bool().bool());
outValue.put('price',faker.number().randomDouble(2,10,100));
outValue.put('quantity',faker.number().randomDouble(2,10,100));
outValue.put('symbol',symbol);
outValue.put('description',symbol+" Description of trade");
outValue.put('traderName',symbol+" Trader");
outValue.put('traderFirm',symbol+" Trader Firm");
outValue.put( 'orderTimestamp', epochSeconds+'');
outValue.put('timestamp', epochSeconds);

//outValue.put( 'currentPosition',faker.number().digits(4));
//outValue.put( 'buyPrice',buyPrice) ;
//outValue.put( 'sellPrice',sellPrice);
//outValue.put( 'profit',sellPrice-buyPrice);


