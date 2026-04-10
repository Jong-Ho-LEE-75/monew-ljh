package com.monew.domain.activity.service;

import com.monew.domain.activity.repository.UserActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserActivityService {

    private final UserActivityRepository userActivityRepository;

    // TODO: getActivity(userId), initializeOnUserCreated, projectCommentCreated,
    //       projectCommentLikeToggled, projectArticleViewed, projectSubscriptionChanged
}
