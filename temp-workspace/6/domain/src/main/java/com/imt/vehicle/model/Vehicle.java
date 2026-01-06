package com.imt.vehicle.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Vehicle {

    private static final String BRAND_MODEL_PATTERN = "^[a-zA-Z0-9 -]{1,100}$";
    private static final String LICENSE_PLATE_PATTERN = "^[A-Z]{2}-[0-9]{3}-[A-Z]{2}$";

    @Builder.Default
    @NotNull(message = "L'identifiant ne peut pas être nul")
    private final String id = UUID.randomUUID().toString();

    @NotNull(message = "La marque ne peut pas être nulle")
    @Pattern(regexp = BRAND_MODEL_PATTERN, message = "La marque n'est pas valide : elle doit être composée de lettres, chiffres, espaces et tirets et faire entre 1 et 100 caractères")
    private final String brand;

    @NotNull(message = "Le modèle ne peut pas être nul")
    @Pattern(regexp = BRAND_MODEL_PATTERN, message = "Le modèle n'est pas valide : il doit être composé de lettres, chiffres, espaces et tirets et faire entre 1 et 100 caractères")
    private final String model;

    @NotNull(message = "La motorisation ne peut pas être nulle")
    private final EngineTypeEnum engineType;

    @NotNull(message = "La couleur ne peut pas être nulle")
    @Pattern(regexp = "^[a-zA-Z ]{1,50}$", message = "La couleur n'est pas valide : elle doit être composée uniquement de lettres et d'espaces et faire entre 1 et 50 caractères")
    private final String color;

    @NotNull(message = "Le numéro d'immatriculation ne peut pas être nul")
    @Pattern(regexp = LICENSE_PLATE_PATTERN, message = "Le numéro d'immatriculation n'est pas valide : format attendu AA-123-AA")
    private final String licensePlate;

    @NotNull(message = "La date d'acquisition ne peut pas être nulle")
    private final LocalDate acquisitionDate;

    @NotNull(message = "L'état ne peut pas être nul")
    private final VehicleStateEnum state;
}

