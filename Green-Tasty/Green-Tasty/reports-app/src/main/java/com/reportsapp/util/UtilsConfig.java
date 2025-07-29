package com.reportsapp.util;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;

@Configuration
public class UtilsConfig {
    @Value("${aws.access.key.id}")
    private String accessKey;

    @Value("${aws.secret.access.key}")
    private String secretKey;

    @Value("${aws.session.token}")
    private String sessionToken;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.role.arn}")
    private String roleArn;

    @Bean("credentialsProvider")
    public StsAssumeRoleCredentialsProvider credentialsProvider() {
        AwsSessionCredentials sessionCredentials = AwsSessionCredentials.create(accessKey, secretKey, sessionToken);

        StsClient stsClient = StsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(sessionCredentials))
                .build();

        return StsAssumeRoleCredentialsProvider.builder()
                .stsClient(stsClient)
                .refreshRequest(r -> r.roleArn(roleArn).roleSessionName("DeveloperAccessRoleTeam9"))
                .build();
    }

    @Bean("v1CredentialsProvider")
    public AWSCredentialsProvider v1CredentialsProvider(@Qualifier("credentialsProvider") software.amazon.awssdk.auth.credentials.AwsCredentialsProvider v2Provider) {
        return new AWSCredentialsProvider() {
            @Override
            public AWSCredentials getCredentials() {
                AwsCredentials creds = v2Provider.resolveCredentials();
                return new com.amazonaws.auth.BasicSessionCredentials(
                        creds.accessKeyId(),
                        creds.secretAccessKey(),
                        creds instanceof software.amazon.awssdk.auth.credentials.AwsSessionCredentials
                                ? ((software.amazon.awssdk.auth.credentials.AwsSessionCredentials) creds).sessionToken()
                                : null
                );
            }
            @Override
            public void refresh() {}
        };
    }

    @Bean("sesClient")
    public AmazonSimpleEmailService provideSesClient(@Qualifier("v1CredentialsProvider") AWSCredentialsProvider credentialsProvider) {
        return AmazonSimpleEmailServiceClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(region)
                .build();
    }

    @Bean
    public SqsAsyncClient sqsAsyncClient(@Qualifier("credentialsProvider") software.amazon.awssdk.auth.credentials.AwsCredentialsProvider v2Provider) {
        return SqsAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(v2Provider)
                .build();
    }

    @Bean("newDynamoDBClient")
    //@Qualifier("newDynamoDBClient")
    AmazonDynamoDB provideNewDynamoDBClient(@Qualifier("v1CredentialsProvider") AWSCredentialsProvider credentialsProvider) {
        return AmazonDynamoDBClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(region)
                .build();
    }

    @Bean("dynamoDBClient")
    //@Qualifier("dynamoDBClient")
    DynamoDB provideDynamoDBClient(@Qualifier("newDynamoDBClient") AmazonDynamoDB amazonDynamoDbClient) {
        return new DynamoDB(amazonDynamoDbClient);
    }
}
