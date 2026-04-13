package com.monew.common.dto;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
    List<T> content,
    String nextCursor,
    int size,
    boolean hasNext,
    Long totalElements
) {

    public static <T> PageResponse<T> of(List<T> items, int requestedSize, Function<T, String> cursorExtractor) {
        boolean hasNext = items.size() > requestedSize;
        List<T> trimmed = hasNext ? items.subList(0, requestedSize) : items;
        String nextCursor = hasNext && !trimmed.isEmpty()
            ? cursorExtractor.apply(trimmed.get(trimmed.size() - 1))
            : null;
        return new PageResponse<>(trimmed, nextCursor, trimmed.size(), hasNext, null);
    }

    public static <T> PageResponse<T> of(List<T> items, int requestedSize, Function<T, String> cursorExtractor, long totalElements) {
        boolean hasNext = items.size() > requestedSize;
        List<T> trimmed = hasNext ? items.subList(0, requestedSize) : items;
        String nextCursor = hasNext && !trimmed.isEmpty()
            ? cursorExtractor.apply(trimmed.get(trimmed.size() - 1))
            : null;
        return new PageResponse<>(trimmed, nextCursor, trimmed.size(), hasNext, totalElements);
    }
}
