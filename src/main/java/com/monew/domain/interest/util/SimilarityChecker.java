package com.monew.domain.interest.util;

public final class SimilarityChecker {

    public static final double DUPLICATE_THRESHOLD = 0.8;

    private SimilarityChecker() {
    }

    public static double similarity(String a, String b) {
        if (a == null || b == null) {
            return 0.0;
        }
        String left = a.toLowerCase();
        String right = b.toLowerCase();
        if (left.equals(right)) {
            return 1.0;
        }
        int maxLen = Math.max(left.length(), right.length());
        if (maxLen == 0) {
            return 1.0;
        }
        int distance = levenshtein(left, right);
        return 1.0 - ((double) distance / maxLen);
    }

    public static boolean isDuplicate(String a, String b) {
        return similarity(a, b) >= DUPLICATE_THRESHOLD;
    }

    private static int levenshtein(String a, String b) {
        int[] prev = new int[b.length() + 1];
        int[] curr = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            prev[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            curr[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(
                    Math.min(curr[j - 1] + 1, prev[j] + 1),
                    prev[j - 1] + cost
                );
            }
            int[] tmp = prev;
            prev = curr;
            curr = tmp;
        }
        return prev[b.length()];
    }
}
