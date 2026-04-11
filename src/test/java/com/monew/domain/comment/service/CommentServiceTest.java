package com.monew.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.monew.domain.article.entity.Article;
import com.monew.domain.article.repository.ArticleRepository;
import com.monew.domain.comment.dto.CommentDto;
import com.monew.domain.comment.dto.request.CommentCreateRequest;
import com.monew.domain.comment.dto.request.CommentUpdateRequest;
import com.monew.domain.comment.entity.Comment;
import com.monew.domain.comment.entity.CommentLike;
import com.monew.domain.comment.event.CommentLikedEvent;
import com.monew.domain.comment.exception.CommentNotFoundException;
import com.monew.domain.comment.exception.CommentNotOwnedException;
import com.monew.domain.comment.mapper.CommentMapper;
import com.monew.domain.comment.repository.CommentLikeRepository;
import com.monew.domain.comment.repository.CommentRepository;
import com.monew.domain.user.entity.User;
import com.monew.domain.user.repository.UserRepository;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private final CommentMapper commentMapper = new CommentMapper();

    private CommentService commentService;

    private User author;
    private User otherUser;
    private Article article;

    @BeforeEach
    void setUp() throws Exception {
        commentService = new CommentService(
            commentRepository,
            commentLikeRepository,
            articleRepository,
            userRepository,
            commentMapper,
            eventPublisher
        );

        author = User.builder().email("a@a.com").nickname("작성자").password("p").build();
        setId(author, UUID.randomUUID());
        otherUser = User.builder().email("b@b.com").nickname("타인").password("p").build();
        setId(otherUser, UUID.randomUUID());
        article = Article.builder()
            .source("NAVER")
            .sourceUrl("https://x")
            .title("기사")
            .summary("요약")
            .publishedAt(java.time.Instant.now())
            .build();
        setId(article, UUID.randomUUID());
    }

    private static void setId(Object target, UUID id) throws Exception {
        Class<?> current = target.getClass();
        while (current != null) {
            try {
                Field field = current.getDeclaredField("id");
                field.setAccessible(true);
                field.set(target, id);
                return;
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new IllegalStateException("id field not found");
    }

    @Nested
    class Create {

        @Test
        void 성공() {
            given(userRepository.findById(author.getId())).willReturn(Optional.of(author));
            given(articleRepository.findById(article.getId())).willReturn(Optional.of(article));
            given(commentRepository.save(any(Comment.class))).willAnswer(inv -> inv.getArgument(0));

            CommentDto dto = commentService.create(author.getId(),
                new CommentCreateRequest(article.getId(), "내용"));

            assertThat(dto.content()).isEqualTo("내용");
            assertThat(dto.userId()).isEqualTo(author.getId());
            assertThat(dto.likedByMe()).isFalse();
        }
    }

    @Nested
    class Update {

        @Test
        void 본인_댓글_수정_성공() throws Exception {
            Comment comment = Comment.builder().article(article).user(author).content("원본").build();
            setId(comment, UUID.randomUUID());

            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));
            given(userRepository.findById(author.getId())).willReturn(Optional.of(author));
            given(commentLikeRepository.existsByComment_IdAndUser_Id(comment.getId(), author.getId()))
                .willReturn(false);

            CommentDto dto = commentService.update(author.getId(), comment.getId(),
                new CommentUpdateRequest("수정됨"));

            assertThat(dto.content()).isEqualTo("수정됨");
            assertThat(comment.getContent()).isEqualTo("수정됨");
        }

        @Test
        void 타인_댓글_수정_시_예외() throws Exception {
            Comment comment = Comment.builder().article(article).user(author).content("원본").build();
            setId(comment, UUID.randomUUID());

            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));
            given(userRepository.findById(otherUser.getId())).willReturn(Optional.of(otherUser));

            assertThatThrownBy(() -> commentService.update(otherUser.getId(), comment.getId(),
                new CommentUpdateRequest("수정됨")))
                .isInstanceOf(CommentNotOwnedException.class);
        }
    }

    @Nested
    class SoftDelete {

        @Test
        void 본인_삭제() throws Exception {
            Comment comment = Comment.builder().article(article).user(author).content("x").build();
            setId(comment, UUID.randomUUID());

            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));
            given(userRepository.findById(author.getId())).willReturn(Optional.of(author));

            commentService.softDelete(author.getId(), comment.getId());

            assertThat(comment.isDeleted()).isTrue();
        }

        @Test
        void 삭제된_댓글_조회_시_NotFound() throws Exception {
            Comment comment = Comment.builder().article(article).user(author).content("x").build();
            setId(comment, UUID.randomUUID());
            comment.softDelete();

            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));

            assertThatThrownBy(() -> commentService.softDelete(author.getId(), comment.getId()))
                .isInstanceOf(CommentNotFoundException.class);
        }
    }

    @Nested
    class Like {

        @Test
        void 좋아요_성공_이벤트_발행() throws Exception {
            Comment comment = Comment.builder().article(article).user(author).content("x").build();
            setId(comment, UUID.randomUUID());

            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));
            given(userRepository.findById(otherUser.getId())).willReturn(Optional.of(otherUser));
            given(commentLikeRepository.existsByComment_IdAndUser_Id(comment.getId(), otherUser.getId()))
                .willReturn(false);

            CommentDto dto = commentService.like(otherUser.getId(), comment.getId());

            assertThat(dto.likedByMe()).isTrue();
            assertThat(comment.getLikeCount()).isEqualTo(1);
            verify(commentLikeRepository).save(any(CommentLike.class));
            verify(eventPublisher).publishEvent(any(CommentLikedEvent.class));
        }

        @Test
        void 본인_댓글_좋아요는_이벤트_미발행() throws Exception {
            Comment comment = Comment.builder().article(article).user(author).content("x").build();
            setId(comment, UUID.randomUUID());

            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));
            given(userRepository.findById(author.getId())).willReturn(Optional.of(author));
            given(commentLikeRepository.existsByComment_IdAndUser_Id(comment.getId(), author.getId()))
                .willReturn(false);

            commentService.like(author.getId(), comment.getId());

            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        void 이미_좋아요_한_경우_중복_생성하지_않음() throws Exception {
            Comment comment = Comment.builder().article(article).user(author).content("x").build();
            setId(comment, UUID.randomUUID());
            comment.increaseLikeCount();

            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));
            given(userRepository.findById(otherUser.getId())).willReturn(Optional.of(otherUser));
            given(commentLikeRepository.existsByComment_IdAndUser_Id(comment.getId(), otherUser.getId()))
                .willReturn(true);

            CommentDto dto = commentService.like(otherUser.getId(), comment.getId());

            assertThat(dto.likedByMe()).isTrue();
            assertThat(comment.getLikeCount()).isEqualTo(1);
            verify(commentLikeRepository, never()).save(any());
        }

        @Test
        void 좋아요_취소() throws Exception {
            Comment comment = Comment.builder().article(article).user(author).content("x").build();
            setId(comment, UUID.randomUUID());
            comment.increaseLikeCount();
            CommentLike like = CommentLike.of(comment, otherUser);

            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));
            given(commentLikeRepository.findByComment_IdAndUser_Id(eq(comment.getId()), eq(otherUser.getId())))
                .willReturn(Optional.of(like));

            CommentDto dto = commentService.unlike(otherUser.getId(), comment.getId());

            assertThat(dto.likedByMe()).isFalse();
            assertThat(comment.getLikeCount()).isEqualTo(0);
            verify(commentLikeRepository).delete(like);
        }
    }
}
