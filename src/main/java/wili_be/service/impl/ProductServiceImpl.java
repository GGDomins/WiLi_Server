package wili_be.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.StrAlgoArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import wili_be.entity.Member;
import wili_be.entity.Post;
import wili_be.repository.ProductRepository;
import wili_be.service.AmazonS3Service;
import wili_be.service.MemberService;
import wili_be.service.ProductService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static wili_be.dto.PostDto.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final AmazonS3Service amazonS3Service;
    private final ProductRepository productRepository;
    private final MemberService memberService;

    @Override
    public void addProduct(MultipartFile file, String productInfoJson, String snsId) {
        ObjectMapper objectMapper = new ObjectMapper();
        PostInfoDto productInfo = null;
        try {
            productInfo = objectMapper.readValue(productInfoJson, PostInfoDto.class);

            byte[] thumbnailImage = createThumbnail(file.getBytes(), 540, 540);

            String key = amazonS3Service.putObject(file.getBytes(), "originalImage" + file.getOriginalFilename());
            String thumbnailImagekey = amazonS3Service.putObject(thumbnailImage,"thumbnailImage" + file.getOriginalFilename());

            productInfo.setImageKey(key);
            productInfo.setThumbnailImageKey(thumbnailImagekey);
            savePost(productInfo, snsId);
        } catch (UsernameNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    // 썸네일 생성 메서드
    private byte[] createThumbnail(byte[] imageBytes, int width, int height) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(new ByteArrayInputStream(imageBytes))
                .size(width, height)
                .toOutputStream(outputStream);
        return outputStream.toByteArray();
    }

    @Override
    public List<String> getThumbnailImagesKeysByMember(String snsId) {
        List<String> ThumbnailimageList = productRepository.findThumbnailImageKeysBysnsId(snsId);
        if (ThumbnailimageList.isEmpty()) {
            throw new NoSuchElementException("이미지가 존재하지 않습니다.");
        }
        return ThumbnailimageList;
    }
    @Override
    public List<String> getImagesKeysByMember(String snsId) {
        List<String> imageList = productRepository.findImageKeysBysnsId(snsId);
        if (imageList.isEmpty()) {
            throw new NoSuchElementException("이미지가 존재하지 않습니다.");
        }
        return imageList;
    }

    @Override
    public List<byte[]> getImagesByMember(String snsId) throws IOException {
        List<String> imageKeyList = getThumbnailImagesKeysByMember(snsId);

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

    @Override
    public byte[] getImageByMember(String imageKey) throws IOException {
        try {
            byte[] image = amazonS3Service.getImageBytesByKey(imageKey);
            if (image == null) {
                return null;
            }
            return image;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("멤버로부터 이미지를 가져오는데 실패했습니다.: " + imageKey, e);

        }
    }

    @Override
    public List<PostMainPageResponse> getPostByMember(String snsId) {
        List<Post> postList = productRepository.findPostBySnsId(snsId);
        List<PostMainPageResponse> postResponseDtoList = postList.stream()
                .map(PostMainPageResponse::new)
                .collect(Collectors.toList());

        return postResponseDtoList;
    }

    @Override
    public PostResponseDto getPostResponseDtoFromId(Long id) {
        Post post = productRepository.findPostById(id);
        PostResponseDto postResponseDto = new PostResponseDto(post);
        if (postResponseDto == null) {
            log.info("post의 값이 null입니다.");
            return null;
        }
        return postResponseDto;
    }

    @Override
    public Post getPostFromId(Long id) {
        Post post = productRepository.findPostById(id);
        return post;
    }

    @Override
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

    @Override
    public String changePostToJson(PostResponseDto post) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String postJson = objectMapper.writeValueAsString(post);
            return postJson;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String changeByteToJson(byte[] bytes) {
        String encodedImage = java.util.Base64.getEncoder().encodeToString(bytes);
        return encodedImage;
    }

    @Override
    public List<String> changeBytesToJson(List<byte[]> bytes) {
        List<String> jsonList = new ArrayList<>();
        for (byte[] imageBytes : bytes) {
            // 바이트 배열을 Base64로 인코딩하여 문자열로 변환합니다.
            String encodedImage = java.util.Base64.getEncoder().encodeToString(imageBytes);
            jsonList.add(encodedImage);
        }
        return jsonList;
    }

    @Override
    public List<String> changePostDtoToJson(List<PostMainPageResponse> postResponseDtoList) {
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

    @Override
    public void deletePostByPostId(Long PostId) {
        try {
            Post post = getPostFromId(PostId);
            productRepository.delete(post);
        } catch (NullPointerException e) {
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void savePost(PostInfoDto productInfo, String snsId) {
        Optional<Member> member_op = memberService.findMemberById(snsId);
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
                .thumbnailImageKey(productInfo.getThumbnailImageKey())
                .member(member)
                .build();
        productRepository.save(post);
    }
}
