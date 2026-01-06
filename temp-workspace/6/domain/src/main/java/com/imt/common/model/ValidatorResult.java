package com.imt.common.model;

import com.imt.common.exceptions.BadRequestException;
import com.imt.common.exceptions.ImtException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Classe représentant le résultat d'une validation.
 * Encapsule les informations sur le succès ou l'échec de la validation, et l'exception associée en cas d'échec.
 */
@Getter
@Builder
@EqualsAndHashCode
@ToString
public class ValidatorResult {
    private final boolean isValid;

    private final ImtException exceptionToThrow;

    /**
     * Crée un résultat de validation valide.
     *
     * @return un ValidatorResult marqué comme valide
     */
    public static ValidatorResult valid() {
        return ValidatorResult.builder().isValid(true).build();
    }

    /**
     * Crée un résultat de validation invalide avec un message d'erreur.
     *
     * @param message le message d'erreur de validation
     * @return un ValidatorResult marqué comme invalide avec une BadRequestException
     */
    public static ValidatorResult invalid(final String message) {
        return ValidatorResult
                .builder()
                .isValid(false)
                .exceptionToThrow(new BadRequestException(message))
                .build();
    }

    /**
     * Crée un résultat de validation invalide avec une exception spécifique.
     *
     * @param exceptionToThrow l'exception à lever
     * @return un ValidatorResult marqué comme invalide avec l'exception fournie
     */
    public static ValidatorResult invalid(final ImtException exceptionToThrow) {
        return ValidatorResult
                .builder()
                .isValid(false)
                .exceptionToThrow(exceptionToThrow)
                .build();
    }

    /**
     * Lève l'exception associée si la validation a échoué.
     * Ne fait rien si la validation est valide.
     *
     * @throws ImtException si la validation a échoué
     */
    public void throwIfInvalid() throws ImtException {
        if (!this.isValid()) {
            throw this.exceptionToThrow;
        }
    }
}
