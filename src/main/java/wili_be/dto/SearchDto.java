package wili_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class SearchDto {
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearchRequestDto {
        private String type;
        private String keyword;
    }
}
