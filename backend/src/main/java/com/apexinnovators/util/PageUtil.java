package com.apexinnovators.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Pageable normalizer: list "page" endpoints default to page=0, size=12 with a
 * maximum size of 100 (contract). Null or negative inputs fall back to the
 * defaults.
 */
public final class PageUtil {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 12;
    public static final int MAX_SIZE = 100;

    private PageUtil() {
    }

    public static Pageable of(Integer page, Integer size) {
        int p = (page == null || page < 0) ? DEFAULT_PAGE : page;
        int s = (size == null || size < 1) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        return PageRequest.of(p, s);
    }
}
