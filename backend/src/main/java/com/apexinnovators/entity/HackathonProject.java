package com.apexinnovators.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** hackathon_projects table — links a hackathon result to its project(s). */
@Entity
@Table(name = "hackathon_projects")
@IdClass(HackathonProjectId.class)
@Getter
@Setter
@NoArgsConstructor
public class HackathonProject {

    @Id
    @Column(name = "hackathon_id")
    private Long hackathonId;

    @Id
    @Column(name = "project_id")
    private Long projectId;

    public HackathonProject(Long hackathonId, Long projectId) {
        this.hackathonId = hackathonId;
        this.projectId = projectId;
    }
}
