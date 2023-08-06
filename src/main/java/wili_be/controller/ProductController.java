package wili_be.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wili_be.controller.status.StatusCode;
import wili_be.entity.Member;
import wili_be.entity.Post;
import wili_be.security.JWT.JwtTokenProvider;
import wili_be.service.AmazonS3Service;
import wili_be.service.ProductService;
import wili_be.service.TokenService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static wili_be.dto.ImageDto.*;
import static wili_be.dto.PostDto.*;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ProductController {
    private final AmazonS3Service amazonS3Service;
    private final ProductService productService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private int StatusResult;


    @PostMapping("/products/upload")
    public ResponseEntity<String> uploadImage(@RequestBody MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            String key = amazonS3Service.putObject(file, fileName);
            return ResponseEntity.status(HttpStatus.OK).body("key: " + key);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 업로드에 실패했습니다." + e.getMessage());
        }
    }

    // 이미지 조회 엔드포인트
    @PostMapping("/products/images-test")
    public ResponseEntity<byte[]> getImageByKey(@RequestBody ImageRequestDto requestDto) {
        String key = requestDto.getKey();
        try {
            byte[] imageBytes = amazonS3Service.getImageBytesByKey(key);

            if (imageBytes != null) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_JPEG);

                return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

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
        return ResponseEntity.ok().body(response);
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
