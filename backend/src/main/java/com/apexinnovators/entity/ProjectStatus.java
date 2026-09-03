package com.apexinnovators.entity;

/**
 * Content lifecycle for projects and posts
 * (projects.status / posts.status ENUM). PUBLISHED = approved + visible;
 * public reads expose APPROVED or PUBLISHED only.
 */
public enum ProjectStatus {
    DRAFT,
    PENDING_REVIEW,
    APPROVED,
    REJECTED,
    PUBLISHED
}
