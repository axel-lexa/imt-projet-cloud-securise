package com.imt.vehicle.model;

import lombok.Getter;
import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
public enum EngineTypeEnum {
    GASOLINE("Essence"),
    DIESEL("Diesel"),
    ELECTRIC("Électrique"),
    HYBRID("Hybride"),
    PLUG_IN_HYBRID("Hybride rechargeable"),
    UNKNOWN("Inconnu");

    public static final String ACCEPTABLE_VALUES = Arrays.stream(EngineTypeEnum.values())
            .filter(type -> !UNKNOWN.equals(type))
            .map(EngineTypeEnum::getLabel)
            .collect(Collectors.joining(", "));

    private final String label;

    EngineTypeEnum(final String label) {
        this.label = label;
    }
}
