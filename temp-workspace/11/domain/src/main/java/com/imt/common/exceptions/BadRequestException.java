package com.imt.common.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serial;

/**
 * Exception levée lorsqu'une requête est mal formée ou invalide.
 * Utilisée notamment pour les erreurs de validation de contraintes.
 */
@Getter
@RequiredArgsConstructor
@ToString
public class BadRequestException extends ImtException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Message d'erreur décrivant le problème de validation
     */
    private final String message;
}
