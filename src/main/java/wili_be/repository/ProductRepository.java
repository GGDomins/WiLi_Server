package wili_be.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import wili_be.entity.Member;
import wili_be.entity.Post;

import java.util.List;

public interface ProductRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p.imageKey FROM Member m JOIN m.posts p WHERE m.snsId = :snsId")
    List<String> findImageKeysBysnsId(@Param("snsId") String snsId);

    @Query("SELECT p FROM Member m JOIN m.posts p WHERE m.snsId = :snsId")
    List<Post> findPostBySnsId(@Param("snsId") String snsId);
}
