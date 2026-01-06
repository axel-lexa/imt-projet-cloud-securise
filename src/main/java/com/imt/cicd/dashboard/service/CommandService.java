package com.imt.cicd.dashboard.service;

import com.imt.cicd.dashboard.model.PipelineExecution;
import com.imt.cicd.dashboard.model.PipelineStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

@Service
public class CommandService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void executeCommand(String command, File workingDir, PipelineExecution execution) throws Exception {
        ProcessBuilder builder = new ProcessBuilder();

        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        if (isWindows) {
            builder.command("cmd.exe", "/c", command);
        } else {
            builder.command("sh", "-c", command);
        }

        builder.directory(workingDir);
        builder.redirectErrorStream(true);

        Process process = builder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Sauvegarde en base
                execution.appendLog(line);

                // Envoi temps réel au front (Topic : /topic/logs/{id})
                messagingTemplate.convertAndSend("/topic/logs/" + execution.getId(), line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Command failed with exit code " + exitCode);
        }
    }
}
