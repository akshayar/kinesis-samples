package com.aksh.avro;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

public class AvroSerDe {


    public byte[] serialize(InputStream schemaIn,Object data) throws IOException{
        Schema schema=new Schema.Parser().parse(schemaIn);
        return serialize(schema,data);
    }


    public byte[] serialize(Schema schema,Object data) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.reset();
        BinaryEncoder binaryEncoder = new EncoderFactory().binaryEncoder(byteArrayOutputStream, null);
        new SpecificDatumWriter<>(schema).write(data,binaryEncoder);
        binaryEncoder.flush();
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return bytes;
    }

    public byte[] serializeFromProperties(InputStream schemaIn, Properties dataIn) throws IOException{
        Schema schema=new Schema.Parser().parse(schemaIn);

        return Optional.ofNullable(dataIn).filter(Objects::nonNull).map(data -> {
            GenericRecord avroRecord = new GenericData.Record(schema);
            data.entrySet().stream().forEach(entry -> {
                avroRecord.put(entry.getKey() + "", entry.getValue());
            });
            byte[] buffer=null;
            try {
                buffer=serialize(schema,avroRecord);
            } catch (IOException e) {
               e.printStackTrace();
            }
            return buffer;
        }).orElse(null);
    }
    public Properties deserializeToProperties(InputStream schemaIn,byte[] dataIn) throws IOException {
        Properties prop=new Properties();
        GenericRecord data=deserializeGeneric(schemaIn,dataIn);
        data.getSchema().getFields().stream().forEach(field -> {
            prop.put(field.name(),data.get(field.name()));
        });
        return prop;
    }
    public Object deserializeSpecific(InputStream schemaIn,byte[] data) throws IOException {
        Schema schema=new Schema.Parser().parse(schemaIn);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        BinaryDecoder binaryDecoder = new DecoderFactory().binaryDecoder(byteArrayInputStream,null);
        return new GenericDatumReader<>(schema).read(null,binaryDecoder);
    }

    public GenericRecord deserializeGeneric(InputStream schemaIn,byte[] data) throws IOException {
        Schema schema=new Schema.Parser().parse(schemaIn);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        BinaryDecoder binaryDecoder = new DecoderFactory().binaryDecoder(byteArrayInputStream,null);
        return new GenericDatumReader<GenericRecord>(schema).read(null,binaryDecoder);
    }


    private Schema readSchema(String schemaPath) throws IOException {
        return new Schema.Parser().parse(new File(schemaPath));
    }
}
