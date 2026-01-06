package com.imt.adaptersinscheduler.jobs;

import com.imt.common.exceptions.ImtException;
import com.imt.contracts.ContractsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ContractJobTest {

    @Mock
    private ContractsService contractsService;

    @InjectMocks
    private ContractJob contractJob;

    @Test
    @DisplayName("processOverdueContracts - Doit appeler le service pour traiter les contrats")
    void processOverdueContracts_shouldCallService() throws ImtException {
        // When
        contractJob.processOverdueContracts();

        // Then
        // On vérifie simplement que la méthode du domain a été appelée
        verify(contractsService).updateOverdueContracts();
    }
}
