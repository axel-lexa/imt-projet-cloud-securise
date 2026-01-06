package com.imt.clients.validators;

import com.imt.clients.ClientStorageProvider;
import com.imt.clients.model.Client;
import com.imt.common.exceptions.ConflictException;
import com.imt.common.exceptions.ImtException;
import com.imt.common.validators.AbstractValidatorStep;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class ClientUnicityValidatorStep extends AbstractValidatorStep<Client> {
    protected ClientStorageProvider service;

    @Override
    public void check(Client toValidate) throws ImtException {
        Optional<Client> existing = service.findByLastNameAndFirstNameAndBirthDate(
                toValidate.getLastName(), toValidate.getFirstName(), toValidate.getDateOfBirth()
        );

        // Si un client existe ET que ce n'est pas le même (ID différents), alors conflit
        if (existing.isPresent() && !existing.get().getId().equals(toValidate.getId())) {
            throw new ConflictException(String.format(
                    "A client with name '%s %s' and birth date '%s' already exists.",
                    toValidate.getFirstName(),
                    toValidate.getLastName(),
                    toValidate.getDateOfBirth()
            ));
        }
    }
}