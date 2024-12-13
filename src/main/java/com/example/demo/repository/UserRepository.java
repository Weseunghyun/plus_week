package com.example.demo.repository;

import com.example.demo.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.status = 'BLOCKED' WHERE u.id IN :userIds")
    int updateStatusToBlocked(@Param("userIds") List<Long> userIds);

    default User findByIdOrElseThrow(Long userId) {
        return findById(userId).orElseThrow(
            () -> new IllegalStateException("존재하지 않는 유저 id 입니다.")
        );
    }

}
