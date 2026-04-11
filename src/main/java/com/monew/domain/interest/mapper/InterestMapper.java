package com.monew.domain.interest.mapper;

import com.monew.domain.interest.dto.InterestDto;
import com.monew.domain.interest.entity.Interest;
import com.monew.domain.interest.entity.InterestKeyword;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class InterestMapper {

    public InterestDto toDto(Interest interest, boolean subscribedByMe) {
        List<String> keywords = interest.getKeywords().stream()
            .map(InterestKeyword::getKeyword)
            .toList();
        return new InterestDto(
            interest.getId(),
            interest.getName(),
            keywords,
            interest.getSubscriberCount(),
            subscribedByMe,
            interest.getCreatedAt()
        );
    }
}
