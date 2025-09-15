package com.ey.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ey.entity.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {
	Optional<Company> findByName(String name);
}