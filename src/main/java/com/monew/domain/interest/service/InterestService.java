package com.monew.domain.interest.service;

import com.monew.common.dto.CursorRequest;
import com.monew.common.dto.PageResponse;
import com.monew.domain.interest.dto.InterestDto;
import com.monew.domain.interest.dto.request.InterestRegisterRequest;
import com.monew.domain.interest.dto.request.InterestUpdateRequest;
import com.monew.domain.interest.entity.Interest;
import com.monew.domain.interest.exception.DuplicateInterestNameException;
import com.monew.domain.interest.exception.InterestNotFoundException;
import com.monew.domain.interest.mapper.InterestMapper;
import com.monew.domain.interest.repository.InterestRepository;
import com.monew.domain.interest.util.SimilarityChecker;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterestService {

    private final InterestRepository interestRepository;
    private final InterestMapper interestMapper;

    @Transactional
    public InterestDto register(InterestRegisterRequest request) {
        checkDuplicateName(request.name());

        Interest interest = Interest.builder()
            .name(request.name())
            .keywords(request.keywords())
            .build();

        Interest saved = interestRepository.save(interest);
        return interestMapper.toDto(saved, false);
    }

    public PageResponse<InterestDto> findAll(CursorRequest cursorRequest) {
        int size = cursorRequest.sizeOrDefault();
        List<Interest> page = interestRepository.findPage(
            cursorRequest.cursor(),
            PageRequest.of(0, size + 1)
        );
        List<InterestDto> dtos = page.stream()
            .map(interest -> interestMapper.toDto(interest, false))
            .toList();
        return PageResponse.of(dtos, size, InterestDto::name);
    }

    @Transactional
    public InterestDto updateKeywords(UUID interestId, InterestUpdateRequest request) {
        Interest interest = findInterestWithKeywords(interestId);
        interest.replaceKeywords(request.keywords());
        return interestMapper.toDto(interest, false);
    }

    @Transactional
    public void delete(UUID interestId) {
        Interest interest = findById(interestId);
        interestRepository.delete(interest);
    }

    private Interest findById(UUID interestId) {
        return interestRepository.findById(interestId)
            .orElseThrow(() -> new InterestNotFoundException(interestId));
    }

    private Interest findInterestWithKeywords(UUID interestId) {
        return interestRepository.findByIdWithKeywords(interestId)
            .orElseThrow(() -> new InterestNotFoundException(interestId));
    }

    private void checkDuplicateName(String requested) {
        List<Interest> all = interestRepository.findAll();
        for (Interest existing : all) {
            double sim = SimilarityChecker.similarity(requested, existing.getName());
            if (sim >= SimilarityChecker.DUPLICATE_THRESHOLD) {
                throw new DuplicateInterestNameException(requested, existing.getName(), sim);
            }
        }
    }
}
