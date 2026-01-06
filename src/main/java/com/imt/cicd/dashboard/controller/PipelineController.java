package com.imt.cicd.dashboard.controller;

import com.imt.cicd.dashboard.model.PipelineExecution;
import com.imt.cicd.dashboard.model.PipelineStatus;
import com.imt.cicd.dashboard.repository.PipelineRepository;
import com.imt.cicd.dashboard.service.PipelineManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;


@RestController
@RequestMapping("/api/pipelines")
@CrossOrigin(origins = "http://localhost:5173")
public class PipelineController {

    @Autowired
    private PipelineManager pipelineManager;

    @Autowired
    private PipelineRepository repository;

    // Lancer un déploiement
    @PostMapping("/run")
    public PipelineExecution triggerPipeline(@RequestBody String repoUrl) {
        PipelineExecution execution = new PipelineExecution();
        execution.setRepoUrl(repoUrl);
        execution.setBranch("main");
        execution.setStatus(PipelineStatus.PENDING);

        PipelineExecution saved = repository.save(execution);

        // Lance le processus en arrière-plan
        pipelineManager.runPipeline(saved.getId());

        return saved;
    }

    // Suivre l'état et les logs
    @GetMapping("/{id}")
    public PipelineExecution getPipeline(@PathVariable Long id) {
        return repository.findById(id).orElseThrow();
    }

    @PostMapping("/webhook")
    public void handleGithubWebhook(@RequestBody String payload, @RequestHeader("X-GitHub-Event") String eventType) {
        if ("push".equals(eventType)) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(payload);

                // Extraction dynamique de l'URL du repo
                String repoUrl = root.path("repository").path("clone_url").asText();
                String branch = root.path("ref").asText().replace("refs/heads/", "");

                if (repoUrl != null && !repoUrl.isEmpty()) {
                    PipelineExecution execution = new PipelineExecution();
                    execution.setRepoUrl(repoUrl);
                    execution.setBranch(branch); // Utilise la vraie branche du push
                    execution.setStatus(PipelineStatus.PENDING);

                    PipelineExecution saved = repository.save(execution);
                    pipelineManager.runPipeline(saved.getId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @GetMapping
    public List<PipelineExecution> getAllPipelines() {
        // Retourne la liste triée du plus récent au plus ancien
        return repository.findAllByOrderByStartTimeDesc();
    }
}
