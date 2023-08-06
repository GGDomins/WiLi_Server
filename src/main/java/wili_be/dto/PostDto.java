package wili_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.bytebuddy.utility.nullability.NeverNull;
import wili_be.entity.Post;

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


        public PostResponseDto(Post post) {
            this.id = post.getId();
            this.brandName = post.getBrandName();
            this.productName = post.getProductName();
            this.category = post.getCategory();
            this.productPrice = post.getProductPrice();
            this.description = post.getDescription();
            this.link = post.getLink();
            this.imageKey = post.getImageKey();
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

        public PostMainPageResponse(Post post) {
            this.id = post.getId();
            this.brandName = post.getBrandName();
            this.productName = post.getProductName();
            this.imageKey = post.getImageKey();
            this.thumbnailImageKey = post.getThumbnailImageKey();
        }
    }
}
