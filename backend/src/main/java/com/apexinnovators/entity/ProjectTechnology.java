package com.apexinnovators.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** project_technologies table — many-to-many link between projects and technologies. */
@Entity
@Table(name = "project_technologies")
@IdClass(ProjectTechnologyId.class)
@Getter
@Setter
@NoArgsConstructor
public class ProjectTechnology {

    @Id
    @Column(name = "project_id")
    private Long projectId;

    @Id
    @Column(name = "technology_id")
    private Long technologyId;

    public ProjectTechnology(Long projectId, Long technologyId) {
        this.projectId = projectId;
        this.technologyId = technologyId;
    }
}
