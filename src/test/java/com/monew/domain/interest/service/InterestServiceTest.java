package com.monew.domain.interest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.monew.common.dto.CursorRequest;
import com.monew.common.dto.PageResponse;
import com.monew.domain.interest.dto.InterestDto;
import com.monew.domain.interest.dto.request.InterestRegisterRequest;
import com.monew.domain.interest.dto.request.InterestUpdateRequest;
import com.monew.domain.interest.entity.Interest;
import com.monew.domain.interest.entity.Subscription;
import com.monew.domain.interest.exception.DuplicateInterestNameException;
import com.monew.domain.interest.exception.InterestNotFoundException;
import com.monew.domain.interest.exception.SubscriptionNotFoundException;
import com.monew.domain.user.entity.User;
import com.monew.domain.user.exception.UserNotFoundException;
import com.monew.domain.interest.mapper.InterestMapper;
import com.monew.domain.interest.repository.InterestRepository;
import com.monew.domain.interest.repository.SubscriptionRepository;
import com.monew.domain.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class InterestServiceTest {

    @Mock
    private InterestRepository interestRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InterestMapper interestMapper;

    @InjectMocks
    private InterestService interestService;

    private Interest newInterest(String name, List<String> keywords) {
        return Interest.builder().name(name).keywords(keywords).build();
    }

    private InterestDto toDto(Interest interest) {
        return new InterestDto(
            UUID.randomUUID(),
            interest.getName(),
            List.of("k"),
            0,
            false,
            Instant.now()
        );
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        void 빈_DB에_정상_등록() {
            InterestRegisterRequest request = new InterestRegisterRequest("Spring", List.of("Boot"));
            Interest saved = newInterest("Spring", List.of("Boot"));

            given(interestRepository.findAll()).willReturn(List.of());
            given(interestRepository.save(any(Interest.class))).willReturn(saved);
            given(interestMapper.toDto(saved, false)).willReturn(toDto(saved));

            InterestDto result = interestService.register(request);

            assertThat(result.name()).isEqualTo("Spring");
        }

        @Test
        void 완전_중복_이름_예외() {
            Interest existing = newInterest("Spring", List.of("Boot"));
            given(interestRepository.findAll()).willReturn(List.of(existing));

            assertThatThrownBy(() -> interestService.register(
                new InterestRegisterRequest("Spring", List.of("Boot"))))
                .isInstanceOf(DuplicateInterestNameException.class);

            verify(interestRepository, never()).save(any());
        }

        @Test
        void 유사도_80퍼센트_예외() {
            Interest existing = newInterest("abcde", List.of("k"));
            given(interestRepository.findAll()).willReturn(List.of(existing));

            assertThatThrownBy(() -> interestService.register(
                new InterestRegisterRequest("abcdX", List.of("k"))))
                .isInstanceOf(DuplicateInterestNameException.class);
        }

        @Test
        void 유사하지_않은_이름_등록_통과() {
            Interest existing = newInterest("GoLang", List.of("k"));
            Interest saved = newInterest("Spring", List.of("Boot"));

            given(interestRepository.findAll()).willReturn(List.of(existing));
            given(interestRepository.save(any(Interest.class))).willReturn(saved);
            given(interestMapper.toDto(saved, false)).willReturn(toDto(saved));

            InterestDto result = interestService.register(
                new InterestRegisterRequest("Spring", List.of("Boot")));

            assertThat(result.name()).isEqualTo("Spring");
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        void 다음_페이지_존재_시_nextCursor_반환() {
            Interest a = newInterest("A", List.of());
            Interest b = newInterest("B", List.of());
            Interest c = newInterest("C", List.of());

            given(interestRepository.findPage(any(), any(Pageable.class)))
                .willReturn(List.of(a, b, c));
            given(interestMapper.toDto(a, false))
                .willReturn(new InterestDto(UUID.randomUUID(), "A", List.of(), 0, false, Instant.now()));
            given(interestMapper.toDto(b, false))
                .willReturn(new InterestDto(UUID.randomUUID(), "B", List.of(), 0, false, Instant.now()));

            PageResponse<InterestDto> result = interestService.findAll(new CursorRequest(null, 2), null);

            assertThat(result.content()).hasSize(2);
            assertThat(result.hasNext()).isTrue();
            assertThat(result.nextCursor()).isEqualTo("B");
        }

        @Test
        void 마지막_페이지는_nextCursor_null() {
            Interest a = newInterest("A", List.of());

            given(interestRepository.findPage(any(), any(Pageable.class)))
                .willReturn(List.of(a));
            given(interestMapper.toDto(a, false))
                .willReturn(new InterestDto(UUID.randomUUID(), "A", List.of(), 0, false, Instant.now()));

            PageResponse<InterestDto> result = interestService.findAll(new CursorRequest(null, 10), null);

            assertThat(result.hasNext()).isFalse();
            assertThat(result.nextCursor()).isNull();
        }
    }

    @Nested
    @DisplayName("updateKeywords")
    class UpdateKeywords {

        @Test
        void 키워드_교체() {
            UUID id = UUID.randomUUID();
            Interest interest = newInterest("Spring", List.of("old"));

            given(interestRepository.findByIdWithKeywords(id)).willReturn(Optional.of(interest));
            given(interestMapper.toDto(interest, false))
                .willReturn(new InterestDto(id, "Spring", List.of("new1", "new2"), 0, false, Instant.now()));

            InterestDto result = interestService.updateKeywords(
                id, new InterestUpdateRequest(List.of("new1", "new2")));

            assertThat(interest.getKeywords()).hasSize(2);
            assertThat(result.keywords()).containsExactly("new1", "new2");
        }

        @Test
        void 존재하지_않는_관심사_예외() {
            UUID id = UUID.randomUUID();
            given(interestRepository.findByIdWithKeywords(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> interestService.updateKeywords(
                id, new InterestUpdateRequest(List.of("k"))))
                .isInstanceOf(InterestNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        void 정상_삭제() {
            UUID id = UUID.randomUUID();
            Interest interest = newInterest("Spring", List.of());
            given(interestRepository.findById(id)).willReturn(Optional.of(interest));

            interestService.delete(id);

            verify(interestRepository).delete(interest);
        }

        @Test
        void 존재하지_않는_관심사_삭제_예외() {
            UUID id = UUID.randomUUID();
            given(interestRepository.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> interestService.delete(id))
                .isInstanceOf(InterestNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("subscribe")
    class Subscribe {

        @Test
        void 정상_구독_시_카운트_증가() {
            UUID userId = UUID.randomUUID();
            UUID interestId = UUID.randomUUID();
            User user = User.builder().email("a@a.com").nickname("n").password("p12345").build();
            Interest interest = newInterest("Spring", List.of());

            given(subscriptionRepository.existsByUser_IdAndInterest_Id(userId, interestId)).willReturn(false);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(interestRepository.findByIdWithKeywords(interestId)).willReturn(Optional.of(interest));
            given(interestMapper.toDto(interest, true))
                .willReturn(new InterestDto(interestId, "Spring", List.of(), 1, true, Instant.now()));

            InterestDto result = interestService.subscribe(userId, interestId);

            assertThat(interest.getSubscriberCount()).isEqualTo(1);
            assertThat(result.subscribedByMe()).isTrue();
            verify(subscriptionRepository).save(any(Subscription.class));
        }

        @Test
        void 이미_구독_중이면_신규_저장_없이_반환() {
            UUID userId = UUID.randomUUID();
            UUID interestId = UUID.randomUUID();
            Interest interest = newInterest("Spring", List.of());

            given(subscriptionRepository.existsByUser_IdAndInterest_Id(userId, interestId)).willReturn(true);
            given(interestRepository.findByIdWithKeywords(interestId)).willReturn(Optional.of(interest));
            given(interestMapper.toDto(interest, true))
                .willReturn(new InterestDto(interestId, "Spring", List.of(), 0, true, Instant.now()));

            interestService.subscribe(userId, interestId);

            verify(subscriptionRepository, never()).save(any());
            assertThat(interest.getSubscriberCount()).isEqualTo(0);
        }

        @Test
        void 존재하지_않는_사용자_구독_예외() {
            UUID userId = UUID.randomUUID();
            UUID interestId = UUID.randomUUID();

            given(subscriptionRepository.existsByUser_IdAndInterest_Id(userId, interestId)).willReturn(false);
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> interestService.subscribe(userId, interestId))
                .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("unsubscribe")
    class Unsubscribe {

        @Test
        void 정상_구독_해제_시_카운트_감소() {
            UUID userId = UUID.randomUUID();
            UUID interestId = UUID.randomUUID();
            User user = User.builder().email("a@a.com").nickname("n").password("p12345").build();
            Interest interest = newInterest("Spring", List.of());
            interest.increaseSubscriber();
            Subscription subscription = Subscription.of(user, interest);

            given(subscriptionRepository.findByUser_IdAndInterest_Id(userId, interestId))
                .willReturn(Optional.of(subscription));

            interestService.unsubscribe(userId, interestId);

            assertThat(interest.getSubscriberCount()).isEqualTo(0);
            verify(subscriptionRepository).delete(subscription);
        }

        @Test
        void 구독_없을_시_예외() {
            UUID userId = UUID.randomUUID();
            UUID interestId = UUID.randomUUID();

            given(subscriptionRepository.findByUser_IdAndInterest_Id(userId, interestId))
                .willReturn(Optional.empty());

            assertThatThrownBy(() -> interestService.unsubscribe(userId, interestId))
                .isInstanceOf(SubscriptionNotFoundException.class);
        }
    }
}
