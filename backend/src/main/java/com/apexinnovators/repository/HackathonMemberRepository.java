package com.apexinnovators.repository;

import com.apexinnovators.entity.HackathonMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HackathonMemberRepository extends JpaRepository<HackathonMember, Long> {

    List<HackathonMember> findByHackathonIdOrderByIdAsc(Long hackathonId);
}
