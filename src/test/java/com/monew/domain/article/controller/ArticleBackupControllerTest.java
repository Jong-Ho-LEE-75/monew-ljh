package com.monew.domain.article.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.monew.domain.article.backup.ArticleBackupService;
import com.monew.domain.article.dto.ArticleRestoreResponse;
import java.time.LocalDate;
import java.util.List;
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

    @Test
    void listBackupDates_서비스_위임() {
        List<LocalDate> dates = List.of(LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 9));
        given(articleBackupService.listBackupDates()).willReturn(dates);

        ResponseEntity<List<LocalDate>> response = controller.listBackupDates();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).containsExactlyElementsOf(dates);
    }

    @Test
    void restore_복구_건수_반환() {
        LocalDate date = LocalDate.of(2026, 4, 10);
        given(articleBackupService.restore(date)).willReturn(7);

        ResponseEntity<ArticleRestoreResponse> response = controller.restore(date);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().date()).isEqualTo(date);
        assertThat(response.getBody().restored()).isEqualTo(7);
    }
}
