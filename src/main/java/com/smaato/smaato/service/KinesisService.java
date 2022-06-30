package com.smaato.smaato.service;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry;
import com.amazonaws.services.kinesis.model.PutRecordsResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.*;

@Service
@Slf4j
public class KinesisService {

    private final AWSCredentialsProvider awsCredentialsProvider;

    @Value("${kinesis.stream.name:Requests-Count}")
    private String streamName;

    @Value("${kinesis.stream.enabled:false}")
    private Boolean streamEnabled;

    @Autowired
    public KinesisService(AWSCredentialsProvider awsCredentialsProvider) {
        this.awsCredentialsProvider = awsCredentialsProvider;
    }

    public void streamData(Long requestsCount) {

        Optional.ofNullable(streamEnabled)
                .filter(Boolean::booleanValue)
                .ifPresent(b -> doStreamData(requestsCount));

    }

    private void doStreamData(Long requestsCount) {
        AmazonKinesisClientBuilder clientBuilder = AmazonKinesisClientBuilder.standard();

        log.info("Started stream data to AmazonKinesis request count :- {}",requestsCount );
        clientBuilder.setRegion("us-west-2");
        clientBuilder.setCredentials(awsCredentialsProvider);
        clientBuilder.setClientConfiguration(new ClientConfiguration());

        PutRecordsRequest putRecordsRequest = new PutRecordsRequest();
        putRecordsRequest.setStreamName(streamName);

        PutRecordsRequestEntry putRecordsRequestEntry = new PutRecordsRequestEntry();
        putRecordsRequestEntry.setData(ByteBuffer.wrap(String.valueOf(requestsCount).getBytes()));
        putRecordsRequestEntry.setPartitionKey("partitionKey-smaato");

        putRecordsRequest.setRecords(Arrays.asList(putRecordsRequestEntry));
        PutRecordsResult putRecordsResult = clientBuilder.build().putRecords(putRecordsRequest);
        log.info("Kinesis Expected Result {}", putRecordsResult);
    }

}
