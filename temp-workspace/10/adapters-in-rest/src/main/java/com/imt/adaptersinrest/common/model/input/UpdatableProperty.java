package com.imt.adaptersinrest.common.model.input;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.function.Function;

/**
 * Wrapper pour les propriétés de mise à jour (PATCH).
 * Il distingue une propriété non fournie (updated=false) d'une propriété fournie (updated=true),
 * même si la valeur est 'null'.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdatableProperty<T extends Serializable> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

//    @Getter(AccessLevel.PROTECTED)
    @EqualsAndHashCode.Exclude
    private final boolean updated;
    private final T value;

    public static <T extends Serializable> UpdatableProperty<T> empty() {
        return new UpdatableProperty<>(false, null);
    }

    public static <T extends Serializable> UpdatableProperty<T> makesChanges(final T newValue) {
        return new UpdatableProperty<>(true, newValue);
    }

    /**
     * Retourne la nouvelle valeur si 'updated' est true, sinon retourne la valeur par défaut.
     */
    public T defaultIfNotOverwrite(final T defaultValue) {
        return this.isUpdated()
                ? this.getValue()
                : defaultValue;
    }

    @Override
    public String toString() {
        return this.isUpdated()
                ? String.format("UpdatableProperty[%s]", this.getValue())
                : "UpdatableProperty.empty";
    }
}