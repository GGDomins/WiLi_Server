package wili_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostUpdateDto {
    private String brandName;
    private String productName;
    private String category;
    private String productPrice;
    private String description;
    private String link;
}
