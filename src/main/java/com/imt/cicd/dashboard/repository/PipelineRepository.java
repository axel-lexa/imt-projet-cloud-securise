package com.imt.cicd.dashboard.repository;

import com.imt.cicd.dashboard.model.PipelineExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PipelineRepository extends JpaRepository<PipelineExecution, Long> {

    List<PipelineExecution> findAllByOrderByStartTimeDesc();

    // (Optionnel) Pour trouver le dernier déploiement réussi (utile pour le Rollback)
    // Optional<PipelineExecution> findFirstByStatusAndRepoUrlOrderByEndTimeDesc(PipelineStatus status, String repoUrl);
}
