package com.monew.domain.article.backup;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "monew.backup", name = "type", havingValue = "s3")
public class S3ArticleBackupStorage implements ArticleBackupStorage {

    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String KEY_PREFIX = "articles/";

    private final S3Client s3Client;
    private final String bucket;

    public S3ArticleBackupStorage(
        S3Client s3Client,
        @Value("${monew.backup.s3.bucket}") String bucket
    ) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    @Override
    public void upload(LocalDate date, String content) {
        String key = keyFor(date);
        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType("application/json")
            .build();
        s3Client.putObject(request, RequestBody.fromBytes(content.getBytes(StandardCharsets.UTF_8)));
        log.info("S3 백업 업로드 완료 bucket={} key={}", bucket, key);
    }

    @Override
    public Optional<String> download(LocalDate date) {
        try {
            ResponseBytes<GetObjectResponse> bytes = s3Client.getObjectAsBytes(
                GetObjectRequest.builder().bucket(bucket).key(keyFor(date)).build());
            return Optional.of(bytes.asUtf8String());
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<LocalDate> listDates() {
        return s3Client.listObjectsV2(ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(KEY_PREFIX)
                .build())
            .contents().stream()
            .map(S3Object::key)
            .map(this::parseDateFromKey)
            .filter(java.util.Objects::nonNull)
            .sorted(java.util.Comparator.reverseOrder())
            .toList();
    }

    private String keyFor(LocalDate date) {
        return KEY_PREFIX + date.format(FILE_DATE) + ".json";
    }

    private LocalDate parseDateFromKey(String key) {
        try {
            String name = key.substring(KEY_PREFIX.length(), key.length() - ".json".length());
            return LocalDate.parse(name, FILE_DATE);
        } catch (Exception e) {
            return null;
        }
    }
}
