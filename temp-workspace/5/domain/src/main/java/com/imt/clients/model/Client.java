package com.imt.clients.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)      // Permet de cloner un objet en modifiant certains champs
@EqualsAndHashCode(of = "id")   // Un client est unique par son ID ou son numPermis
@ToString
public class Client {
    /**
     * Pattern pour le prénom et le nom de famille : uniquement des lettres, des espaces et des tirets, entre 2 et 100 caractères.
     */
    private static final String FIRST_NAME_LAST_NAME_PATTERN = "^[a-zA-Z- ]{2,100}$";

    /**
     * Pattern pour le numéro de permis : 15 charactères alphanumériques.
     */
    private static final String LICENSE_NUMBER_PATTERN = "^[A-Z0-9]{1,15}$";

    @Builder.Default
    @NotNull(message = "Id cannot be null")
    private String id = UUID.randomUUID().toString();

    @NotNull(message = "Last name cannot be null")
    @Pattern(regexp = FIRST_NAME_LAST_NAME_PATTERN, message = "Last name must be between 2 and 100 characters and contain only letters, spaces, and hyphens")
    private String lastName;

    @NotNull(message = "First name cannot be null")
    @Pattern(regexp = FIRST_NAME_LAST_NAME_PATTERN, message = "First name must be between 2 and 100 characters and contain only letters, spaces, and hyphens")
    private String firstName;

    @NotNull(message = "Date of birth cannot be null")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "License number cannot be null")
    @Pattern(regexp = LICENSE_NUMBER_PATTERN, message = "License number must be between 1 and 15 alphanumeric characters")
    private String licenseNumber;

    @NotNull(message = "Address cannot be null")
    @Size(min = 5, max = 255, message = "Address must be between 5 and 255 characters")
    private String address;
}
