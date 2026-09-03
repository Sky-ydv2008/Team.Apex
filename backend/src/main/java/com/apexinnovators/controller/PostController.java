package com.apexinnovators.controller;

import com.apexinnovators.dto.PageResponse;
import com.apexinnovators.dto.PostDto;
import com.apexinnovators.dto.PostRequest;
import com.apexinnovators.dto.PostUpdateRequest;
import com.apexinnovators.entity.PostType;
import com.apexinnovators.security.UserPrincipal;
import com.apexinnovators.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "Community Posts", description = "Published post listing/detail plus member create, edit, submit, comments and likes")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    @Operation(summary = "List published posts (page/size/type filter)")
    public PageResponse<PostDto> list(@RequestParam(required = false) PostType type,
                                      @RequestParam(required = false) Integer page,
                                      @RequestParam(required = false) Integer size,
                                      @AuthenticationPrincipal UserPrincipal principal) {
        Long viewerId = principal == null ? null : principal.getId();
        return postService.pagePublished(type, page, size, viewerId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a published post (author may view their own posts in any status)")
    public PostDto get(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return postService.getPublished(id, principal);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a post as DRAFT")
    public PostDto create(@AuthenticationPrincipal UserPrincipal principal,
                          @Valid @RequestBody PostRequest request) {
        return postService.create(principal, request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update own post content")
    public PostDto update(@AuthenticationPrincipal UserPrincipal principal,
                          @PathVariable Long id,
                          @Valid @RequestBody PostUpdateRequest request) {
        return postService.update(principal, id, request);
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit own draft or rejected post for review")
    public PostDto submit(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return postService.submit(principal, id);
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "Like a published post (idempotent)")
    public ResponseEntity<Void> like(@AuthenticationPrincipal UserPrincipal principal,
                                     @PathVariable Long id) {
        postService.like(principal, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/like")
    @Operation(summary = "Remove the like from a published post (idempotent)")
    public ResponseEntity<Void> unlike(@AuthenticationPrincipal UserPrincipal principal,
                                       @PathVariable Long id) {
        postService.unlike(principal, id);
        return ResponseEntity.noContent().build();
    }
}
