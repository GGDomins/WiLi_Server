package wili_be.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wili_be.entity.Member;
import wili_be.entity.Post;
import wili_be.security.JWT.JwtTokenProvider;
import wili_be.service.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.List;

//import static wili_be.dto.ImageDto.*;
import static wili_be.dto.MemberDto.*;
import static wili_be.dto.PostDto.*;
import static wili_be.exception.CustomExceptions.*;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ProductController {
    private final AmazonS3Service amazonS3Service;
    private final ProductService productService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final MemberService memberService;
    private final JsonService jsonService;

    @PostMapping("/products/add")
    public ResponseEntity<String> addProduct(
            @RequestParam("file") MultipartFile file,
            @RequestParam("productInfo") String productInfoJson, HttpServletRequest httpServletRequest) {
        String accessToken = jwtTokenProvider.resolveToken(httpServletRequest);

        if (accessToken == null) {
            throw new NotLoggedInException();
        }
        tokenService.validateAccessToken(accessToken);
        String snsId = jwtTokenProvider.getUsersnsId(accessToken);
        productService.addProduct(file, productInfoJson, snsId);
        return ResponseEntity.ok("Product 저장 성공.");
    }

    @GetMapping("/users/products")
    ResponseEntity<?> getPostsByUser(HttpServletRequest httpRequest) throws IOException {
        String accessToken = jwtTokenProvider.resolveToken(httpRequest);
        try {
            if (accessToken == null) {
                throw new NotLoggedInException();
            }
            tokenService.validateAccessToken(accessToken);
            String snsId = jwtTokenProvider.getUsersnsId(accessToken);
            List<byte[]> images = productService.getImagesByMember(snsId);
            List<PostMainPageResponse> postList = productService.getPostByMember(snsId);

            Map<String, Object> response = new HashMap<>();
            List<String> image_json = jsonService.changeByteListToJson(images);
            List<String> post_json = jsonService.changePostMainPageResponseDtoListToJson(postList);
            response.put("message", "제품 있음");
            response.put("images", image_json);
            response.put("posts", post_json);
            return ResponseEntity.ok().body(response);
        } catch (NoSuchElementException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "제품 없음");
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/products/{PostId}")
    ResponseEntity<?> getPostsById(HttpServletRequest httpRequest, @PathVariable Long PostId) throws IOException {
        String accessToken = jwtTokenProvider.resolveToken(httpRequest);

        if (accessToken == null) {
            throw new NotLoggedInException();
        }
        tokenService.validateAccessToken(accessToken);
        PostResponseDto post = productService.getPostResponseDtoFromId(PostId);
        byte[] image = productService.getImageByMember(post.getImageKey());

        String JsonImage = jsonService.changeByteToJson(image);
        String JsonPost = jsonService.changePostResponseDtoToJson(post);
        Map<String, Object> response = new HashMap<>();
        response.put("image", JsonImage);
        response.put("post", JsonPost);

        String snsId = jwtTokenProvider.getUsersnsId(accessToken);
        if (productService.validateUserFromPostAndSnsId(snsId, PostId)) {
            return ResponseEntity.ok()
                    .header("isMyPost", String.valueOf(true))
                    .body(response);
        } else {
            return ResponseEntity.ok()
                    .header("isMyPost", String.valueOf(false))
                    .body(response);
        }
    }

    @Transactional
    @PatchMapping("/products/{PostId}")
        // http method가 다르면 uri는 겹쳐도 된다.
    ResponseEntity<String> updateProduct(HttpServletRequest httpRequest, @PathVariable Long PostId, @RequestBody PostUpdateResponseDto postUpdateDto) {
        String accessToken = jwtTokenProvider.resolveToken(httpRequest);

        if (accessToken == null) {
            throw new NotLoggedInException();
        }
        tokenService.validateAccessToken(accessToken);
        String snsId = jwtTokenProvider.getUsersnsId(accessToken);
        Post post = productService.getPostFromId(PostId);
        Member member = post.getMember();
        if (member.getSnsId().equals(snsId)) {
            PostResponseDto updatePost = productService.updatePost(PostId, postUpdateDto);
            String jsonPost = jsonService.changePostResponseDtoToJson(updatePost);
            return ResponseEntity.ok(jsonPost);
        }
        throw new CustomException(HttpStatus.BAD_REQUEST, "다른 사용자가 product를 수정하려고 시도합니다.");
    }

    @Transactional
    @DeleteMapping("/products/{PostId}")
    ResponseEntity<String> removeProduct(HttpServletRequest httpRequest, @PathVariable Long PostId) {
        String accessToken = jwtTokenProvider.resolveToken(httpRequest);

        if (accessToken == null) {
            throw new NotLoggedInException();
        }
        tokenService.validateAccessToken(accessToken);
        String snsId = jwtTokenProvider.getUsersnsId(accessToken);
        Post post = productService.getPostFromId(PostId);
        Member member = post.getMember();

        if (member.getSnsId().equals(snsId)) {
            productService.deletePostByPostId(PostId);
            amazonS3Service.deleteImageByKey(post.getThumbnailImageKey());
            amazonS3Service.deleteImageByKey(post.getImageKey());
            return ResponseEntity.ok()
                    .body("delete 성공!");
        } else {
            throw new CustomException(HttpStatus.BAD_REQUEST, "다른 사용자가 product를 수정하려고 시도합니다.");
        }
    }

    @GetMapping("/random-feed")
    ResponseEntity<?> randomFeed(HttpServletRequest httpRequest) throws IOException {
        String accessToken = jwtTokenProvider.resolveToken(httpRequest);

        if (accessToken == null) {
            throw new NotLoggedInException();
        }
        tokenService.validateAccessToken(accessToken);
        try {

            String snsId = jwtTokenProvider.getUsersnsId(accessToken);
            Member member = memberService.findMemberById(snsId).orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, "member가 존재하지 않습니다."));
            RandomFeedDto response = productService.randomFeed(member);
            List<PostMainPageResponse> posts = response.getPageResponses();
            List<String> imageKeysList = response.getImageKeyList();
            List<byte[]> imageList = amazonS3Service.getImageBytesByKeys(imageKeysList);

            List<String> image_json = jsonService.changeByteListToJson(imageList);
            List<String> product_json = jsonService.changePostMainPageResponseDtoListToJson(posts);

            Map<String, Object> Map_response = new HashMap<>();
            Map_response.put("message", "제품 있음");
            Map_response.put("images", image_json);
            Map_response.put("posts", product_json);
            return ResponseEntity.ok().body(Map_response);
        } catch (NoSuchElementException e) {
            Map<String, Object> Map_response = new HashMap<>();
            Map_response.put("message", "제품 없음");
            return ResponseEntity.ok(Map_response);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchPostOrMember(HttpServletRequest httpServletRequest, @RequestParam String query) throws IOException {
        String accessToken = jwtTokenProvider.resolveToken(httpServletRequest);

        if (accessToken == null) {
            throw new NotLoggedInException();
        }
        tokenService.validateAccessToken(accessToken);
        try {
            char firstLetter = query.charAt(0);
            if (firstLetter == '@') {
                Member member = memberService.findMemberByMemberName(query.substring(1));
                List<PostMainPageResponse> postList = productService.getPostByMember(member.getSnsId());
                List<byte[]> images = productService.getImagesByMember(member.getSnsId());

                Map<String, Object> response = new HashMap<>();
                List<String> image_json = jsonService.changeByteListToJson(images);
                List<String> post_json = jsonService.changePostMainPageResponseDtoListToJson(postList);

                response.put("message", "제품 있음");
                response.put("images", image_json);
                response.put("posts", post_json);
                return ResponseEntity.ok().body(response);
            } else {
                SearchPageResponse response = productService.getPostResponseDtoFromProductName(query);
                List<PostMainPageResponse> productList = response.getProduct().get("product");
                List<String> imageKeys = response.getImageKey().get("image");
                List<byte[]> images = amazonS3Service.getImageBytesByKeys(imageKeys);

                List<String> product_json = jsonService.changePostMainPageResponseDtoListToJson(productList);
                List<String> image_json = jsonService.changeByteListToJson(images);

                Map<String, Object> Map_response = new HashMap<>();
                Map_response.put("message", "제품 있음");
                Map_response.put("images", image_json);
                Map_response.put("posts", product_json);
                return ResponseEntity.ok().body(Map_response);
            }
        } catch (NoSuchElementException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "제품 없음");
            return ResponseEntity.ok(response);
        } catch (UsernameNotFoundException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "유저 없음");
            return ResponseEntity.ok(response);
        }
    }
}
