package com.apexinnovators.service;

import com.apexinnovators.audit.AuditService;
import com.apexinnovators.dto.AdminCreatePostRequest;
import com.apexinnovators.dto.PageResponse;
import com.apexinnovators.dto.PostDto;
import com.apexinnovators.dto.PostRequest;
import com.apexinnovators.dto.PostStatusRequest;
import com.apexinnovators.dto.PostUpdateRequest;
import com.apexinnovators.entity.Post;
import com.apexinnovators.entity.PostLike;
import com.apexinnovators.entity.PostType;
import com.apexinnovators.entity.ProjectStatus;
import com.apexinnovators.entity.User;
import com.apexinnovators.exception.ApiException;
import com.apexinnovators.repository.CommentRepository;
import com.apexinnovators.repository.PostLikeRepository;
import com.apexinnovators.repository.PostRepository;
import com.apexinnovators.repository.UserRepository;
import com.apexinnovators.security.UserPrincipal;
import com.apexinnovators.util.PageUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private static final List<ProjectStatus> VISIBLE =
            List.of(ProjectStatus.APPROVED, ProjectStatus.PUBLISHED);
    private static final List<ProjectStatus> ALL_STATUSES =
            List.of(ProjectStatus.values());

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final AuditService auditService;

    // ------------------------------------------------------------------
    // Public reads
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public PageResponse<PostDto> pagePublished(PostType type, Integer page, Integer size, Long viewerId) {
        return list(VISIBLE, type, page, size, viewerId);
    }

    @Transactional(readOnly = true)
    public PostDto getPublished(Long id, UserPrincipal viewer) {
        Post post = requirePost(id);
        if (!VISIBLE.contains(post.getStatus())
                && (viewer == null || viewer.getId().longValue() != post.getAuthorId().longValue())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Post not found");
        }
        return toDto(post, viewer == null ? null : viewer.getId());
    }

    @Transactional(readOnly = true)
    public List<PostDto> searchTopPublished(String q) {
        List<Post> posts = postRepository.searchTop(VISIBLE, q == null ? "" : q.trim(),
                PageUtil.of(0, 10));
        return posts.stream().map(p -> toDto(p, null)).toList();
    }

    // ------------------------------------------------------------------
    // Member actions
    // ------------------------------------------------------------------

    /** Creates a DRAFT post owned by the acting member. */
    @Transactional
    public PostDto create(UserPrincipal actor, PostRequest request) {
        Post post = new Post();
        post.setAuthorId(actor.getId());
        post.setType(request.type());
        post.setTitle(request.title().trim());
        post.setBody(request.body());
        post.setStatus(ProjectStatus.DRAFT);
        postRepository.save(post);
        return toDto(post, actor.getId());
    }

    /** Editing is limited to the post author. */
    @Transactional
    public PostDto update(UserPrincipal actor, Long id, PostUpdateRequest request) {
        Post post = requirePost(id);
        requireAuthor(post, actor);
        post.setTitle(request.title().trim());
        if (request.type() != null) {
            post.setType(request.type());
        }
        post.setBody(request.body());
        postRepository.save(post);
        return toDto(post, actor.getId());
    }

    /** Submits the author's DRAFT/REJECTED post for review. */
    @Transactional
    public PostDto submit(UserPrincipal actor, Long id) {
        Post post = requirePost(id);
        requireAuthor(post, actor);
        if (post.getStatus() != ProjectStatus.DRAFT && post.getStatus() != ProjectStatus.REJECTED) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Only draft or rejected posts can be submitted for review");
        }
        post.setStatus(ProjectStatus.PENDING_REVIEW);
        postRepository.save(post);
        return toDto(post, actor.getId());
    }

    @Transactional
    public void like(UserPrincipal actor, Long postId) {
        requireVisiblePost(postId);
        if (!postLikeRepository.existsByPostIdAndUserId(postId, actor.getId())) {
            postLikeRepository.save(new PostLike(postId, actor.getId()));
        }
    }

    @Transactional
    public void unlike(UserPrincipal actor, Long postId) {
        requireVisiblePost(postId);
        postLikeRepository.deleteByPostIdAndUserId(postId, actor.getId());
    }

    // ------------------------------------------------------------------
    // Admin actions
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public PageResponse<PostDto> pageAdmin(ProjectStatus status, Integer page, Integer size) {
        List<ProjectStatus> statuses = status == null ? ALL_STATUSES : List.of(status);
        return list(statuses, null, page, size, null);
    }

    @Transactional
    public PostDto patchStatus(UserPrincipal actor, Long id, PostStatusRequest request) {
        Post post = requirePost(id);
        post.setStatus(request.status());
        postRepository.save(post);
        auditService.record(actor.getId(), "STATUS_CHANGE", "Post", post.getId(),
                "Status " + post.getStatus() + " for post '" + post.getTitle() + "'");
        return toDto(post, null);
    }

    /** ADMIN creates a post directly; author is the acting admin, status defaults to PUBLISHED. */
    @Transactional
    public PostDto createAdmin(UserPrincipal actor, AdminCreatePostRequest request) {
        Post post = new Post();
        post.setAuthorId(actor.getId());
        post.setType(request.type());
        post.setTitle(request.title().trim());
        post.setBody(request.body());
        post.setStatus(request.status() == null ? ProjectStatus.PUBLISHED : request.status());
        postRepository.save(post);
        auditService.record(actor.getId(), "CREATE", "Post", post.getId(),
                "Created post '" + post.getTitle() + "' as " + post.getStatus());
        return toDto(post, null);
    }

    // ------------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------------

    private PageResponse<PostDto> list(List<ProjectStatus> statuses, PostType type,
                                       Integer page, Integer size, Long viewerId) {
        Pageable pageable = PageUtil.of(page, size);
        Page<Post> posts = (type == null)
                ? postRepository.findByStatusInOrderByCreatedAtDesc(statuses, pageable)
                : postRepository.findByStatusInAndTypeOrderByCreatedAtDesc(statuses, type, pageable);
        List<PostDto> content = posts.getContent().stream()
                .map(p -> toDto(p, viewerId))
                .toList();
        return new PageResponse<>(content, posts.getNumber(), posts.getSize(),
                posts.getTotalElements(), posts.getTotalPages());
    }

    private PostDto toDto(Post post, Long viewerId) {
        User author = userRepository.findById(post.getAuthorId()).orElse(null);
        boolean likedByMe = viewerId != null
                && postLikeRepository.existsByPostIdAndUserId(post.getId(), viewerId);
        return new PostDto(
                post.getId(),
                post.getAuthorId(),
                author == null ? null : author.getName(),
                post.getType(),
                post.getTitle(),
                post.getBody(),
                post.getStatus(),
                post.getCreatedAt(),
                commentRepository.countByPostId(post.getId()),
                postLikeRepository.countByPostId(post.getId()),
                likedByMe);
    }

    private void requireAuthor(Post post, UserPrincipal actor) {
        if (!post.getAuthorId().equals(actor.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only manage your own posts");
        }
    }

    private void requireVisiblePost(Long postId) {
        Post post = requirePost(postId);
        if (!VISIBLE.contains(post.getStatus())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Post not found");
        }
    }

    private Post requirePost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Post not found"));
    }
}
