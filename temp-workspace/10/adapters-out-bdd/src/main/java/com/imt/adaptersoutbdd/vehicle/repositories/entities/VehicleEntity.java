package com.imt.adaptersoutbdd.vehicle.repositories.entities;

import com.imt.vehicle.model.EngineTypeEnum;
import com.imt.vehicle.model.VehicleStateEnum;
import jakarta.validation.constraints.NotNull;
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
@Document(collection = "vehicle")
public class VehicleEntity {
    @Id
    private String id;
    private String licensePlate;
    private String brand;
    private String model;
    private EngineTypeEnum engineType;
    private String color;
    private LocalDate acquisitionDate;
    private VehicleStateEnum state;
}
