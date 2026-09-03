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

/** hackathon_members table — team members who took part in a hackathon. */
@Entity
@Table(name = "hackathon_members")
@Getter
@Setter
@NoArgsConstructor
public class HackathonMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hackathon_id", nullable = false)
    private Long hackathonId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public HackathonMember(Long hackathonId, Long userId) {
        this.hackathonId = hackathonId;
        this.userId = userId;
    }
}
