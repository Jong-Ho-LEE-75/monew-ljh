package com.monew;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("로컬 MongoDB가 실행 중일 때만 활성화. CI에서는 Testcontainers 도입 후 제거 예정.")
class MonewApplicationTests {

    @Test
    void contextLoads() {
    }
}
