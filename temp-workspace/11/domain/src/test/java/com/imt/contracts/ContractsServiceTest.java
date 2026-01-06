package com.imt.contracts;

import com.imt.common.exceptions.ImtException;
import com.imt.contracts.model.Contract;
import com.imt.contracts.model.ContractStateEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContractsServiceTest {

    @Mock
    private ContractStorageProvider repository;

    @InjectMocks
    private ContractsService service;

    @Test
    @DisplayName("updateOverdueContracts should update overdue contracts to LATE and cancel future contracts for the same vehicle")
    void updateOverdueContracts_shouldUpdateToLate_AndCancelNext() throws ImtException {
        // Given
        Contract overdueContract = Contract.builder()
                .identifier(UUID.randomUUID())
                .vehicleIdentifier(UUID.randomUUID())
                .state(ContractStateEnum.IN_PROGRESS)
                .endDate(LocalDate.now().minusDays(1)) // Fini hier
                .build();

        Contract futureContract = Contract.builder()
                .identifier(UUID.randomUUID())
                .vehicleIdentifier(overdueContract.getVehicleIdentifier()) // Même véhicule
                .state(ContractStateEnum.PENDING)
                .startDate(LocalDate.now()) // Commence aujourd'hui
                .build();

        // Mock du port de sortie
        when(repository.findOverdueContracts(any())).thenReturn(List.of(overdueContract));
        when(repository.findPendingContractsByVehicleId(overdueContract.getVehicleIdentifier()))
                .thenReturn(List.of(futureContract));

        // When
        service.updateOverdueContracts();

        // Then
        // On capture les sauvegardes pour vérifier les changements d'état
        ArgumentCaptor<Contract> captor = ArgumentCaptor.forClass(Contract.class);
        verify(repository, times(2)).save(captor.capture()); // 1 pour le retard, 1 pour l'annulation

        List<Contract> savedContracts = captor.getAllValues();

        // Règle 2 : Le contrat expiré doit passer à LATE
        assertThat(savedContracts).anyMatch(c ->
                c.getIdentifier().equals(overdueContract.getIdentifier())
                        && c.getState() == ContractStateEnum.LATE
        );

        // Règle 3 : Le contrat futur bloqué doit passer à CANCELLED
        assertThat(savedContracts).anyMatch(c ->
                c.getIdentifier().equals(futureContract.getIdentifier())
                        && c.getState() == ContractStateEnum.CANCELLED
        );
    }
}
