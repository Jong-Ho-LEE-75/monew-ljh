package com.monew.domain.interest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.monew.common.dto.CursorRequest;
import com.monew.common.dto.PageResponse;
import com.monew.domain.article.dto.SortDirection;
import com.monew.domain.interest.dto.InterestDto;
import com.monew.domain.interest.dto.InterestSearchCondition;
import com.monew.domain.interest.dto.InterestSortBy;
import com.monew.domain.interest.dto.request.InterestRegisterRequest;
import com.monew.domain.interest.dto.request.InterestUpdateRequest;
import com.monew.domain.interest.service.InterestService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class InterestControllerTest {

    @Mock
    private InterestService interestService;

    @InjectMocks
    private InterestController controller;

    private InterestDto sampleDto() {
        return new InterestDto(UUID.randomUUID(), "관심사", List.of("k1"), 0L, false, Instant.now());
    }

    @Test
    void register_201() {
        InterestRegisterRequest req = new InterestRegisterRequest("관심사", List.of("k1"));
        InterestDto dto = sampleDto();
        given(interestService.register(req)).willReturn(dto);

        ResponseEntity<InterestDto> response = controller.register(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void findAll_위임() {
        UUID userId = UUID.randomUUID();
        PageResponse<InterestDto> page = new PageResponse<>(List.of(sampleDto()), null, 1, false);
        given(interestService.findAll(any(InterestSearchCondition.class), any(CursorRequest.class), eq(userId)))
            .willReturn(page);

        ResponseEntity<PageResponse<InterestDto>> response = controller.findAll(
            "kw", InterestSortBy.SUBSCRIBER_COUNT, SortDirection.DESC, null, 10, userId);

        assertThat(response.getBody()).isEqualTo(page);
    }

    @Test
    void updateKeywords_위임() {
        UUID interestId = UUID.randomUUID();
        InterestUpdateRequest req = new InterestUpdateRequest(List.of("new"));
        InterestDto dto = sampleDto();
        given(interestService.updateKeywords(interestId, req)).willReturn(dto);

        ResponseEntity<InterestDto> response = controller.updateKeywords(interestId, req);

        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void delete_204() {
        UUID interestId = UUID.randomUUID();

        ResponseEntity<Void> response = controller.delete(interestId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        then(interestService).should(times(1)).delete(interestId);
    }

    @Test
    void subscribe_201() {
        UUID interestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        InterestDto dto = sampleDto();
        given(interestService.subscribe(userId, interestId)).willReturn(dto);

        ResponseEntity<InterestDto> response = controller.subscribe(interestId, userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void unsubscribe_204() {
        UUID interestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ResponseEntity<Void> response = controller.unsubscribe(interestId, userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        then(interestService).should(times(1)).unsubscribe(userId, interestId);
    }
}
