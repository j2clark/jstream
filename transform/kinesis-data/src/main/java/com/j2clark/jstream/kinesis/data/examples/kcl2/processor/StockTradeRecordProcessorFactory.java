package com.j2clark.jstream.kinesis.data.examples.kcl2.processor;

import software.amazon.kinesis.processor.ShardRecordProcessor;
import software.amazon.kinesis.processor.ShardRecordProcessorFactory;

public class StockTradeRecordProcessorFactory implements ShardRecordProcessorFactory {
    @Override
    public ShardRecordProcessor shardRecordProcessor() {
        return new StockTradeRecordProcessor();
    }
}