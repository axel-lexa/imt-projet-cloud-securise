package com.imt.contracts.validators;

import com.imt.common.exceptions.BadRequestException;
import com.imt.common.exceptions.ImtException;
import com.imt.common.validators.AbstractValidatorStep;
import com.imt.contracts.model.Contract;
import com.imt.contracts.model.ContractStateEnum;

public class ContractStateValidatorStep extends AbstractValidatorStep<Contract> {

    @Override
    public void check(final Contract toValidate) throws ImtException {
        if (ContractStateEnum.UNKNOWN.equals(toValidate.getState())) {
            throw new BadRequestException(
                    String.format(
                            "Le statut communiqué ne correspond pas aux valeurs acceptables : %s",
                            ContractStateEnum.ACCEPTABLE_VALUES
                    )
            );
        }
    }
}


