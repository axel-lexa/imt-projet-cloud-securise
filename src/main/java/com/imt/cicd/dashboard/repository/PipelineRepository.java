package com.imt.cicd.dashboard.repository;

import com.imt.cicd.dashboard.model.PipelineExecution;
import com.imt.cicd.dashboard.model.PipelineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PipelineRepository extends JpaRepository<PipelineExecution, Long> {

    List<PipelineExecution> findAllByOrderByStartTimeDesc();

    Optional<PipelineExecution> findFirstByRepoUrlAndStatusOrderByStartTimeDesc(String repoUrl, PipelineStatus status);
}