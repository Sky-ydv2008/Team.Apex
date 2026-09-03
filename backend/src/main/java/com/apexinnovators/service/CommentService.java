package com.apexinnovators.service;

import com.apexinnovators.dto.CommentDto;
import com.apexinnovators.dto.CommentRequest;
import com.apexinnovators.entity.Comment;
import com.apexinnovators.entity.Post;
import com.apexinnovators.entity.ProjectStatus;
import com.apexinnovators.exception.ApiException;
import com.apexinnovators.repository.CommentRepository;
import com.apexinnovators.repository.PostRepository;
import com.apexinnovators.repository.UserRepository;
import com.apexinnovators.security.UserPrincipal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.apexinnovators.audit.AuditService;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private static final List<ProjectStatus> VISIBLE =
            List.of(ProjectStatus.APPROVED, ProjectStatus.PUBLISHED);

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    /** Public comment thread of a visible post, oldest first. */
    @Transactional(readOnly = true)
    public List<CommentDto> list(Long postId) {
        requireVisiblePost(postId);
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        return toDtos(comments);
    }

    /** Adds a comment to a visible post (member action). */
    @Transactional
    public CommentDto create(UserPrincipal actor, Long postId, CommentRequest request) {
        requireVisiblePost(postId);
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setAuthorId(actor.getId());
        comment.setBody(request.body().trim());
        commentRepository.save(comment);
        auditService.record(actor.getId(), "CREATE", "Comment", comment.getId(),
                "Commented on post #" + postId + " (user #" + actor.getId() + ")");
        return toDtos(List.of(comment)).get(0);
    }

    private void requireVisiblePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Post not found"));
        if (!VISIBLE.contains(post.getStatus())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Post not found");
        }
    }

    private List<CommentDto> toDtos(List<Comment> comments) {
        if (comments.isEmpty()) {
            return List.of();
        }
        Map<Long, String> namesById = new HashMap<>();
        userRepository.findAllById(comments.stream().map(Comment::getAuthorId).distinct().toList())
                .forEach(u -> namesById.put(u.getId(), u.getName()));
        return comments.stream()
                .map(c -> new CommentDto(c.getId(), c.getAuthorId(), namesById.get(c.getAuthorId()),
                        c.getBody(), c.getCreatedAt()))
                .toList();
    }
}
