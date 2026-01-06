package com.imt.vehicle.model;

import lombok.Getter;
import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
public enum VehicleStateEnum {
    AVAILABLE("Disponible"),
    IN_RENTAL("En location"),
    BROKEN("En panne"),
    UNKNOWN("Inconnu");

    public static final String ACCEPTABLE_VALUES = Arrays.stream(VehicleStateEnum.values())
            .filter(state -> !UNKNOWN.equals(state))
            .map(VehicleStateEnum::getLabel)
            .collect(Collectors.joining(", "));

    private final String label;

    VehicleStateEnum(final String label) {
        this.label = label;
    }
}
