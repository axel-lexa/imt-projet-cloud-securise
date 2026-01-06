package com.imt.common.validators;

import com.imt.clients.model.Client;
import com.imt.common.exceptions.ImtException;
import com.imt.common.model.ValidatorResult;

import java.util.Objects;

/**
 * Classe abstraite pour la validation en chaîne (Chain of Responsibility pattern).
 * Permet de lier plusieurs étapes de validation qui s'exécutent séquentiellement.
 *
 * @param <T> le type d'objet à valider
 */
public abstract class AbstractValidatorStep<T> {
    private AbstractValidatorStep<T> nextStep;

    /**
     * Effectue la vérification métier spécifique à cette étape de validation.
     *
     * @param toValidate l'objet à valider
     * @throws RuntimeException si la validation échoue
     */
    public abstract void check(final T toValidate) throws ImtException;

    /**
     * Valide l'objet en exécutant cette étape et toutes les étapes suivantes.
     *
     * @param toValidate l'objet à valider
     * @return le résultat de la validation (valide ou invalide avec l'exception)
     * @throws NullPointerException si toValidate est null
     */
    public ValidatorResult validate(final T toValidate) {
        Objects.requireNonNull(toValidate, "Object to validate cannot be null");

        try {
            this.check(toValidate);
        } catch (final ImtException e) {
            return ValidatorResult.invalid(e);
        }

        if (Objects.nonNull(this.nextStep)) {
            return this.nextStep.validate(toValidate);
        }

        return ValidatorResult.valid();
    }

    /**
     * Lie cette étape de validation avec la suivante pour créer une chaîne.
     *
     * @param nextStep la prochaine étape de validation à exécuter
     * @return cette instance pour permettre le chaînage
     */
    public AbstractValidatorStep<T> linkWith(final AbstractValidatorStep<T> nextStep) {
        if (Objects.isNull(this.nextStep)) {
            this.nextStep = nextStep;
        } else {
            this.nextStep.linkWith(nextStep);
        }

        return this;
    }
}
