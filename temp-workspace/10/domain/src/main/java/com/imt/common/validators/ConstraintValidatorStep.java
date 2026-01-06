package com.imt.common.validators;

import com.imt.common.exceptions.BadRequestException;
import com.imt.common.exceptions.ImtException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validateur pour les contraintes Jakarta Validation.
 * Vérifie que toutes les annotations de validation (@NotNull, @Pattern, etc.) sont respectées.
 *
 * @param <T> le type d'objet à valider
 */
public class ConstraintValidatorStep<T> extends AbstractValidatorStep<T> {

    /**
     * Vérifie que toutes les contraintes de validation sur l'objet sont respectées.
     *
     * @param toValidate l'objet à valider
     * @throws BadRequestException si au moins une contrainte n'est pas respectée
     */
    @Override
    public void check(final T toValidate) throws ImtException {
        try (final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            final Set<ConstraintViolation<T>> violations = validatorFactory.getValidator().validate(toValidate);

            if (!violations.isEmpty()) {
                throw new BadRequestException(String.format(
                        "At least one constraint violation found: %s",
                        violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", ")))
                );
            }
        }
    }
}
