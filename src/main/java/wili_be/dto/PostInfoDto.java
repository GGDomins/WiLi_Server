package wili_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.bytebuddy.utility.nullability.NeverNull;
import wili_be.entity.Post;

public class PostInfoDto {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostDto {
        private String brandName;
        private String productName;
        private String category;
        private String productPrice;
        private String description;
        private String link;
        private String imageKey;

        public Post to_Entity() {
            return Post.builder()
                    .brandName(brandName)
                    .productName(productName)
                    .category(category)
                    .productPrice(productPrice)
                    .description(description)
                    .link(link)
                    .imageKey(imageKey)
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

        public PostResponseDto(Post post) {
            this.id = post.getId();
            this.brandName = post.getBrandName();
            this.productName = post.getProductName();
            this.category = post.getCategory();
            this.productPrice = post.getProductPrice();
            this.description = post.getDescription();
            this.link = post.getLink();
            this.imageKey = post.getImageKey();
        }
    }
}
