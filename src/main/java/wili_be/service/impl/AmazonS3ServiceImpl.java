package wili_be.service.impl;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    public ResponseEntity<InputStreamResource> downloadObject(String key) {
        S3Object s3Object = getAmazonS3().getObject(bucketName, key);
        return ResponseEntity.ok()
                .contentLength(s3Object.getObjectMetadata().getContentLength())
                .contentType(MediaType.parseMediaType(s3Object.getObjectMetadata().getContentType()))
                .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS)) // キャッシュ設定
                .body(new InputStreamResource(s3Object.getObjectContent()));
    }

    private AmazonS3 getAmazonS3() {
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, secretAccessKey)))
                .withRegion(region)
                .build();
    }

    // 이미지 조회를 위한 메서드
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

    public List<byte[]> getImageBytesByKeys(List<String> keys) throws IOException {
        List<byte[]> imageBytesList = new ArrayList<>();

        for (String key : keys) {
            S3Object s3Object = getAmazonS3().getObject(bucketName, key);

            try (InputStream inputStream = s3Object.getObjectContent(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                imageBytesList.add(outputStream.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException("Failed to read the image from Amazon S3", e);
            }
        }

        return imageBytesList;
    }
}