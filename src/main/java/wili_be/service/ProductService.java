package wili_be.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.StrAlgoArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import wili_be.dto.PostInfoDto;
import wili_be.entity.Member;
import wili_be.entity.Post;
import wili_be.repository.ProductRepository;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static wili_be.dto.PostInfoDto.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {
    private final AmazonS3Service amazonS3Service;
    private final ProductRepository productRepository;
    private final MemberService memberService;

    public void addProduct(MultipartFile file, String productInfoJson, String snsId) {
        ObjectMapper objectMapper = new ObjectMapper();
        PostDto productInfo = null;
        try {
            productInfo = objectMapper.readValue(productInfoJson, PostDto.class);

            String key = amazonS3Service.putObject(file, file.getOriginalFilename());
            productInfo.setImageKey(key);
            savePost(productInfo,snsId);
        }
        catch (UsernameNotFoundException e) {
        }
        catch (JsonProcessingException e) {
        }
    }

    public List<String> getImagesKeysByMember(String snsId) {
        List<String> imageList =  productRepository.findImageKeysBysnsId(snsId);
        if (imageList.isEmpty()) {
            return null;
        }
            return imageList;
    }

    public List<byte[]> getImagesByMember(String snsId) throws IOException {
        List<String> imageKeyList = getImagesKeysByMember(snsId);
        log.info(imageKeyList.toString());
        log.info("imageInfo");
        try {
            List<byte[]> images = amazonS3Service.getImageBytesByKeys(imageKeyList);
            if (images.isEmpty()) {
                return null;
            }
            return images;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    public List<PostResponseDto> getPostByMember(String snsId) {
        List<Post> postList = productRepository.findPostBySnsId(snsId);
        List<PostResponseDto> postResponseDtoListt = postList.stream().map(PostResponseDto::new).collect(Collectors.toList());

        return postResponseDtoListt;
    }

    public PostResponseDto getPostFromId(Long id) {
        Post post = productRepository.findPostById(id);
        PostResponseDto postResponseDto = new PostResponseDto(post);
        if (postResponseDto == null) {
            log.info("post의 값이 null입니다.");
            return null;
        }
        return postResponseDto;
    }

    public PostResponseDto updatePost(Long postId, PostUpdateResponseDto postUpdateDto) {
        Post post = productRepository.findPostById(postId);

        if (post == null) {
            // 요청한 postId에 해당하는 Post가 없으면 예외 처리
            throw new NoSuchElementException("해당하는 게시물을 찾을 수 없습니다.");
        }
        // 요청으로 받은 필드들로 업데이트
        if (postUpdateDto.getBrandName() != null) {
            post.setBrandName(postUpdateDto.getBrandName());
        }
        if (postUpdateDto.getProductName() != null) {
            post.setProductName(postUpdateDto.getProductName());
        }
        if (postUpdateDto.getCategory() != null) {
            post.setCategory(postUpdateDto.getCategory());
        }
        if (postUpdateDto.getProductPrice() != null) {
            post.setProductPrice(postUpdateDto.getProductPrice());
        }
        if (postUpdateDto.getDescription() != null) {
            post.setDescription(postUpdateDto.getDescription());
        }
        if (postUpdateDto.getLink() != null) {
            post.setLink(postUpdateDto.getLink());
        }
        productRepository.save(post);
        PostResponseDto postResponseDto = new PostResponseDto(post);
        // 업데이트된 게시물 저장
        return postResponseDto;
    }

    public String changeToJson(PostResponseDto post) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String postJson = objectMapper.writeValueAsString(post);
            return postJson;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String> changeByteToJson(List<byte[]> bytes) {
        List<String> jsonList = new ArrayList<>();
        for (byte[] imageBytes : bytes) {
            // 바이트 배열을 Base64로 인코딩하여 문자열로 변환합니다.
            String encodedImage = java.util.Base64.getEncoder().encodeToString(imageBytes);
            jsonList.add(encodedImage);
        }
        return jsonList;
    }

    public List<String> changePostDtoToJson(List<PostResponseDto> postResponseDtoList) {
        List<String> postJsonList = postResponseDtoList.stream()
                .map(postResponseDto -> {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        return objectMapper.writeValueAsString(postResponseDto);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(json -> json != null)
                .collect(Collectors.toList());
        return postJsonList;
    }

    private void savePost(PostDto productInfo, String snsId) {
        Optional<Member> member_op = memberService.findUserBySnsId(snsId);
        Member member = member_op.get();
        Post post = Post.builder()
                .member(member)
                .brandName(productInfo.getBrandName())
                .productName(productInfo.getProductName())
                .category(productInfo.getCategory())
                .productPrice(productInfo.getProductPrice())
                .description(productInfo.getDescription())
                .link(productInfo.getLink())
                .imageKey(productInfo.getImageKey())
                .member(member)
                .build();
        productRepository.save(post);
    }
}
