package wili_be.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import wili_be.dto.PostDto;
import wili_be.entity.Member;
import wili_be.entity.Post;
import wili_be.repository.MemberRepository;
import wili_be.repository.ProductRepository;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
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

    public List<String> getImagesByMember(String snsId) throws IOException {
        List<String> imageKeyList = getImagesKeysByMember(snsId);
        log.info(imageKeyList.toString());
        log.info("imageInfo");
        try {
            List<byte[]> images = amazonS3Service.getImageBytesByKeys(imageKeyList);
            if (images.isEmpty()) {
                return null;
            }
            List<String> imageStringList = new ArrayList<>();
            for (byte[] imageBytes : images) {
                // 바이트 배열을 Base64로 인코딩하여 문자열로 변환합니다.
                String encodedImage = java.util.Base64.getEncoder().encodeToString(imageBytes);
                imageStringList.add(encodedImage);
            }
            return imageStringList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    public List<String> getPostByMember(String snsId) {
        List<Post> postList = productRepository.findPostBySnsId(snsId);
        log.info(postList.toString());
        log.info("postInfo");
        List<String> postJsonList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // postList의 각 Post 객체를 JSON 문자열로 변환하여 postJsonList에 추가합니다.
            for (Post post : postList) {
                String json = objectMapper.writeValueAsString(post);
                postJsonList.add(json);
            }
            log.info(postJsonList.toString());
            log.info("postjsonList");
            return postJsonList;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
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
