package com.imt.common.exceptions;

import java.io.Serial;

public abstract class ImtException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Retourne le type de l'exception (nom de la classe).
     *
     * @return le nom simple de la classe d'exception
     */
    public String getType() {
        return this.getClass().getSimpleName();
    }
}
