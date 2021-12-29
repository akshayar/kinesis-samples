package com.aksh.kinesis.producer;

import com.aksh.kinesis.producer.fake.JSRandomDataGenerator;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.Properties;

@Component
public class AvroByteArrayFromRandomObject implements ByteArrayGenerationStrategy{
    @Value("${avroSchemaPath:src/main/resources/avro/com/aksh/kafka/avro/fake/TradeData.avsc}")
    private String avroSchemaPath;

    @Value("${faker.fakeGeneratorJSScript:src/main/resources/generate-data.js}")
    private String fakeGeneratorJSScript;

    @Autowired
    JSRandomDataGenerator jsRandomDataGenerator;

    AvroSerDe avroSerDe=new AvroSerDe();

    public AvroByteArrayFromRandomObject() {
    }
    public AvroByteArrayFromRandomObject(JSRandomDataGenerator jsRandomDataGenerator) {
        this.jsRandomDataGenerator = jsRandomDataGenerator;
    }

    private static final String PROPERTY_SCHEMA_PATH="PROPERTY_SCHEMA_PATH";

    @Override
    public ByteBuffer generateData() throws Exception{
        return getByteBuffer(avroSchemaPath);
    }

    public ByteBuffer generateData(Properties properties) throws Exception{

        String schemaPath= Optional.ofNullable(properties.get(PROPERTY_SCHEMA_PATH)).map(Object::toString).orElse(avroSchemaPath);
        return getByteBuffer(schemaPath);

    }


    private ByteBuffer getByteBuffer(String schemaPath) throws Exception {
        Schema avroSchema=readSchema(schemaPath);
        GenericRecord avroRecord=new GenericData.Record(avroSchema);
        Properties generatedValues= jsRandomDataGenerator.createPayloadObject(null,fakeGeneratorJSScript);
        generatedValues.entrySet().stream().forEach(entry->{
            avroRecord.put(entry.getKey()+"",entry.getValue());
        });

        return avroSerDe.serialize(avroSchema,avroRecord);

    }

    private Schema readSchema(String schemaPath) throws IOException {
        String template=FileUtils.readFileToString(new File(schemaPath));
        return  new Schema.Parser().parse(template);
    }

    public void setAvroSchemaPath(String avroSchemaPath) {
        this.avroSchemaPath = avroSchemaPath;
    }

    public void setFakeGeneratorJSScript(String fakeGeneratorJSScript) {
        this.fakeGeneratorJSScript = fakeGeneratorJSScript;
    }
}
