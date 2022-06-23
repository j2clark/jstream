package com.j2clark.jstream.flink.examples.utils;

import java.io.Serializable;

import com.j2clark.jstream.flink.examples.TripEvent;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketAssigner;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.SimpleVersionedStringSerializer;

public class TripEventBucketAssigner implements BucketAssigner<TripEvent, String>, Serializable {
    private final String prefix;

    public TripEventBucketAssigner(String prefix) {
        this.prefix = prefix;
    }

    public String getBucketId(TripEvent event, Context context) {
        return String.format("%spickup_location=%03d/year=%04d/month=%02d",
                prefix,
                event.getPickupLocationId(),
                event.getPickupDatetime().getYear(),
                event.getPickupDatetime().getMonthOfYear()
        );
    }

    public SimpleVersionedSerializer<String> getSerializer() {
        return SimpleVersionedStringSerializer.INSTANCE;
    }
}