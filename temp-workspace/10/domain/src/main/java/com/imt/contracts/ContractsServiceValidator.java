package com.imt.contracts;

import com.imt.common.exceptions.ImtException;
import com.imt.common.validators.ConstraintValidatorStep;
import com.imt.contracts.model.Contract;
import com.imt.contracts.validators.ContractPeriodValidatorStep;
import com.imt.contracts.validators.ContractStateValidatorStep;
import com.imt.contracts.validators.ContractVehicleAvailabilityValidatorStep;
import com.imt.contracts.validators.ContractVehicleReadinessValidatorStep;

public class ContractsServiceValidator extends ContractsService {

    public ContractsServiceValidator(final ContractStorageProvider service) {
        super(service);
    }

    public Contract create(final Contract newContract) throws ImtException {
        new ConstraintValidatorStep<Contract>()
                .linkWith(new ContractStateValidatorStep())
                .linkWith(new ContractPeriodValidatorStep())
                .linkWith(new ContractVehicleReadinessValidatorStep())
                .linkWith(new ContractVehicleAvailabilityValidatorStep(this.service))
                .validate(newContract)
                .throwIfInvalid();

        return super.create(newContract);
    }

    public void update(final Contract contractToUpdate) throws ImtException {
        new ConstraintValidatorStep<Contract>()
                .linkWith(new ContractStateValidatorStep())
                .linkWith(new ContractPeriodValidatorStep())
                .linkWith(new ContractVehicleReadinessValidatorStep())
                .linkWith(new ContractVehicleAvailabilityValidatorStep(this.service))
                .validate(contractToUpdate)
                .throwIfInvalid();

        super.update(contractToUpdate);
    }
}


