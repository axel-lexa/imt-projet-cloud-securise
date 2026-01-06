package com.imt.clients.validators;

import com.imt.clients.ClientStorageProvider;
import com.imt.clients.model.Client;
import com.imt.common.exceptions.ConflictException;
import com.imt.common.exceptions.ImtException;
import com.imt.common.validators.AbstractValidatorStep;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class ClientUnicityLicenseValidatorStep extends AbstractValidatorStep<Client> {
    protected ClientStorageProvider service;

    @Override
    public void check(Client toValidate) throws ImtException {
        Optional<Client> existing = service.findByLicenseNumber(toValidate.getLicenseNumber());

        // Si un permis existe ET que ce n'est pas celui du client actuel, alors conflit
        if (existing.isPresent() && !existing.get().getId().equals(toValidate.getId())) {
            throw new ConflictException(String.format(
                    "A client with license number '%s' already exists.",
                    toValidate.getLicenseNumber()
            ));
        }
    }
}