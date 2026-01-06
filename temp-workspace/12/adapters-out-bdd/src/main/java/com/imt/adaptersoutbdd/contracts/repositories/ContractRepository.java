package com.imt.adaptersoutbdd.contracts.repositories;

import com.imt.adaptersoutbdd.contracts.repositories.entities.ContractEntity;
import com.imt.contracts.model.ContractStateEnum;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ContractRepository extends MongoRepository<ContractEntity, String> {

    List<ContractEntity> findByClientId(String clientId);

    List<ContractEntity> findByVehicleId(String vehicleId);

    List<ContractEntity> findByVehicleIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            String vehicleId,
            LocalDate startDate,
            LocalDate endDate
    );

    // Pour la Règle 2 : Trouver les contrats EN COURS dont la date de fin est AVANT aujourd'hui
    List<ContractEntity> findByStateAndEndDateBefore(ContractStateEnum state, LocalDate date);

    // Pour la Règle 1 : Trouver les contrats EN ATTENTE liés à un véhicule
    List<ContractEntity> findByVehicleIdAndState(String vehicleId, ContractStateEnum state);}


