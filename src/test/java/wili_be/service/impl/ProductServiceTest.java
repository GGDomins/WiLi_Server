package wili_be.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import wili_be.dto.PostDto;
import wili_be.dto.PostDto.PostUpdateResponseDto;
import wili_be.entity.Post;
import wili_be.repository.ProductRepository;
import wili_be.service.ProductService;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static wili_be.dto.PostDto.*;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;
    private PostInfoDto postInfoDto1;
    private PostInfoDto postInfoDto2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        //테스트 시작 전에 PostInfo_Dto 객체를 초기화
        postInfoDto1 = new PostInfoDto();
        postInfoDto1.setBrandName("나이키");
        postInfoDto1.setProductName("범고래");
        postInfoDto1.setCategory("신발");
        postInfoDto1.setProductPrice("200");
        postInfoDto1.setDescription("범고래 신발 너무 흔해요");
        postInfoDto1.setLink("www.nike.com");

        postInfoDto2 = new PostInfoDto();
        postInfoDto2.setBrandName("애플");
        postInfoDto2.setProductName("아이폰 15");
        postInfoDto2.setCategory("핸드폰");
        postInfoDto2.setProductPrice("100");
        postInfoDto2.setDescription("아이폰 15는 C타입입니다.");
        postInfoDto2.setLink("www.iphone.com");
    }

    @Test
    void updatePost_Successful() {
        // Given
        Long postId = 1L;
        Long postId2 = 2L;

        Post existingPost = postInfoDto2.to_Entity();
        existingPost.setId(postId);

        PostUpdateResponseDto postUpdateDto = new PostUpdateResponseDto();
        postUpdateDto.setBrandName("나이키");
        postUpdateDto.setProductName("범고래");
        postUpdateDto.setCategory("신발");
        postUpdateDto.setProductPrice("200");
        postUpdateDto.setDescription("범고래 신발 너무 흔해");
        postUpdateDto.setLink("www.nike.com");

        when(productRepository.findPostById(postId)).thenReturn(existingPost);
        when(productRepository.save(any(Post.class))).thenReturn(existingPost);

        // When
        PostResponseDto updatedPostResponse = productService.updatePost(postId, postUpdateDto);

        // Then
        assertNotNull(updatedPostResponse);
        assertEquals(postId, updatedPostResponse.getId());
        assertEquals("나이키", updatedPostResponse.getBrandName());
        assertEquals("범고래", updatedPostResponse.getProductName());
        assertEquals("신발", updatedPostResponse.getCategory());
        assertEquals("200", updatedPostResponse.getProductPrice());
        assertEquals("범고래 신발 너무 흔해", updatedPostResponse.getDescription());
        assertEquals("www.nike.com", updatedPostResponse.getLink());

        verify(productRepository).findPostById(postId);
        verify(productRepository).save(any(Post.class));
    }

    @Test
    void updatePost_PostNotFound() {
        // Given
        Long postId = 1L;
        PostUpdateResponseDto postUpdateDto = new PostUpdateResponseDto();
        postUpdateDto.setBrandName("Updated Brand");

        when(productRepository.findPostById(postId)).thenReturn(null);

        // When & Then
        assertThrows(NoSuchElementException.class, () -> productService.updatePost(postId, postUpdateDto));

        verify(productRepository).findPostById(postId);
        verifyNoMoreInteractions(productRepository);
    }
    @Test
    public void testDeletePostByPostId() {
        // given
        Long postId = 1L;
        Post post = postInfoDto2.to_Entity();
        post.setId(postId);

        when(productRepository.findPostById(postId)).thenReturn(post);

        // when
        productService.deletePostByPostId(postId);

        // then
        verify(productRepository, times(1)).delete(post);
    }
}
