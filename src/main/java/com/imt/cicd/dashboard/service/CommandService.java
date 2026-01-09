package com.imt.cicd.dashboard.service;

import com.imt.cicd.dashboard.model.PipelineExecution;
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

    // Méthode par défaut (bavarde)
    public void executeCommand(String command, File workingDir, PipelineExecution execution) throws Exception {
        executeCommand(command, workingDir, execution, false);
    }

    // Méthode avec option "quiet"
    public void executeCommand(String command, File workingDir, PipelineExecution execution, boolean quiet) throws Exception {
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
                
                // Logique de filtrage ULTRA STRICTE si le mode quiet est activé
                if (quiet) {
                    // On ne garde que les vraies erreurs et la conclusion finale
                    boolean isCritical = line.contains("[ERROR]") || 
                                         line.contains("BUILD FAILURE") || 
                                         line.contains("ANALYSIS SUCCESSFUL") ||
                                         line.contains("EXECUTION FAILURE");
                    
                    if (!isCritical) {
                        continue; // On ignore tout le reste (INFO, WARN, téléchargements, etc.)
                    }
                }
                // System.out.println("[CMD] " + line);

                // Sauvegarde en base
                execution.appendLog(line);

                // Envoi temps réel au front
                messagingTemplate.convertAndSend("/topic/logs/" + execution.getId(), line);
            }
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Command failed with exit code " + exitCode);
        }
    }
}
