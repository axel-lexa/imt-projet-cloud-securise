package com.imt.common.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serial;

/**
 * Exception levée lorsqu'il y a un conflit avec l'état actuel de la ressource.
 * Utilisée notamment lorsqu'un client déjà.
 */
@Getter
@RequiredArgsConstructor
@ToString
public class ConflictException extends ImtException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Message d'erreur décrivant le conflit
     */
    private final String message;
}
