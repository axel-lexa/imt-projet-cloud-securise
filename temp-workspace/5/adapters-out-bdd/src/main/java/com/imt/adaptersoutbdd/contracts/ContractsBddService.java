package com.imt.adaptersoutbdd.contracts;

import com.imt.adaptersoutbdd.contracts.repositories.ContractRepository;
import com.imt.adaptersoutbdd.contracts.repositories.mappers.ContractBddMapper;
import com.imt.contracts.ContractStorageProvider;
import com.imt.contracts.model.Contract;
import com.imt.contracts.model.ContractStateEnum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ContractsBddService implements ContractStorageProvider {

    private final ContractRepository contractRepository;
    private final ContractBddMapper contractBddMapper;

    @Override
    public boolean exist(UUID identifier) {
        return Optional.ofNullable(identifier)
                .map(UUID::toString)
                .map(contractRepository::existsById)
                .orElse(false);
    }

    @Override
    public Collection<Contract> getAll() {
        return contractRepository.findAll()
                .stream()
                .map(contractBddMapper::from)
                .toList();
    }

    @Override
    public Optional<Contract> get(UUID identifier) {
        return Optional.ofNullable(identifier)
                .map(UUID::toString)
                .flatMap(contractRepository::findById)
                .map(contractBddMapper::from);
    }

    @Override
    public Collection<Contract> findByClientIdentifier(UUID clientIdentifier) {
        return Optional.ofNullable(clientIdentifier)
                .map(UUID::toString)
                .map(contractRepository::findByClientId)
                .orElseGet(java.util.List::of)
                .stream()
                .map(contractBddMapper::from)
                .toList();
    }

    @Override
    public Collection<Contract> findByVehicleIdentifier(UUID vehicleIdentifier) {
        return Optional.ofNullable(vehicleIdentifier)
                .map(UUID::toString)
                .map(contractRepository::findByVehicleId)
                .orElseGet(java.util.List::of)
                .stream()
                .map(contractBddMapper::from)
                .toList();
    }

    @Override
    public Collection<Contract> findByVehicleIdentifierBetween(UUID vehicleIdentifier, LocalDate startDate, LocalDate endDate) {
        if (vehicleIdentifier == null || startDate == null || endDate == null) {
            return java.util.List.of();
        }

        return contractRepository
                .findByVehicleIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        vehicleIdentifier.toString(),
                        startDate,
                        endDate
                )
                .stream()
                .map(contractBddMapper::from)
                .toList();
    }

    @Override
    public Contract save(Contract contract) {
        return Optional.ofNullable(contract)
                .map(contractBddMapper::to)
                .map(contractRepository::save)
                .map(contractBddMapper::from)
                .orElse(null);
    }

    @Override
    public void delete(UUID identifier) {
        Optional.ofNullable(identifier)
                .map(UUID::toString)
                .ifPresent(contractRepository::deleteById);
    }

    @Override
    public Collection<Contract> findOverdueContracts(LocalDate referenceDate) {
        // On cherche ceux qui sont "EN COURS" mais dont la date de fin est dépassée
        return contractRepository.findByStateAndEndDateBefore(ContractStateEnum.IN_PROGRESS, referenceDate)
                .stream()
                .map(contractBddMapper::from)
                .toList();
    }

    @Override
    public Collection<Contract> findPendingContractsByVehicleId(UUID vehicleId) {
        // On cherche ceux qui sont "EN ATTENTE" pour ce véhicule
        return contractRepository.findByVehicleIdAndState(vehicleId.toString(), ContractStateEnum.PENDING)
                .stream()
                .map(contractBddMapper::from)
                .toList();
    }
}


