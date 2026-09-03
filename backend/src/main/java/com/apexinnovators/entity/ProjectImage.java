package com.apexinnovators.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * project_images table — gallery screenshots for a project. Managed
 * data-entry side; no REST surface in the MVP (schema parity only).
 */
@Entity
@Table(name = "project_images")
@Getter
@Setter
@NoArgsConstructor
public class ProjectImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(length = 300)
    private String caption;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}
