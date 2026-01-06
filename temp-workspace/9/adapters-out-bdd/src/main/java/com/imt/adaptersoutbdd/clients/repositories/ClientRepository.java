package com.imt.adaptersoutbdd.clients.repositories;

import com.imt.adaptersoutbdd.clients.repositories.entities.ClientEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ClientRepository extends MongoRepository<ClientEntity, String> {

    /**
     * Méthode magique pour trouver par numéro de permis
     * Spring Data traduit ça en : db.clients.find({ licenseNumber: ? })
     *
     * @param licenseNumber
     * @return
     */
    Optional<ClientEntity> findByLicenseNumber(String licenseNumber);

    /**
     * Méthode magique pour trouver par unicité personne
     * db.clients.find({ lastName: ?, firstName: ?, dateOfBirth: ? })
     *
     * @param lastName
     * @param firstName
     * @param dateOfBirth
     * @return
     */
    Optional<ClientEntity> findByLastNameAndFirstNameAndDateOfBirth(String lastName, String firstName, LocalDate dateOfBirth);
}
