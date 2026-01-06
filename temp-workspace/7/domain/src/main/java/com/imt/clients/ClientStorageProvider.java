package com.imt.clients;

import com.imt.clients.model.Client;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ClientStorageProvider {
    /**
     * Vérifie si un client existe.
     *
     * @param id l'identifiant du client
     * @return true si le client existe, false sinon
     */
    boolean exist(final String id);

    /**
     * Récupère tous les clients stockés.
     *
     * @return une collection de tous les clients, ou une collection vide si aucun client n'existe
     */
    Collection<Client> getAll();

    /**
     * Récupère un client spécifique par son identifiant.
     *
     * @param id l'identifiant unique du client
     * @return un Optional contenant le client s'il existe, Optional.empty() sinon
     */
    Optional<Client> get(final String id);

    /**
     * Sauvegarde un client (création ou mise à jour).
     *
     * @param client le client à sauvegarder
     * @return le client sauvegardé
     * @throws NullPointerException si le client est null
     */
    Client save(final Client client);

    /**
     * Supprime un client du stockage.
     *
     * @param id l'identifiant du client à supprimer
     */
    void delete(final String id);

    /**
     * Recherche un client par son numéro de licence.
     *
     * @param licenseNumber le numéro de licence du client
     */
    Optional<Client> findByLicenseNumber(final String licenseNumber);

    /**
     * Recherche un client par son nom, prénom et date de naissance.
     *
     * @param lastName
     * @param firstName
     * @param birthDate
     */
    Optional<Client> findByLastNameAndFirstNameAndBirthDate(final String lastName, final String firstName, final LocalDate birthDate);
}
