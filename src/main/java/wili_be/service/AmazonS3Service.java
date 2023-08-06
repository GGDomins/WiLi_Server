package wili_be.service;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
public interface AmazonS3Service {
    String putObject(byte[] imageBytes, String filename);
    ResponseEntity<InputStreamResource> downloadObject(String key);
    byte[] getImageBytesByKey(String key) throws IOException;
    List<byte[]> getImageBytesByKeys(List<String> keys) throws IOException;
    void deleteImageByKey(String key);
    void deleteImagesByKeys(List<String> keys);
}
