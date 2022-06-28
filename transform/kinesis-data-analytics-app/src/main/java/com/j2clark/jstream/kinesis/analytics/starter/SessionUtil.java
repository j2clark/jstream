package com.j2clark.jstream.kinesis.analytics.starter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.flink.kinesis.shaded.com.amazonaws.services.s3.AmazonS3;
import org.apache.flink.kinesis.shaded.com.amazonaws.services.s3.AmazonS3ClientBuilder;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.model.ListShardsRequest;
import com.amazonaws.services.kinesis.model.ListShardsResult;
import com.amazonaws.services.kinesis.model.Shard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SessionUtil {

    private static final Logger log = LoggerFactory.getLogger(SessionUtil.class);

    /**
     * Method checks if a Kinesis Stream exist
     */
    public static boolean checkIfStreamExist(String region, String streamName) {

        boolean streamExist = false;
        AmazonKinesis kinesis = AmazonKinesisClientBuilder.standard().withRegion(region).build();
        ListShardsRequest listShardsRequest = new ListShardsRequest();
        listShardsRequest.setStreamName(streamName);
        // get shards
        try {
            ListShardsResult listShardResult = kinesis.listShards(listShardsRequest);
            List<Shard> shardList = listShardResult.getShards();
            if (shardList.size() > 0)
                streamExist = true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception recieved while listing shards for stream: " + streamName);
        }
        kinesis.shutdown();
        return streamExist;
    }

    /**
     * Method checks if an S3 bucket exist
     */
    public static boolean checkIfBucketExist(String region, String s3Path) {
        boolean bucketExist = false;
        String prefix = "s3a://"; // To use in starts with
        String bktname = s3Path.substring(6);
        bktname = bktname.substring(0, bktname.indexOf("/")); // Extracting S3 Bucket name
        AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(region).build();
        try {
            if (s3Path.startsWith(prefix)) {
                if (s3.doesBucketExistV2(bktname)) {
                    bucketExist = true;
                    log.info("The provided S3 path exist: " + s3Path);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exceptions received while checking the S3 bucket: " + s3Path);
        }
        s3.shutdown();
        return bucketExist;
    }

    /**
     * Method checks if a region is valid or not
     */
    public static boolean checkIfRegionExist(String region) {
        boolean regionExist = false;
        if (region.equalsIgnoreCase(Regions.US_EAST_1.getName()) || region.equalsIgnoreCase(Regions.US_EAST_2.getName())
                || region.equalsIgnoreCase(Regions.US_WEST_1.getName())
                || region.equalsIgnoreCase(Regions.US_WEST_2.getName())) {
            regionExist = true;
            log.info("The provided region is valid: " + region);
        } else
            log.error("The provided region is not valid: " + region);
        return regionExist;
    }

    /**
     * This method validates a data format Reference:
     * <a href="https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html">SimpleDateFormat</a>
     */
    public static boolean validateDate(String streamInitialTimestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        try {
            sdf.parse(streamInitialTimestamp);
        } catch (ParseException e) {
            e.printStackTrace();
            System.out.printf("Invalid data format supplied. '%s' is not a valid value. \n", streamInitialTimestamp);
            return false;
        }
        return true;
    }

}
