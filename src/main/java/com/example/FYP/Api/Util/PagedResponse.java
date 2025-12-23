package com.example.FYP.Api.Util;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PagedResponse<T> {
    private List<T> content;
    private Metadata metadata;

    public PagedResponse(List<T> content, Metadata metadata) {
        this.content = content;
        this.metadata = metadata;
    }

    public static <T> PagedResponse<T> fromPage(Page<T> page) {
        Metadata metadata = new Metadata(
                page.getSize(),
                page.getTotalElements(),
                page.getNumber() + 1,
                page.getTotalPages()
        );
        return new PagedResponse<>(page.getContent(), metadata);
    }

    @Data
    public static class Metadata {
        private int itemsPerPage;
        private long totalItems;
        private int currentPage;
        private int totalPages;

        public Metadata(int itemsPerPage, long totalItems, int currentPage, int totalPages) {
            this.itemsPerPage = itemsPerPage;
            this.totalItems = totalItems;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
        }


    }
}
