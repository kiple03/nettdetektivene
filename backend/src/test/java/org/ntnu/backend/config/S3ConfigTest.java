package org.ntnu.backend.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;

class S3ConfigTest {

    @Test
    void s3Client_createsClientWithValidConfig() {
        S3Config config = new S3Config();
        ReflectionTestUtils.setField(config, "endpoint", "http://localhost:9000");
        ReflectionTestUtils.setField(config, "accessKey", "minioadmin");
        ReflectionTestUtils.setField(config, "secretKey", "minioadmin");
        ReflectionTestUtils.setField(config, "region", "us-east-1");

        S3Client client = config.s3Client();

        assertThat(client).isNotNull();
        client.close();
    }

    @Test
    void s3Client_throwsOnInvalidEndpoint() {
        S3Config config = new S3Config();
        ReflectionTestUtils.setField(config, "endpoint", "not a valid uri!!");
        ReflectionTestUtils.setField(config, "accessKey", "key");
        ReflectionTestUtils.setField(config, "secretKey", "secret");
        ReflectionTestUtils.setField(config, "region", "us-east-1");

        assertThatThrownBy(config::s3Client)
                .isInstanceOf(Exception.class);
    }

    @Test
    void s3Client_usesCorrectRegion() {
        S3Config config = new S3Config();
        ReflectionTestUtils.setField(config, "endpoint", "http://localhost:9000");
        ReflectionTestUtils.setField(config, "accessKey", "key");
        ReflectionTestUtils.setField(config, "secretKey", "secret");
        ReflectionTestUtils.setField(config, "region", "eu-west-1");

        // Should not throw — region is valid
        S3Client client = config.s3Client();
        assertThat(client).isNotNull();
        client.close();
    }
}