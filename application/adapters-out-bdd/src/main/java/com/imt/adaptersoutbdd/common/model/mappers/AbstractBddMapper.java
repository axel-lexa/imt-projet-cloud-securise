package com.imt.adaptersoutbdd.common.model.mappers;

/**
 * Classe abstraite pour les mappers entre objets métier et entités de base de données.
 * Définit le contrat pour la conversion bidirectionnelle.
 *
 * @param <T> le type de l'objet métier (Domain)
 * @param <E> le type de l'entité de base de données (Entity)
 */
public abstract class AbstractBddMapper<T, E> {
    /**
     * Convertit une entité de base de données en objet métier.
     */
    public abstract T from(final E input);

    /**
     * Convertit un objet métier en entité de base de données.
     */
    public abstract E to(final T object);
}
