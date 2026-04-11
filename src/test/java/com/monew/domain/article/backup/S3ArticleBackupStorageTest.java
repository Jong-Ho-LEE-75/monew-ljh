package com.monew.domain.article.backup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

class S3ArticleBackupStorageTest {

    private S3Client s3Client;
    private S3ArticleBackupStorage storage;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        storage = new S3ArticleBackupStorage(s3Client, "bucket-x");
    }

    @Test
    void upload_PutObject_호출() {
        storage.upload(LocalDate.of(2026, 4, 10), "{\"a\":1}");

        then(s3Client).should(times(1)).putObject(any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class));
    }

    @Test
    void download_정상_반환() {
        @SuppressWarnings("unchecked")
        ResponseBytes<GetObjectResponse> bytes = mock(ResponseBytes.class);
        given(bytes.asUtf8String()).willReturn("payload");
        given(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).willReturn(bytes);

        Optional<String> result = storage.download(LocalDate.of(2026, 4, 10));

        assertThat(result).contains("payload");
    }

    @Test
    void download_NoSuchKey_빈값() {
        given(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
            .willThrow(NoSuchKeyException.builder().message("missing").build());

        Optional<String> result = storage.download(LocalDate.of(2026, 4, 10));

        assertThat(result).isEmpty();
    }

    @Test
    void listDates_역순_정렬() {
        ListObjectsV2Response resp = ListObjectsV2Response.builder()
            .contents(
                S3Object.builder().key("articles/2026-04-10.json").build(),
                S3Object.builder().key("articles/2026-04-09.json").build(),
                S3Object.builder().key("articles/잘못된키.json").build(),
                S3Object.builder().key("articles/2026-04-11.json").build()
            )
            .build();
        given(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).willReturn(resp);

        List<LocalDate> dates = storage.listDates();

        assertThat(dates).containsExactly(
            LocalDate.of(2026, 4, 11),
            LocalDate.of(2026, 4, 10),
            LocalDate.of(2026, 4, 9)
        );
    }
}
