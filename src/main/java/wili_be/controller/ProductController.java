package wili_be.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wili_be.controller.status.ApiResponse;
import wili_be.entity.Member;
import wili_be.entity.Post;
import wili_be.security.JWT.JwtTokenProvider;
import wili_be.service.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.List;
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

    //상품 등록
    @PostMapping("/products/add")
    public ResponseEntity<ApiResponse> addProduct(
            @RequestParam("file") MultipartFile file,
            @RequestParam("productInfo") String productInfoJson,
            HttpServletRequest httpServletRequest) {
        String accessToken = jwtTokenProvider.resolveToken(httpServletRequest);

        if (accessToken == null) {
            throw new NotLoggedInException();
        }
        tokenService.validateAccessToken(accessToken);
        String snsId = jwtTokenProvider.getUsersnsId(accessToken);
        productService.addProduct(file, productInfoJson, snsId);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.success_post_add();
        return ResponseEntity.ok(apiResponse);
    }

    // user의 상품조회
    @GetMapping("/users/products")
    public ResponseEntity<?> getPostsByUser(HttpServletRequest httpRequest) throws IOException {
        // 1. 토큰 검증과 추출
        String accessToken = jwtTokenProvider.resolveToken(httpRequest);
        if (accessToken == null) {
            throw new NotLoggedInException();
        }
        tokenService.validateAccessToken(accessToken);

        try {
            // 2. SNS ID를 추출하고 사용자 관련 데이터 가져오기
            String snsId = jwtTokenProvider.getUsersnsId(accessToken);
            List<byte[]> images = productService.getImagesByMember(snsId);
            List<PostMainPageResponse> postList = productService.getPostByMember(snsId);

            // 3. 응답 생성
            Map<String, Object> response = new HashMap<>();
            List<String> imageJsonList = jsonService.changeByteListToJson(images);
            List<String> postJsonList = jsonService.changePostMainPageResponseDtoListToJson(postList);

            response.put("message", "제품 있음");
            response.put("images", imageJsonList);
            response.put("posts", postJsonList);

            ApiResponse apiResponse = new ApiResponse();
            apiResponse.success_post_add();

            return ResponseEntity.ok().body(response);
        } catch (NoSuchElementException e) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.fail_post_add();
            return ResponseEntity.ok(apiResponse);
        }
    }

    //상품 조회
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

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.success_post_lookup(response);

        String snsId = jwtTokenProvider.getUsersnsId(accessToken);
        if (productService.validateUserFromPostAndSnsId(snsId, PostId)) {
            return ResponseEntity.ok()
                    .header("isMyPost", String.valueOf(true))
                    .body(apiResponse);
        } else {
            return ResponseEntity.ok()
                    .header("isMyPost", String.valueOf(false))
                    .body(apiResponse);
        }
    }

    //상품 수정
    @Transactional
    @PatchMapping("/products/{PostId}")
        // http method가 다르면 uri는 겹쳐도 된다.
    ResponseEntity<ApiResponse> updateProduct(HttpServletRequest httpRequest, @PathVariable Long PostId, @RequestBody PostUpdateResponseDto postUpdateDto) {
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

            ApiResponse apiResponse = new ApiResponse();
            apiResponse.success_post_edit();
            return ResponseEntity.ok(apiResponse);
        }
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.failed_post_edit();
        throw new CustomException(HttpStatus.BAD_REQUEST,apiResponse);
    }

    //상품 삭제
    @Transactional
    @DeleteMapping("/products/{PostId}")
    ResponseEntity<ApiResponse> removeProduct(HttpServletRequest httpRequest, @PathVariable Long PostId) {
        String accessToken = jwtTokenProvider.resolveToken(httpRequest);

        if (accessToken == null) {
            throw new NotLoggedInException();
        }
        tokenService.validateAccessToken(accessToken);
        String snsId = jwtTokenProvider.getUsersnsId(accessToken);
        Post post = productService.getPostFromId(PostId);
        Member member = post.getMember();

        ApiResponse apiResponse = new ApiResponse();
        if (member.getSnsId().equals(snsId)) {
            productService.deletePostByPostId(PostId);
            amazonS3Service.deleteImageByKey(post.getThumbnailImageKey());
            amazonS3Service.deleteImageByKey(post.getImageKey());
            apiResponse.success_post_delete();
            return ResponseEntity.ok()
                    .body(apiResponse);
        } else {
            apiResponse.failed_post_delete();
            throw new CustomException(HttpStatus.BAD_REQUEST, apiResponse);
        }
    }

    //랜덤 피드
    @GetMapping("/random-feed")
    ResponseEntity<ApiResponse> randomFeed(HttpServletRequest httpRequest) throws IOException {
        String accessToken = jwtTokenProvider.resolveToken(httpRequest);
        if (accessToken == null) {
            throw new NotLoggedInException();
        }
        tokenService.validateAccessToken(accessToken);

        ApiResponse apiResponse = new ApiResponse();
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
            Map_response.put("images", image_json);
            Map_response.put("posts", product_json);

            apiResponse.success_random_feed(Map_response);
            return ResponseEntity.ok().body(apiResponse);
        } catch (NoSuchElementException e) {
            apiResponse.failed_random_feed();
            return ResponseEntity.ok(apiResponse);
        }
    }

    //검색 api
    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchPostOrMember(HttpServletRequest httpServletRequest, @RequestParam String query) throws IOException {
        String accessToken = jwtTokenProvider.resolveToken(httpServletRequest);
        if (accessToken == null) {
            throw new NotLoggedInException();
        }
        tokenService.validateAccessToken(accessToken);
        ApiResponse apiResponse = new ApiResponse();
        try {
            char firstLetter = query.charAt(0);
            if (firstLetter == '@') {
                Member member = memberService.findMemberByMemberName(query.substring(1));
                List<PostMainPageResponse> postList = productService.getPostByMember(member.getSnsId());
                List<byte[]> images = productService.getImagesByMember(member.getSnsId());

                Map<String, Object> response = new HashMap<>();
                List<String> image_json = jsonService.changeByteListToJson(images);
                List<String> post_json = jsonService.changePostMainPageResponseDtoListToJson(postList);

                response.put("images", image_json);
                response.put("posts", post_json);
                apiResponse.success_search_user(response);
                return ResponseEntity.ok().body(apiResponse);
            } else {
                SearchPageResponse response = productService.getPostResponseDtoFromProductName(query);
                List<PostMainPageResponse> productList = response.getProduct().get("product");
                List<String> imageKeys = response.getImageKey().get("image");
                List<byte[]> images = amazonS3Service.getImageBytesByKeys(imageKeys);

                List<String> product_json = jsonService.changePostMainPageResponseDtoListToJson(productList);
                List<String> image_json = jsonService.changeByteListToJson(images);

                Map<String, Object> Map_response = new HashMap<>();
                Map_response.put("images", image_json);
                Map_response.put("posts", product_json);
                apiResponse.success_search_product(Map_response);
                return ResponseEntity.ok().body(apiResponse);
            }
        } catch (NoSuchElementException e) {
            apiResponse.failed_search_product();
            return ResponseEntity.ok(apiResponse);
        } catch (UsernameNotFoundException e) {
            apiResponse.failed_search_user();
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            apiResponse.failed_search();
            return ResponseEntity.ok(apiResponse);
        }
    }
}
