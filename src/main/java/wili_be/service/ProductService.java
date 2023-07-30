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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    public String getImagesByMember(String snsId) throws IOException {
        List<String> imageKeyList = getImagesKeysByMember(snsId);
        List<byte[]> images = amazonS3Service.getImageBytesByKeys(imageKeyList);
        if (images.isEmpty()) {
            return null;
        }
        return images.toString();

    }

    public String getPostByMember(String snsId) {
        List<Post> postList = productRepository.findPostBySnsId(snsId);
        return postList.toString();
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
