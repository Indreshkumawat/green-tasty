package com.restaurantapp.util;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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

    @Bean("passwordEncoder")
    BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(12);
    }

    @Bean("credentialsProvider")
    AWSCredentialsProvider userCredentialsProvider(){
        AWSStaticCredentialsProvider awsStaticCredentialsProvider = new AWSStaticCredentialsProvider(
                new BasicSessionCredentials(accessKey, secretKey, sessionToken));

        AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClient.builder()
                .withCredentials(awsStaticCredentialsProvider)
                .withRegion(region)
                .build();

        return new STSAssumeRoleSessionCredentialsProvider.Builder(roleArn, "DeveloperAccessRoleTeam9")
                .withStsClient(stsClient)
                .build();
    }


    @Bean
    @Qualifier("amazonSQSClient")
    AmazonSQS provideAmazonSQSClient(@Qualifier("credentialsProvider") AWSCredentialsProvider credentialsProvider) {
        return AmazonSQSClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(region)
                .build();
    }


    @Bean
    @Qualifier("s3Client")
    AmazonS3 provideS3Client(@Qualifier("credentialsProvider") AWSCredentialsProvider credentialsProvider) {
        return AmazonS3ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(region)
                .build();
    }

    @Bean
    @Qualifier("newDynamoDBClient")
    AmazonDynamoDB provideNewDynamoDBClient(@Qualifier("credentialsProvider") AWSCredentialsProvider credentialsProvider) {
        return AmazonDynamoDBClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(region)
                .build();
    }

    @Bean
    @Qualifier("dynamoDBClient")
    DynamoDB provideDynamoDBClient(@Qualifier("newDynamoDBClient") AmazonDynamoDB amazonDynamoDbClient) {
        return new DynamoDB(amazonDynamoDbClient);
    }
}
