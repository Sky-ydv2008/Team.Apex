package com.apexinnovators.repository;

import com.apexinnovators.entity.User;
import com.apexinnovators.entity.UserStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findAllByStatusOrderByCreatedAtAsc(UserStatus status);

    long countByStatus(UserStatus status);

    @Query("""
            select u from User u
            where (:q = '' or lower(u.name) like lower(concat('%', :q, '%'))
                     or lower(u.email) like lower(concat('%', :q, '%')))
            order by u.createdAt desc, u.id desc
            """)
    Page<User> search(@Param("q") String q, Pageable pageable);
}
