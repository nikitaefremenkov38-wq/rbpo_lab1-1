package com.example.demo.repository;

import com.example.demo.entity.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttractionRepository extends JpaRepository<Attraction, Long> {
    boolean existsByNameIgnoreCase(String name);
}
