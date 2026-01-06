package com.imt.adaptersoutbdd.contracts.repositories.entities;

import com.imt.contracts.model.ContractStateEnum;
import com.imt.vehicle.model.VehicleStateEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "contracts")
public class ContractEntity {

    @Id
    private String id;

    private String clientId;
    private String vehicleId;
    private LocalDate startDate;
    private LocalDate endDate;
    private ContractStateEnum state;
    private VehicleStateEnum vehicleState;
}


