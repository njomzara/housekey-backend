package com.housekey.shared.web;

import java.util.List;

public record PageResponse<T>(
        List<T> data,
        PaginationResponse pagination) {
}
