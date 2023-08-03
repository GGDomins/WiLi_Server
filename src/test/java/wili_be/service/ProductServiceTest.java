package wili_be.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import wili_be.dto.PostDto;
import wili_be.dto.PostDto.PostUpdateResponseDto;
import wili_be.entity.Post;
import wili_be.repository.ProductRepository;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static wili_be.dto.PostDto.*;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void updatePost_Successful() {
        // Given
        Long postId = 1L;
        PostUpdateResponseDto postUpdateDto = new PostUpdateResponseDto();
        postUpdateDto.setBrandName("나이키");
        postUpdateDto.setProductName("범고래");
        postUpdateDto.setCategory("신발");
        postUpdateDto.setProductPrice("200");
        postUpdateDto.setDescription("범고래 신발 너무 흔해");
        postUpdateDto.setLink("www.nike.com");

        Post existingPost = new Post();
        existingPost.setId(postId);
        existingPost.setBrandName("애플");
        existingPost.setProductName("아이폰 15");
        existingPost.setCategory("핸드폰");
        existingPost.setProductPrice("100");
        existingPost.setDescription("아이폰 15는 C타입입니다.");
        existingPost.setLink("www.iphone.com");

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
}
