package com.j2clark.jstream.flink.examples.utils;

import com.j2clark.jstream.flink.examples.TripEvent;
import org.apache.avro.data.TimeConversions;
import org.apache.avro.specific.SpecificData;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.formats.avro.typeutils.AvroTypeInfo;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TripEventSchema implements SerializationSchema<TripEvent>, DeserializationSchema<TripEvent> {

    private final ObjectMapper mapper = new ObjectMapper();
    private static final Logger LOG = LoggerFactory.getLogger(TripEventSchema.class);

    static {
        SpecificData.get().addLogicalTypeConversion(new TimeConversions.TimestampConversion());
    }

    @Override
    public byte[] serialize(TripEvent event) {
        return toJson(event).getBytes();
    }

    @Override
    public TripEvent deserialize(byte[] bytes) {
        try {
            ObjectNode node = this.mapper.readValue(bytes, ObjectNode.class);

            JsonNode padding = node.get("padding");

            return TripEvent
                    .newBuilder()
                    .setVendorId(node.get("vendor_id").asInt())
                    .setPickupDatetime(new DateTime(node.get("pickup_datetime").asText()))
                    .setDropoffDatetime(new DateTime(node.get("dropoff_datetime").asText()))
                    .setPassengerCount(node.get("passenger_count").asInt())
                    .setTripDistance(node.get("trip_distance").asDouble())
                    .setRatecodeId(node.get("ratecode_id").asInt())
                    .setStoreAndFwdFlag(node.get("store_and_fwd_flag").asText())
                    .setPickupLocationId(node.get("pickup_location_id").asInt())
                    .setDropoffLocationId(node.get("dropoff_location_id").asInt())
                    .setPaymentType(node.get("payment_type").asInt())
                    .setFareAmount(node.get("fare_amount").asDouble())
                    .setExtra(node.get("extra").asDouble())
                    .setMtaTax(node.get("mta_tax").asDouble())
                    .setTipAmount(node.get("tip_amount").asDouble())
                    .setTollsAmount(node.get("tolls_amount").asDouble())
                    .setImprovementSurcharge(node.get("improvement_surcharge").asDouble())
                    .setTotalAmount(node.get("total_amount").asDouble())
                    .setTripId(node.get("trip_id").asLong(0))
                    .setType(node.get("type").asText())
                    .setPadding(padding==null ? "" : padding.asText())
                    .build();
        } catch (Exception e) {
            LOG.warn("Failed to serialize event: {}", new String(bytes), e);

            return null;
        }
    }

    @Override
    public boolean isEndOfStream(TripEvent tripEvent) {
        return false;
    }

    @Override
    public TypeInformation<TripEvent> getProducedType() {
        return new AvroTypeInfo<>(TripEvent.class);
    }


    public static String toJson(TripEvent event) {
        StringBuilder builder = new StringBuilder();

        builder.append("{");
        addField(builder, event, "vendor_id");
        builder.append(", ");
        addField(builder, "pickup_datetime", event.getPickupDatetime().getMillis());
        builder.append(", ");
        addField(builder, "dropoff_datetime", event.getDropoffDatetime().getMillis());
        builder.append(", ");
        addField(builder, event, "passenger_count");
        builder.append(", ");
        addField(builder, event, "trip_distance");
        builder.append(", ");
        addField(builder, event, "ratecode_id");
        builder.append(", ");
        addTextField(builder, event, "store_and_fwd_flag");
        builder.append(", ");
        addField(builder, event, "pickup_location_id");
        builder.append(", ");
        addField(builder, event, "dropoff_location_id");
        builder.append(", ");
        addField(builder, event, "fare_amount");
        builder.append(", ");
        addField(builder, event, "extra");
        builder.append(", ");
        addField(builder, event, "mta_tax");
        builder.append(", ");
        addField(builder, event, "tip_amount");
        builder.append(", ");
        addField(builder, event, "tolls_amount");
        builder.append(", ");
        addField(builder, event, "improvement_surcharge");
        builder.append(", ");
        addField(builder, event, "total_amount");
        builder.append(", ");
        addTextField(builder, event, "trip_id");
        builder.append(", ");
        addTextField(builder, event, "type");
        builder.append("}");

        return builder.toString();
    }

    private static void addField(StringBuilder builder, TripEvent event, String fieldName) {
        addField(builder, fieldName, event.get(fieldName));
    }

    private static void addField(StringBuilder builder, String fieldName, Object value) {
        builder.append("\"");
        builder.append(fieldName);
        builder.append("\"");

        builder.append(": ");
        builder.append(value);
    }

    private static void addTextField(StringBuilder builder, TripEvent event, String fieldName) {
        builder.append("\"");
        builder.append(fieldName);
        builder.append("\"");

        builder.append(": ");
        builder.append("\"");
        builder.append(event.get(fieldName));
        builder.append("\"");
    }
}