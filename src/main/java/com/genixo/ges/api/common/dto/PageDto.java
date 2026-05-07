package com.genixo.ges.api.common.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PageDto<T> {
    List<T> items;
    int page;
    int size;
    long totalItems;
    int totalPages;
}

