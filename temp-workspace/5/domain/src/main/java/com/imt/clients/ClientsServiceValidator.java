package com.imt.clients;

import com.imt.clients.model.Client;
import com.imt.clients.validators.ClientUnicityLicenseValidatorStep;
import com.imt.clients.validators.ClientUnicityValidatorStep;
import com.imt.common.exceptions.ImtException;
import com.imt.common.validators.ConstraintValidatorStep;

/**
 * Service métier de gestion des clients avec validation.
 * Étend ClientsService en ajoutant des validations métier avant les opérations de création et modification.
 * Utilise le pattern Chain of Responsibility pour enchaîner les validations.
 */
public class ClientsServiceValidator extends ClientsService {

    /**
     * Constructeur du service de validation des clients.
     *
     * @param service le service d'accès à la base de données pour les clients
     */
    public ClientsServiceValidator(final ClientStorageProvider service) {
        super(service);
    }

    /**
     * Crée un nouveau client après validation.
     * Valide que :
     * - Les contraintes sur l'objet sont respectées
     * - Le client est unique (nom, prénom, date de naissance)
     * - Le numéro de licence est unique
     *
     * @param client le client à créer
     * @return le client créé
     * @throws com.imt.common.exceptions.BadRequestException si les contraintes ne sont pas respectées
     * @throws com.imt.common.exceptions.ConflictException   si un client similaire existe déjà
     */
    public Client create(final Client client) throws ImtException {
        new ConstraintValidatorStep<Client>()
                .linkWith(new ClientUnicityValidatorStep(this.service))
                .linkWith(new ClientUnicityLicenseValidatorStep(this.service))
                .validate(client)
                .throwIfInvalid();

        return super.create(client);
    }

    /**
     * Met à jour un client existant après validation.
     * Valide que les contraintes sur l'objet sont respectées.
     *
     * @param client
     * @return
     * @throws ImtException
     */
    public Client update(final Client client) throws ImtException {
        new ConstraintValidatorStep<Client>()
                // On réactive les règles d'unicité ici
                .linkWith(new ClientUnicityValidatorStep(this.service))
                .linkWith(new ClientUnicityLicenseValidatorStep(this.service))
                .validate(client)
                .throwIfInvalid();

        super.update(client);
        return client;
    }
}
