package wili_be.dto;

import lombok.*;
import wili_be.entity.Post;

import javax.persistence.Lob;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostDto {
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
