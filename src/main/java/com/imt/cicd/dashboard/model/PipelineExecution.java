package com.imt.cicd.dashboard.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class PipelineExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String repoUrl;
    private String branch;

    @Enumerated(EnumType.STRING)
    private PipelineStatus status;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Column(columnDefinition = "TEXT")
    private String logs;

    public void appendLog(String log) {
        this.logs = (this.logs == null ? "" : this.logs) + log + "\n";
    }
}
