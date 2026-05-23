package com.godblessyou.lottery.domain.repository;

import com.godblessyou.lottery.domain.entity.User;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
