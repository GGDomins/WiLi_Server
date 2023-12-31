package wili_be.service;

import org.springframework.web.multipart.MultipartFile;
import wili_be.dto.PostDto;
import wili_be.entity.Member;
import wili_be.entity.Post;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static wili_be.dto.PostDto.*;

public interface ProductService {
    void addProduct(MultipartFile file, String productInfoJson, String snsId);

    List<String> getImagesKeysByMember(String snsId);

    List<String> getThumbnailImagesKeysByMember(String snsId);

    List<byte[]> getImagesByMember(String snsId) throws IOException;

    byte[] getImageByMember(String imageKey) throws IOException;

    List<PostMainPageResponse> getPostByMember(String snsId);

    PostResponseDto getPostResponseDtoFromId(Long id);

    SearchPageResponse getPostResponseDtoFromProductName(String productName);
    List<PostMainPageResponse> getPostResponseDtoFromBrandName(String brandName);

    Post getPostFromId(Long id);

    Boolean validateUserFromPostAndSnsId(String snsId, Long postId);

    PostResponseDto updatePost(Long postId, PostUpdateResponseDto postUpdateDto);

    RandomFeedDto randomFeed(Member member);

    void deletePostByPostId(Long PostId);
}
