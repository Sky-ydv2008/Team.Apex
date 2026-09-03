package com.apexinnovators.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Composite key of project_technologies (project_id, technology_id). */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTechnologyId implements Serializable {

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "technology_id")
    private Long technologyId;
}
