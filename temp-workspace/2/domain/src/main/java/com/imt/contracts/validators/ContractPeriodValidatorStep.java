package com.imt.contracts.validators;

import com.imt.common.exceptions.BadRequestException;
import com.imt.common.exceptions.ImtException;
import com.imt.common.validators.AbstractValidatorStep;
import com.imt.contracts.model.Contract;

public class ContractPeriodValidatorStep extends AbstractValidatorStep<Contract> {

    @Override
    public void check(final Contract toValidate) throws ImtException {
        if (toValidate.getStartDate().isAfter(toValidate.getEndDate())) {
            throw new BadRequestException("La date de début doit précéder ou être égale à la date de fin du contrat.");
        }
    }
}


