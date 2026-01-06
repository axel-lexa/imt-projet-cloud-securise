package com.imt.adaptersinrest.vehicle.model.input;

import com.imt.adaptersinrest.common.model.input.AbstractInput;
import com.imt.vehicle.model.EngineTypeEnum;
import com.imt.vehicle.model.Vehicle;
import com.imt.vehicle.model.VehicleStateEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class VehicleInput extends AbstractInput {
    @Serial
    private static final long serialVersionUID = 123456789L;

    @NotNull(message = "La marque ne peut pas être nulle")
    @Pattern(regexp = "^[a-zA-Z0-9 -]{1,100}$", message = "La marque doit contenir des lettres, des chiffres, des espaces, des tirets, longueur 1-100")
    private String brand;

    @NotNull(message = "le modèle ne peut pas être nul")
    @Pattern(regexp = "^[a-zA-Z0-9 -]{1,100}$", message = "Le modèle doit contenir des lettres, des chiffres, des espaces, des tirets, longueur 1-100")
    private String model;

    @NotNull(message = "Le type de moteur ne peut pas être nul")
    private EngineTypeEnum engineType;

    @NotNull(message = "La couleur ne peut pas être nulle")
    @Pattern(regexp = "^[a-zA-Z ]{1,50}$", message = "La couleur doit contenir des lettres et des espaces, longueur 1-50")
    private String color;

    @NotNull(message = "La plaque d'immatriculation ne peut pas être nulle")
    @Pattern(regexp = "^[A-Z]{2}-[0-9]{3}-[A-Z]{2}$", message = "La plaque d'immatriculation doit suivre le format XX-999-XX")
    private String licensePlate;

    @NotNull(message = "La date d'acquisition ne peut pas être nulle")
    private LocalDate acquisitionDate;

    @NotNull(message = "L'état du véhicule ne peut pas être nul")
    private VehicleStateEnum state;

    // Conversion DTO --> Entity
    public static Vehicle convert(final VehicleInput input) {
        return Vehicle.builder()
                .brand(input.getBrand())
                .model(input.getModel())
                .engineType(input.getEngineType())
                .color(input.getColor())
                .licensePlate(input.getLicensePlate())
                .acquisitionDate(input.getAcquisitionDate())
                .state(input.getState())
                .build();
    }
}

