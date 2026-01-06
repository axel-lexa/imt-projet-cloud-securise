package com.imt.contracts;

import com.imt.common.exceptions.ImtException;
import com.imt.contracts.model.Contract;
import com.imt.contracts.model.ContractStateEnum;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class ContractsService {

    protected ContractStorageProvider service;

    public Collection<Contract> getAll() {
        return Objects.requireNonNullElse(this.service.getAll(), Collections.emptySet());
    }

    public Optional<Contract> getOne(final UUID identifier) {
        return this.service.get(identifier);
    }

    public Collection<Contract> getByClient(final UUID clientIdentifier) {
        return Objects.requireNonNullElse(
                this.service.findByClientIdentifier(clientIdentifier),
                Collections.emptySet()
        );
    }

    public Collection<Contract> getByVehicle(final UUID vehicleIdentifier) {
        return Objects.requireNonNullElse(
                this.service.findByVehicleIdentifier(vehicleIdentifier),
                Collections.emptySet()
        );
    }

    public Collection<Contract> getByVehicleBetween(final UUID vehicleIdentifier,
                                                    final LocalDate startDate,
                                                    final LocalDate endDate) {
        return Objects.requireNonNullElse(
                this.service.findByVehicleIdentifierBetween(vehicleIdentifier, startDate, endDate),
                Collections.emptySet()
        );
    }

    public Contract create(final Contract newContract) throws ImtException {
        return this.service.save(newContract);
    }

    public void update(final Contract updatedContract) throws ImtException {
        this.service.save(updatedContract);
    }

    public void delete(final UUID identifier) throws ImtException {
        this.service.delete(identifier);
    }

    /**
     * Règle 1 : Annulation automatique.
     * Si un véhicule est déclaré en panne, annuler les contrats en attente.
     * Cette méthode doit être appelée par le VehicleService ou via un événement.
     */
    public void cancelContractsForBrokenVehicule(final UUID vehicleId) throws ImtException {
        Collection<Contract> pendingContracts = this.service.findPendingContractsByVehicleId(vehicleId);

        for (Contract contract : pendingContracts) {
            Contract cancelledContract = contract.toBuilder()
                    .state(ContractStateEnum.CANCELLED)
                    .build();

            this.update(cancelledContract);
        }
    }

    /**
     * Règle 2 et 3 : Gestion des retards et annulation en cascade.
     * Méthode destinée à être appelée par le Scheduler.
     */
    public void updateOverdueContracts() throws ImtException {
        LocalDate today = LocalDate.now();
        // Règle 2 : Trouver les contrats qui auraient dû finir hier ou avant
        Collection<Contract> overdueContracts = this.service.findOverdueContracts(today);

        for (Contract contract : overdueContracts) {
            // Passage à l'état LATE
            Contract lateContract = contract.toBuilder()
                    .state(ContractStateEnum.LATE)
                    .build();
            this.update(lateContract);

            // Règle 3 : Cascade. Si ce retard empêche un contrat futur de démarrer.
            // On vérifie s'il y a des contrats en attente pour ce véhicule qui commencent aujourd'hui ou avant (puisque la voiture n'est pas là)
            this.cancelConflictingPendingContracts(lateContract.getVehicleIdentifier(), today);
        }
    }

    private void cancelConflictingPendingContracts(final UUID vehicleId, final LocalDate referenceDate) throws ImtException {
        Collection<Contract> pendingContracts = this.service.findPendingContractsByVehicleId(vehicleId);

        for (Contract pending : pendingContracts) {
            // Si le contrat en attente est censé avoir commencé (ou commence ajd) alors que la voiture est absente
            if (!pending.getStartDate().isAfter(referenceDate)) {
                Contract cancelled = pending.toBuilder()
                        .state(ContractStateEnum.CANCELLED)
                        .build();
                this.update(cancelled);
            }
        }
    }
}


