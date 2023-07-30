package wili_be.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wili_be.controller.status.StatusCode;
import wili_be.dto.ImageRequestDto;
import wili_be.entity.Post;
import wili_be.security.JWT.JwtTokenProvider;
import wili_be.service.AmazonS3Service;
import wili_be.service.ProductService;
import wili_be.service.TokenService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

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
        log.info(file.getContentType());
        log.info(file.getOriginalFilename());
        try {
            // File을 AmazonS3Service를 사용하여 S3 버킷에 업로드
            String fileName = file.getOriginalFilename();
            String key = amazonS3Service.putObject(file, fileName);
            // 업로드 성공 응답
            return ResponseEntity.status(HttpStatus.OK).body("key: " + key);
        } catch (Exception e) {
            // 업로드 실패 시 에러 응답
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Image upload failed: " + e.getMessage());
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
            log.error("Failed to load image from S3.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/products/add")
    public ResponseEntity<String> addProduct(@RequestParam("file") MultipartFile file, @RequestParam("productInfo") String productInfoJson, HttpServletRequest httpServletRequest) {
        String accessToken = jwtTokenProvider.resolveToken(httpServletRequest);
        String snsId = jwtTokenProvider.getUsersnsId(accessToken);

        if (accessToken == null) {
            return createUnauthorizedResponse("접근 토큰이 없습니다");
        } else {
            StatusResult = tokenService.validateAccessToken(accessToken);
            if (StatusResult == StatusCode.UNAUTHORIZED) {
                return createExpiredTokenResponse("접근 토큰이 만료되었습니다");
            } else if (StatusResult == StatusCode.OK) {
                productService.addProduct(file, productInfoJson, snsId);
                return ResponseEntity.ok("Product added successfully.");
            } else {
                return createBadRequestResponse("잘못된 요청입니다");
            }
        }
    }

    @PostMapping("/users/products")
    ResponseEntity<?> getImageKeysByUser(HttpServletRequest httpRequest) {
        String accessToken = jwtTokenProvider.resolveToken(httpRequest);
        String snsId = jwtTokenProvider.getUsersnsId(accessToken);

        if (accessToken == null) {
            return createUnauthorizedResponse("접근 토큰이 없습니다");
        } else {
            StatusResult = tokenService.validateAccessToken(accessToken);
            if (StatusResult == StatusCode.UNAUTHORIZED) {
                return createExpiredTokenResponse("접근 토큰이 만료되었습니다");
            } else if (StatusResult == StatusCode.OK) {
                List<String> imageList = productService.getImagesKeysByMember(snsId);
                return ResponseEntity.ok().body(imageList);
            } else {
                return createBadRequestResponse("잘못된 요청입니다");
            }
        }
    }
    @GetMapping("/users/products")
    ResponseEntity<?> getPostsByUser(HttpServletRequest httpRequest) throws IOException {
        String accessToken = jwtTokenProvider.resolveToken(httpRequest);
        String snsId = jwtTokenProvider.getUsersnsId(accessToken);
            if (accessToken == null) {
                return createUnauthorizedResponse("접근 토큰이 없습니다");
            } else {
                StatusResult = tokenService.validateAccessToken(accessToken);
                if (StatusResult == StatusCode.UNAUTHORIZED) {
                    return createExpiredTokenResponse("접근 토큰이 만료되었습니다");
                } else if (StatusResult == StatusCode.OK) {
                    String images = productService.getImagesByMember(snsId);
                    String postList = productService.getPostByMember(snsId);

                    if (images == null && postList == null) {
                        return ResponseEntity.status(HttpStatus.OK)
                                .body("result: none");
                    }
                    return ResponseEntity.ok().body("images: " + images + " posts: " + postList);
                } else {
                    return createBadRequestResponse("잘못된 요청입니다");
                }
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