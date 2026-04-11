package com.monew.domain.article.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.monew.common.exception.ErrorCode;
import com.monew.common.exception.MonewException;
import com.monew.domain.article.backup.ArticleBackupService;
import com.monew.domain.article.dto.ArticleRestoreResponse;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ArticleBackupControllerTest {

    @Mock
    private ArticleBackupService articleBackupService;

    @InjectMocks
    private ArticleBackupController controller;

    @BeforeEach
    void setUp() throws Exception {
        setAdminToken("secret-token");
    }

    private void setAdminToken(String token) throws Exception {
        Field field = ArticleBackupController.class.getDeclaredField("adminToken");
        field.setAccessible(true);
        field.set(controller, token);
    }

    @Test
    void listBackupDates_서비스_위임() {
        List<LocalDate> dates = List.of(LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 9));
        given(articleBackupService.listBackupDates()).willReturn(dates);

        ResponseEntity<List<LocalDate>> response = controller.listBackupDates();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).containsExactlyElementsOf(dates);
    }

    @Test
    void restore_유효한_토큰_성공() {
        LocalDate date = LocalDate.of(2026, 4, 10);
        given(articleBackupService.restore(date)).willReturn(7);

        ResponseEntity<ArticleRestoreResponse> response = controller.restore(date, "Bearer secret-token");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().date()).isEqualTo(date);
        assertThat(response.getBody().restored()).isEqualTo(7);
    }

    @Test
    void restore_헤더_누락_시_UNAUTHENTICATED() {
        assertThatThrownBy(() -> controller.restore(LocalDate.of(2026, 4, 10), null))
            .isInstanceOf(MonewException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.UNAUTHENTICATED);
    }

    @Test
    void restore_잘못된_토큰_시_FORBIDDEN() {
        assertThatThrownBy(() -> controller.restore(LocalDate.of(2026, 4, 10), "Bearer wrong"))
            .isInstanceOf(MonewException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void restore_토큰_미설정_시_FORBIDDEN() throws Exception {
        setAdminToken("");

        assertThatThrownBy(() -> controller.restore(LocalDate.of(2026, 4, 10), "Bearer anything"))
            .isInstanceOf(MonewException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void restore_Bearer_접두어_없음_시_UNAUTHENTICATED() {
        assertThatThrownBy(() -> controller.restore(LocalDate.of(2026, 4, 10), "secret-token"))
            .isInstanceOf(MonewException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.UNAUTHENTICATED);
    }
}
