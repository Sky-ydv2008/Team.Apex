package com.apexinnovators.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Composite key of hackathon_projects (hackathon_id, project_id). */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HackathonProjectId implements Serializable {

    @Column(name = "hackathon_id")
    private Long hackathonId;

    @Column(name = "project_id")
    private Long projectId;
}
