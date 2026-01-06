package com.imt.contracts;

import com.imt.contracts.model.Contract;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ContractStorageProvider {
    boolean exist(final UUID identifier);

    Collection<Contract> getAll();

    Optional<Contract> get(final UUID identifier);

    Collection<Contract> findByClientIdentifier(final UUID clientIdentifier);

    Collection<Contract> findByVehicleIdentifier(final UUID vehicleIdentifier);

    Collection<Contract> findByVehicleIdentifierBetween(final UUID vehicleIdentifier, final LocalDate startDate, final LocalDate endDate);

    Contract save(final Contract contract);

    void delete(final UUID identifier);

    /**
     * Règle 2 : Trouve les contrats "En cours" dont la date de fin est passée (avant la date donnée).
     */
    Collection<Contract> findOverdueContracts(final LocalDate referenceDate);

    /**
     * Règle 1 & 3 : Trouve les contrats "En attente" liés à un véhicule spécifique.
     */
    Collection<Contract> findPendingContractsByVehicleId(final UUID vehicleId);
}


