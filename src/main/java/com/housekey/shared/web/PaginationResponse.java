package com.housekey.shared.web;

import org.springframework.data.domain.Page;

public record PaginationResponse(
        int page,
        int perPage,
        Integer prePage,
        Integer nextPage,
        long total,
        int totalPages) {

    public static PaginationResponse from(Page<?> result, int requestedPage, int requestedSize) {
        int totalPages = result.getTotalPages();
        Integer prePage = requestedPage > 1 ? requestedPage - 1 : null;
        Integer nextPage = requestedPage < totalPages ? requestedPage + 1 : null;

        return new PaginationResponse(
                requestedPage,
                requestedSize,
                prePage,
                nextPage,
                result.getTotalElements(),
                totalPages);
    }
}
