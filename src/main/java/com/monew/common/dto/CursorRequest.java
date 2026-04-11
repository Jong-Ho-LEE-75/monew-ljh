package com.monew.common.dto;

public record CursorRequest(
    String cursor,
    Integer size
) {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    public int sizeOrDefault() {
        if (size == null || size <= 0) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
}
