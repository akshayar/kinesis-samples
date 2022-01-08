var FakerHelper = Java.type('com.aksh.kinesis.producer.faker.FakerHelper');
var Properties = Java.type('java.util.Properties');
var TimeUnit = Java.type('java.util.concurrent.TimeUnit') ;
var outValue = new Properties();
var faker= FakerHelper.faker;
var timestamp = faker.date().past(2, TimeUnit.HOURS).getTime() ;
var buyPrice = faker.number().randomDouble(2,10,100) ;
var sellPrice = faker.number().randomDouble(2,10,100) ;
var symbol = faker.options().option("AAPL","INFY","AMZN","GOOG","IBM") ;
var tradeId=faker.numerify('##########') ;
outValue.put('tradeId',tradeId);
outValue.put('orderId',"order"+tradeId);
outValue.put('portfolioId',"port"+tradeId);
outValue.put('customerId',"cust"+tradeId);
outValue.put('symbol',symbol);
outValue.put('timestamp', timestamp);
outValue.put( 'orderTimestamp', timestamp+'');
outValue.put('description',symbol+" Description of trade");
outValue.put('traderName',symbol+" Trader");
outValue.put('traderFirm',symbol+" Trader Firm");
outValue.put( 'buy',faker.bool().bool());
outValue.put( 'currentPosition',faker.number().digits(4));
outValue.put('quantity',faker.number().randomDouble(2,10,100));
outValue.put('price',faker.number().randomDouble(2,10,100));
outValue.put( 'buyPrice',buyPrice) ;
outValue.put( 'sellPrice',sellPrice);
outValue.put( 'profit',sellPrice-buyPrice);


