package com.ey.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ey.entity.Interview;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
	List<Interview> findByApplicationIdIn(List<Long> applicationIds);

	List<Interview> findByInterviewerId(Long interviewerId);

	List<Interview> findByCreatedById(Long creatorId);

	List<Interview> findByApplicationJobHrId(Long hrId);
}