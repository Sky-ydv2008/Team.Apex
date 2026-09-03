package com.apexinnovators.repository;

import com.apexinnovators.entity.Comment;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);

    long countByPostId(Long postId);

    void deleteByAuthorId(Long authorId);

    void deleteByPostIdIn(Collection<Long> postIds);
}
