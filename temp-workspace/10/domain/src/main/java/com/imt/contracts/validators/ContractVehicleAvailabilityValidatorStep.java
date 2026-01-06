package com.imt.contracts.validators;

import com.imt.common.exceptions.ConflictException;
import com.imt.common.exceptions.ImtException;
import com.imt.common.validators.AbstractValidatorStep;
import com.imt.contracts.ContractStorageProvider;
import com.imt.contracts.model.Contract;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@AllArgsConstructor
public class ContractVehicleAvailabilityValidatorStep extends AbstractValidatorStep<Contract> {

    private final ContractStorageProvider service;

    @Override
    public void check(final Contract toValidate) throws ImtException {
        final Collection<Contract> existingContracts = Objects.requireNonNullElse(
                this.service.findByVehicleIdentifier(toValidate.getVehicleIdentifier()),
                Collections.emptySet()
        );

        for (final Contract existingContract : existingContracts) {
            if (existingContract.getIdentifier().equals(toValidate.getIdentifier())) {
                continue;
            }

            if (!existingContract.getState().locksVehicle()) {
                continue;
            }

            if (periodsOverlap(
                    existingContract.getStartDate(),
                    existingContract.getEndDate(),
                    toValidate.getStartDate(),
                    toValidate.getEndDate()
            )) {
                throw new ConflictException(
                        String.format(
                                "Le véhicule est déjà réservé par le contrat %s sur la période demandée (%s - %s).",
                                existingContract.getIdentifier(),
                                existingContract.getStartDate(),
                                existingContract.getEndDate()
                        )
                );
            }
        }
    }

    private static boolean periodsOverlap(final LocalDate start1,
                                          final LocalDate end1,
                                          final LocalDate start2,
                                          final LocalDate end2) {
        return !end1.isBefore(start2) && !end2.isBefore(start1);
    }
}


