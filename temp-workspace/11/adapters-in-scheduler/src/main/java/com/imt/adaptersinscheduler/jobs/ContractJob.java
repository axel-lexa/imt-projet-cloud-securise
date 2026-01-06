package com.imt.adaptersinscheduler.jobs;

import com.imt.common.exceptions.ImtException;
import com.imt.contracts.ContractsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ContractJob {

    private final ContractsService contractsService;

    //@Scheduled(cron = "0 0 1 * * *") // Runs daily at 1 AM
    @Scheduled(cron = "*/10 * * * * *") // Toutes les 10 secondes pour les tests
    public void processOverdueContracts() {
        log.info("SCHEDULER - Démarrage du traitement des contrats en retard");

        try {
            contractsService.updateOverdueContracts();
            log.info("SCHEDULER - Fin du traitement des contrats");
        } catch (ImtException e) {
            log.error("SCHEDULER - Erreur lors du traitement", e);
        }
    }
}
