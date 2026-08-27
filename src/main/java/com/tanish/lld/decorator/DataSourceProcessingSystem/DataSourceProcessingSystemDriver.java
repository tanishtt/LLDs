package com.tanish.lld.decorator.DataSourceProcessingSystem;

import java.util.HashMap;
import java.util.Map;

interface DataSource{
    String readData();
    void writeData(String data);
}

class FileDataSource implements DataSource{

    private static final Map<String, String> FAKE_DISK=new HashMap<>();
    private final String fileName;

    FileDataSource(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String readData() {
        String data=FAKE_DISK.getOrDefault(fileName,"");
        System.out.println("[FileDataSource] read "+data.length()+" raw bytes from "+fileName);
        return data;
    }

    @Override
    public void writeData(String data) {
        System.out.println("[FileDataSource] wrote "+data.length()+" raw bytes to "+fileName);
        FAKE_DISK.put(fileName, data);
    }
}

//decorator
abstract class DataSourceDecorator implements DataSource{
    protected final DataSource wrappee;

    DataSourceDecorator(DataSource wrappee) {
        this.wrappee = wrappee;
    }
    //a decorator that only wants to affect writeData() can override just that.
    //So default delegation in the abstract decorator is good, currently all require both read and write but if someone wants only few, then this will be good.
    @Override
    public String readData(){
        return wrappee.readData();
    }
    @Override
    public void writeData(String data){
        wrappee.writeData(data);
    }
}
class CompressionDataSource extends DataSourceDecorator{

    CompressionDataSource(DataSource wrappee) {
        super(wrappee);
    }

    @Override
    public String readData() {
        String raw = wrappee.readData();
        String decompressed = decompress(raw);
        System.out.println("[Compression] decompressed " + raw.length() + " -> " + decompressed.length() + " chars");
        return decompressed;
    }

    @Override
    public void writeData(String data) {
        String compressed = compress(data);
        System.out.println("[Compression] " + data.length() + " -> " + compressed.length() + " chars");
        wrappee.writeData(compressed);
    }
    private String compress(String data) {
        return "[C]" + data.replaceAll("(.)\\1+", "$1*");
    }

    private String decompress(String data) {
        if (data.startsWith("[C]")) data = data.substring(3);
        return data.replaceAll("(.)\\*", "$1$1$1"); // lossy toy example, illustrative only
    }
}
class EncryptionDataSource extends DataSourceDecorator{
    private static final int SHIFT = 3;
    EncryptionDataSource(DataSource wrappee) {
        super(wrappee);
    }

    @Override
    public String readData() {
        String decrypted=decode(wrappee.readData());
        System.out.println("[Encryption] decrypted payload after read");
        return decrypted;
    }

    @Override
    public void writeData(String data) {
        System.out.println("[Encryption] encrypted payload before write");
        String encrypted=encode(data);
        wrappee.writeData(encrypted);
    }
    private String decode(String data){
        StringBuilder sb=new StringBuilder();
        for (char ch : data.toCharArray()){
            sb.append((char)(ch - SHIFT));
        }
        return sb.toString();
    }
    private String encode(String data){
        StringBuilder sb=new StringBuilder();
        for (char ch : data.toCharArray()){
            sb.append((char)(ch + SHIFT));
        }
        return sb.toString();
    }
}
class LoggingDataSource extends DataSourceDecorator{

    LoggingDataSource(DataSource wrappee) {
        super(wrappee);
    }

    @Override
    public String readData() {
        System.out.println("[Logging] READ requested, timestamp=" + System.currentTimeMillis());
        String data = wrappee.readData();
        System.out.println("[Logging] READ completed, payload length=" + data.length());
        return data;
    }

    @Override
    public void writeData(String data) {
        System.out.println("[Logging] WRITE requested, payload length=" + data.length()
                + ", timestamp=" + System.currentTimeMillis());
        wrappee.writeData(data);
    }
}

class DataSourcePipelineBuilder{
    private DataSource dataSource;
    public DataSourcePipelineBuilder(String fileName){
        this.dataSource=new FileDataSource(fileName);
    }

    public DataSourcePipelineBuilder withCompression(){
        dataSource = new CompressionDataSource(dataSource);
        return this;
    }
    public DataSourcePipelineBuilder withEncryption(){
        dataSource = new EncryptionDataSource(dataSource);
        return this;
    }
    public DataSourcePipelineBuilder withLogging(){
        dataSource = new LoggingDataSource(dataSource);
        return this;
    }
    public DataSource build(){
        return dataSource;
    }
}
public class DataSourceProcessingSystemDriver {
    public static void main(String[] args) {
        System.out.println("=== Manual chaining (java.io style) ===");
        DataSource pipeline=new LoggingDataSource(
                new EncryptionDataSource(
                        new CompressionDataSource(
                                new FileDataSource("abc.txt")
                        )
                )
        );
        pipeline.writeData("aaaabbbbccccHello World aaaabbbb");
        System.out.println();
        String result = pipeline.readData();
        System.out.println("Final decoded data: " + result);


        DataSource pipeline2=new DataSourcePipelineBuilder("audit.log")
                .withEncryption()
                .withCompression()
                .withLogging()
                .build();
        pipeline2.writeData("ssssystem event xxxxlog entry");
        pipeline2.readData();
    }
}
