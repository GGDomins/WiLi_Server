package wili_be.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import wili_be.entity.Member;
import wili_be.entity.Post;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p.imageKey FROM Member m JOIN m.posts p WHERE m.snsId = :snsId")
    List<String> findImageKeysBysnsId(@Param("snsId") String snsId);

    @Query("SELECT p FROM Member m JOIN m.posts p WHERE m.snsId = :snsId")
    List<Post> findPostBySnsId(@Param("snsId") String snsId);

    @Query("SELECT p.thumbnailImageKey FROM Member m JOIN m.posts p WHERE m.snsId = :snsId")
    List<String> findThumbnailImageKeysBysnsId(@Param("snsId") String snsId);
    @Query("SELECT p FROM Post p WHERE p.category IN :favoriteCategories")
    List<Post> findPostsMatchingFavoriteCategories(@Param("favoriteCategories") List<String> favoriteCategories);

    List<Post> findPostsByBrandName(String brandName);


    @Query("select p from Post p where p.productName like :searchName")
    List<Post> findPostsByProductName(@Param("searchName") String searchName);


    Post findPostById(Long Id);
}
