package wili_be.service.impl;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.amazonaws.services.s3.model.DeleteObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import wili_be.service.AmazonS3Service;

@Service
public class AmazonS3ServiceImpl implements AmazonS3Service {
    @Value("${aws.accessKeyId}")
    private String accessKeyId;

    @Value("${aws.secretAccessKey}")
    private String secretAccessKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.bucketName}")
    private String bucketName;

    @Transactional
    public String putObject(MultipartFile file, String filename) {
        try {
            String key = UUID.randomUUID().toString() + "/" + filename;
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(file.getContentType());
            objectMetadata.setContentLength(file.getSize());
            getAmazonS3().putObject(bucketName, key, file.getInputStream(), objectMetadata);
            return key;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Transactional
    public ResponseEntity<InputStreamResource> downloadObject(String key) {
        S3Object s3Object = getAmazonS3().getObject(bucketName, key);
        return ResponseEntity.ok()
                .contentLength(s3Object.getObjectMetadata().getContentLength())
                .contentType(MediaType.parseMediaType(s3Object.getObjectMetadata().getContentType()))
                .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
                .body(new InputStreamResource(s3Object.getObjectContent()));
    }

    private AmazonS3 getAmazonS3() {
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, secretAccessKey)))
                .withRegion(region)
                .build();
    }

    // 이미지 조회를 위한 메서드
    @Transactional
    public byte[] getImageBytesByKey(String key) throws IOException {
        S3Object s3Object = getAmazonS3().getObject(bucketName, key);

        try (InputStream inputStream = s3Object.getObjectContent(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read the image from Amazon S3", e);
        }
    }

    @Transactional
    public List<byte[]> getImageBytesByKeys(List<String> keys) throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(keys.size());
        try {
            List<CompletableFuture<byte[]>> futures = keys.stream()
                    .map(key -> CompletableFuture.supplyAsync(() -> downloadImageBytes(key), executorService))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            return futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Amazon S3에서 이미지를 읽어오는데 실패했습니다.", e);
        } finally {
            executorService.shutdown();
        }
    }
    private byte[] downloadImageBytes(String key) {
        S3Object s3Object = getAmazonS3().getObject(bucketName, key);

        try (InputStream inputStream = s3Object.getObjectContent(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Amazon S3에서 이미지를 읽어오는데 실패했습니다.", e);
        }
    }
    @Transactional
    public void deleteImageByKey(String key) {
        try {
            // AmazonS3 클라이언트를 생성합니다.
            AmazonS3 s3Client = getAmazonS3();

            // 이미지를 삭제합니다.
            s3Client.deleteObject(bucketName, key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



}
