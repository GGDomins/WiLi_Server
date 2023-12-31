package wili_be.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import wili_be.entity.Member;
import wili_be.entity.Post;
import wili_be.exception.CustomExceptions;
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
import static wili_be.exception.CustomExceptions.*;


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

            byte[] thumbnailImage = createThumbnail(file.getBytes(), 480, 480);

            String key = amazonS3Service.putObject(file.getBytes(), "originalImage" + file.getOriginalFilename());
            String thumbnailImagekey = amazonS3Service.putObject(thumbnailImage, "thumbnailImage" + file.getOriginalFilename());

            productInfo.setImageKey(key);
            productInfo.setThumbnailImageKey(thumbnailImagekey);
            savePost(productInfo, snsId);
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
            throw new CustomException(HttpStatus.BAD_REQUEST, "postResponseDto의 값이 null입니다.");
        }
        return postResponseDto;
    }

    @Override
    public SearchPageResponse getPostResponseDtoFromProductName(String productName) {
        List<Post> postList = productRepository.findPostsByProductName("%" + productName + "%");
        List<String> imageKeyList = new ArrayList<>();

        for (Post post : postList) {
            imageKeyList.add(post.getThumbnailImageKey());
        }
        if (postList.isEmpty()) {
            throw new NoSuchElementException("해당 키워드에 맞는 제품이 없습니다.");
        } else {
            List<PostMainPageResponse> postResponseDtoList = postList.stream()
                    .map(PostMainPageResponse::new)
                    .collect(Collectors.toList());

            SearchPageResponse response = new SearchPageResponse();
            Map<String, List<PostMainPageResponse>> product = new HashMap<>();
            Map<String, List<String>> imageKey = new HashMap<>();

            product.put("product", postResponseDtoList);
            imageKey.put("image", imageKeyList);

            response.setProduct(product);
            response.setImageKey(imageKey);
            return response;
        }
    }

    @Override
    public List<PostMainPageResponse> getPostResponseDtoFromBrandName(String brandName) {
        List<Post> postList = productRepository.findPostsByBrandName(brandName);
        if (postList.isEmpty()) {
            throw new NoSuchElementException("해당 키워드에 맞는 제품이 없습니다.");
        } else {
            List<PostMainPageResponse> postResponseDtoList = postList.stream()
                    .map(PostMainPageResponse::new)
                    .collect(Collectors.toList());
            return postResponseDtoList;
        }
    }

    @Override
    public Post getPostFromId(Long id) {
        Post post = productRepository.findPostById(id);
        return post;
    }

    @Override
    public Boolean validateUserFromPostAndSnsId(String snsId, Long postId) {
        Post post = getPostFromId(postId);
        Member member = post.getMember();
        if (Objects.equals(member.getSnsId(), snsId)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public PostResponseDto updatePost(Long postId, PostUpdateResponseDto postUpdateDto) {
        Post post = productRepository.findPostById(postId);

        if (post == null) {
            // 요청한 postId에 해당하는 Post가 없으면 예외 처리
            throw new CustomException(HttpStatus.NOT_FOUND, "해당하는 게시물을 찾을 수 없습니다.");
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
    public void deletePostByPostId(Long PostId) {
        Post post = getPostFromId(PostId);
        productRepository.delete(post);
    }

    public RandomFeedDto randomFeed(Member member) {
        RandomFeedDto randomFeedDto = new RandomFeedDto();
        List<String> imageKeyList = new ArrayList<>();
        List<String> favoriteCategories = Arrays.asList(member.getFavorites().split(","));
        List<Post> posts = productRepository.findPostsMatchingFavoriteCategories(favoriteCategories);
        if (posts.isEmpty()) {
            throw new NoSuchElementException("사용자의 카테고리에 맞는 피드가 없습니다.");
        } else {
            List<PostMainPageResponse> postResponseDtoList = posts.stream()
                    .map(PostMainPageResponse::new)
                    .collect(Collectors.toList());
            for (PostMainPageResponse response : postResponseDtoList) {
                imageKeyList.add(response.getImageKey());
            }
            randomFeedDto.setPageResponses(postResponseDtoList);
            randomFeedDto.setImageKeyList(imageKeyList);
            return randomFeedDto;
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
                .registrationDate(productInfo.getDate())
                .member(member)
                .build();
        productRepository.save(post);
    }
}
