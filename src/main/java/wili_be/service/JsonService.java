package wili_be.service;

import wili_be.dto.MemberDto;
import wili_be.dto.PostDto;

import java.util.List;

import static wili_be.dto.MemberDto.*;
import static wili_be.dto.PostDto.*;

public interface JsonService {
    String changePostResponseDtoToJson(PostResponseDto postResponseDto);

    String changeByteToJson(byte[] bytes);

    List<String> changeByteListToJson(List<byte[]> bytes);

    List<String> changePostMainPageResponseDtoListToJson(List<PostMainPageResponse> pageResponses);

    String changeMemberInfoDtoToJson(Member_info_Dto memberInfoDto);

    String changeMemberResponseDtoToJson(MemberResponseDto memberResponseDto);
}
