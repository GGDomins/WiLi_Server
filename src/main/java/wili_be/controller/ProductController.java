package wili_be.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wili_be.controller.status.StatusCode;
import wili_be.entity.Member;
import wili_be.entity.Post;
import wili_be.security.JWT.JwtTokenProvider;
import wili_be.service.AmazonS3Service;
import wili_be.service.MemberService;
import wili_be.service.ProductService;
import wili_be.service.TokenService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.List;

//import static wili_be.dto.ImageDto.*;
import static wili_be.dto.MemberDto.*;
import static wili_be.dto.PostDto.*;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ProductController {
    private final AmazonS3Service amazonS3Service;
    private final ProductService productService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final MemberService memberService;
    private int StatusResult;

    @PostMapping("/products/add")
    public ResponseEntity<String> addProduct(@RequestParam("file") MultipartFile file, @RequestParam("productInfo") String productInfoJson, HttpServletRequest httpServletRequest) {
        String accessToken = jwtTokenProvider.resolveToken(httpServletRequest);

        if (accessToken == null) {
            return createUnauthorizedResponse("접근 토큰이 없습니다");
        }
        StatusResult = tokenService.validateAccessToken(accessToken);

        if (StatusResult == StatusCode.UNAUTHORIZED) {
            return createExpiredTokenResponse("접근 토큰이 만료되었습니다");
        }
        if (StatusResult != StatusCode.OK) {
            return createBadRequestResponse("잘못된 요청입니다");
        }
        String snsId = jwtTokenProvider.getUsersnsId(accessToken);
        productService.addProduct(file, productInfoJson, snsId);
        return ResponseEntity.ok("Product added successfully.");
    }

    @GetMapping("/users/products")
    ResponseEntity<?> getPostsByUser(HttpServletRequest httpRequest) throws IOException {
        String accessToken = jwtTokenProvider.resolveToken(httpRequest);

        if (accessToken == null) {
            return createUnauthorizedResponse("접근 토큰이 없습니다");
        }
        StatusResult = tokenService.validateAccessToken(accessToken);

        if (StatusResult == StatusCode.UNAUTHORIZED) {
            return createExpiredTokenResponse("접근 토큰이 만료되었습니다");
        }
        if (StatusResult != StatusCode.OK) {
            return createBadRequestResponse("잘못된 요청입니다");
        }
        String snsId = jwtTokenProvider.getUsersnsId(accessToken);
        List<byte[]> images = productService.getImagesByMember(snsId);
        List<PostMainPageResponse> postList = productService.getPostByMember(snsId);

        List<String> image_json = productService.changeBytesToJson(images);
        List<String> post_json = productService.changePostDtoToJson(postList);

        Map<String, Object> response = new HashMap<>();
        response.put("images", image_json);
        response.put("posts", post_json);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/products/{PostId}")
    ResponseEntity<?> getPostsById(HttpServletRequest httpRequest, @PathVariable Long PostId) throws IOException {
        String accessToken = jwtTokenProvider.resolveToken(httpRequest);

        if (accessToken == null) {
            return createUnauthorizedResponse("접근 토큰이 없습니다");
        }
        StatusResult = tokenService.validateAccessToken(accessToken);

        if (StatusResult == StatusCode.UNAUTHORIZED) {
            return createExpiredTokenResponse("접근 토큰이 만료되었습니다");
        }
        if (StatusResult != StatusCode.OK) {
            return createBadRequestResponse("잘못된 요청입니다");
        }
        PostResponseDto post = productService.getPostResponseDtoFromId(PostId);
        byte[] image = productService.getImageByMember(post.getImageKey());

        String JsonImage = productService.changeByteToJson(image);
        String JsonPost = productService.changePostToJson(post);
        Map<String, Object> response = new HashMap<>();
        response.put("image", JsonImage);
        response.put("post", JsonPost);

        String snsId = jwtTokenProvider.getUsersnsId(accessToken);
        if (productService.validateUserFromPostAndSnsId(snsId, PostId)) {
            return ResponseEntity.ok().body(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }
    }

    @Transactional
    @PatchMapping("/products/{PostId}")
        // http method가 다르면 uri는 겹쳐도 된다.
    ResponseEntity<String> updateProduct(HttpServletRequest httpRequest, @PathVariable Long PostId, @RequestBody PostUpdateResponseDto postUpdateDto) {
        String accessToken = jwtTokenProvider.resolveToken(httpRequest);
        if (accessToken == null) {
            return createUnauthorizedResponse("접근 토큰이 없습니다");
        }
        StatusResult = tokenService.validateAccessToken(accessToken);
        if (StatusResult == StatusCode.UNAUTHORIZED) {
            return createExpiredTokenResponse("접근 토큰이 만료되었습니다");
        }
        if (StatusResult != StatusCode.OK) {
            return createBadRequestResponse("잘못된 요청입니다");
        }
        String snsId = jwtTokenProvider.getUsersnsId(accessToken);
        Post post = productService.getPostFromId(PostId);
        Member member = post.getMember();
        if (member.getSnsId().equals(snsId)) {
            PostResponseDto updatePost = productService.updatePost(PostId, postUpdateDto);
            String jsonPost = productService.changePostToJson(updatePost);
            return ResponseEntity.ok(jsonPost);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("다른 사용자가 product를 수정하려고 시도합니다.");

    }

    @Transactional
    @DeleteMapping("/products/{PostId}")
    ResponseEntity<String> removeProduct(HttpServletRequest httpRequest, @PathVariable Long PostId) {
        String accessToken = jwtTokenProvider.resolveToken(httpRequest);

        if (accessToken == null) {
            return createUnauthorizedResponse("접근 토큰이 없습니다");
        }

        StatusResult = tokenService.validateAccessToken(accessToken);

        if (StatusResult == StatusCode.UNAUTHORIZED) {
            return createExpiredTokenResponse("접근 토큰이 만료되었습니다");
        }

        if (StatusResult != StatusCode.OK) {
            return createBadRequestResponse("잘못된 요청입니다");
        }
        try {
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
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("다른 사용자가 product를 수정하려고 시도합니다.");
            }

        } catch (NullPointerException e) {
            return ResponseEntity.ok()
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.toString());
        }
    }
    @GetMapping("/random-feed")
    ResponseEntity<?> randomFeed(HttpServletRequest httpRequest) throws IOException {
        String accessToken = jwtTokenProvider.resolveToken(httpRequest);

        if (accessToken == null) {
            return createUnauthorizedResponse("접근 토큰이 없습니다");
        }
        StatusResult = tokenService.validateAccessToken(accessToken);

        if (StatusResult == StatusCode.UNAUTHORIZED) {
            return createExpiredTokenResponse("접근 토큰이 만료되었습니다");
        }
        if (StatusResult != StatusCode.OK) {
            return createBadRequestResponse("잘못된 요청입니다");
        }
        try {
            String snsId = jwtTokenProvider.getUsersnsId(accessToken);
            Optional<Member> member = memberService.findMemberById(snsId);
            if (member.isPresent()) {
                List<PostMainPageResponse> posts = productService.randomFeed(member.get());
                List<String> post_json = productService.changePostDtoToJson(posts);
                return ResponseEntity.ok()
                        .body(post_json);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("member를 찾을 수 없습니다.");
            }
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchPostOrMember(HttpServletRequest httpServletRequest,@RequestParam String query) {
        String accessToken = jwtTokenProvider.resolveToken(httpServletRequest);

        if (accessToken == null) {
            return createUnauthorizedResponse("접근 토큰이 없습니다");
        }
        StatusResult = tokenService.validateAccessToken(accessToken);

        if (StatusResult == StatusCode.UNAUTHORIZED) {
            return createExpiredTokenResponse("접근 토큰이 만료되었습니다");
        }
        if (StatusResult != StatusCode.OK) {
            return createBadRequestResponse("잘못된 요청입니다");
        }
        try {
            char firstLetter = query.charAt(0);
            if (firstLetter == '@') {
                MemberResponseDto memberResponseDto = memberService.findMemberByUserName(query.substring(1));
                String memberResponseJson = memberService.changeMemberResponseDtoToJson(memberResponseDto);
                return ResponseEntity.ok().body(memberResponseJson);
            }
            else {
                SearchPageResponse response = productService.getPostResponseDtoFromProductName(query);
                List<PostMainPageResponse> productList = response.getProduct().get("product");
                List<String> imageKeys = response.getImageKey().get("image");
                List<byte[]> images = amazonS3Service.getImageBytesByKeys(imageKeys);

                List<String> product_json = productService.changePostDtoToJson(productList);
                List<String> image_json = productService.changeBytesToJson(images);
                Map<String, Object> Map_response = new HashMap<>();
                Map_response.put("images", image_json);
                Map_response.put("posts", product_json);
                return ResponseEntity.ok().body(Map_response);
            }

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private ResponseEntity<String> createUnauthorizedResponse(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("WWW-Authenticate", "not-logged-in")
                .body(message);
    }

    private ResponseEntity<String> createExpiredTokenResponse(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("WWW-Authenticate", "Bearer error=\"invalid_token\"")
                .body(message);
    }

    private ResponseEntity<String> createBadRequestResponse(String message) {
        return ResponseEntity.badRequest().body(message);
    }
}
