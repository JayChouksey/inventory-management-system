package com.example.coditas.factory.repository;

import com.example.coditas.factory.entity.Factory;
import com.example.coditas.factory.entity.UserFactoryMapping;
import com.example.coditas.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserFactoryMappingRepository extends JpaRepository<UserFactoryMapping, Long> {

    List<UserFactoryMapping> findByUser(User user);
}
