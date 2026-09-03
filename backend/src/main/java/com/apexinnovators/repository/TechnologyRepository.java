package com.apexinnovators.repository;

import com.apexinnovators.entity.Technology;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechnologyRepository extends JpaRepository<Technology, Long> {

    List<Technology> findAllByOrderByNameAsc();

    boolean existsByName(String name);
}
