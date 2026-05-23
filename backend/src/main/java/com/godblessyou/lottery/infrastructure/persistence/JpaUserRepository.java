package com.godblessyou.lottery.infrastructure.persistence;

import com.godblessyou.lottery.domain.entity.User;
import com.godblessyou.lottery.domain.repository.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRepository extends JpaRepository<User, Long>, UserRepository {
}
