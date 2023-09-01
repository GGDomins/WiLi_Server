package wili_be.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import wili_be.dto.MemberDto;
import wili_be.dto.PostDto;
import wili_be.dto.PostDto.PostResponseDto;
import wili_be.service.JsonService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static wili_be.dto.MemberDto.*;

@Service
public class JsonServiceImpl implements JsonService {

    @Override
    public String changePostResponseDtoToJson(PostResponseDto postResponseDto) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String postJson = objectMapper.writeValueAsString(postResponseDto);
            return postJson;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String changeByteToJson(byte[] bytes) {
        String encodedImage = java.util.Base64.getEncoder().encodeToString(bytes);
        return encodedImage;
    }

    @Override
    public List<String> changeByteListToJson(List<byte[]> bytes) {
        List<String> jsonList = new ArrayList<>();
        for (byte[] imageBytes : bytes) {
            // 바이트 배열을 Base64로 인코딩하여 문자열로 변환합니다.
            String encodedImage = java.util.Base64.getEncoder().encodeToString(imageBytes);
            jsonList.add(encodedImage);
        }
        return jsonList;
    }

    @Override
    public List<String> changePostMainPageResponseDtoListToJson(List<PostDto.PostMainPageResponse> pageResponseList) {
        List<String> postJsonList = pageResponseList.stream()
                .map(postResponseDto -> {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        return objectMapper.writeValueAsString(postResponseDto);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(json -> json != null)
                .collect(Collectors.toList());
        return postJsonList;
    }
}
