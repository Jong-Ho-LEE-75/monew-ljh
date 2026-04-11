package com.monew.domain.article.backup;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConditionalOnProperty(prefix = "monew.backup", name = "type", havingValue = "s3")
public class S3BackupConfig {

    @Bean
    public S3Client s3Client(@Value("${monew.backup.s3.region:ap-northeast-2}") String region) {
        return S3Client.builder()
            .region(Region.of(region))
            .build();
    }
}
