package wili_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.bytebuddy.utility.nullability.NeverNull;
import wili_be.entity.Post;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostDto {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostInfoDto {
        private String brandName;
        private String productName;
        private String category;
        private String productPrice;
        private String description;
        private String link;
        private String imageKey;
        private String thumbnailImageKey;
        private String date;


        public Post to_Entity() {
            return Post.builder()
                    .brandName(brandName)
                    .productName(productName)
                    .category(category)
                    .productPrice(productPrice)
                    .description(description)
                    .link(link)
                    .imageKey(imageKey)
                    .thumbnailImageKey(thumbnailImageKey)
                    .registrationDate(date)
                    .build();
        }
    }

    @Data
    public static class PostIdDto {
        Long id;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostUpdateResponseDto {
        private String brandName;
        private String productName;
        private String category;
        private String productPrice;
        private String description;
        private String link;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostResponseDto {
        private Long id;
        private String brandName;
        private String productName;
        private String category;
        private String productPrice;
        private String description;
        private String link;
        private String imageKey;
        private String thumbnailImageKey;
        private String date;


        public PostResponseDto(Post post) {
            this.id = post.getId();
            this.brandName = post.getBrandName();
            this.productName = post.getProductName();
            this.category = post.getCategory();
            this.productPrice = post.getProductPrice();
            this.description = post.getDescription();
            this.link = post.getLink();
            this.imageKey = post.getImageKey();
            this.date = post.getRegistrationDate();
            thumbnailImageKey = post.getThumbnailImageKey();
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostMainPageResponse {
        private Long id;
        private String brandName;
        private String productName;
        private String imageKey;
        private String thumbnailImageKey;
        private String category;
        private String date;


        public PostMainPageResponse(Post post) {
            this.id = post.getId();
            this.brandName = post.getBrandName();
            this.productName = post.getProductName();
            this.category = post.getCategory();
            this.imageKey = post.getImageKey();
            this.thumbnailImageKey = post.getThumbnailImageKey();
            this.date = post.getRegistrationDate();
        }
    }
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearchPageResponse {

        private Map<String, List<PostMainPageResponse>> product = new HashMap<>();
        private Map<String, List<String>> imageKey = new HashMap<>();

    }
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RandomFeedDto {

        private List<PostMainPageResponse> pageResponses;
        private List<String> imageKeyList;

    }

}
