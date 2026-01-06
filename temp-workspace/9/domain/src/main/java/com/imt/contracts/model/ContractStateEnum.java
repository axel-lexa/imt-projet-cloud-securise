package com.imt.contracts.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

@Getter
public enum ContractStateEnum {
    PENDING("En attente"),
    IN_PROGRESS("En cours"),
    COMPLETED("Terminé"),
    LATE("En retard"),
    CANCELLED("Annulé"),
    UNKNOWN("Inconnu");

    public static final String ACCEPTABLE_VALUES = Arrays.stream(ContractStateEnum.values())
            .filter(state -> !UNKNOWN.equals(state))
            .map(ContractStateEnum::getLabel)
            .collect(Collectors.joining(", "));

    private static final EnumSet<ContractStateEnum> LOCKING_STATES = EnumSet.of(PENDING, IN_PROGRESS, LATE);

    private final String label;

    ContractStateEnum(final String label) {
        this.label = label;
    }

    public boolean locksVehicle() {
        return LOCKING_STATES.contains(this);
    }
}


