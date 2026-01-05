package com.imt.adaptersinrest.clients.model.input;

import com.imt.adaptersinrest.common.model.input.AbstractInput;
import com.imt.clients.model.Client;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO pour la création (POST) d'un client.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class ClientInput extends AbstractInput {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Last name cannot be null")
    @Pattern(regexp = "^[a-zA-Z- ]{2,100}$", message = "Last name is invalid")
    private String lastName;

    @NotNull(message = "First name cannot be null")
    @Pattern(regexp = "^[a-zA-Z- ]{2,100}$", message = "First name is invalid")
    private String firstName;

    @NotNull(message = "Date of birth cannot be null")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "License number cannot be null")
    @Pattern(regexp = "^[A-Z0-9]{1,15}$", message = "License number pattern is invalid")
    private String licenseNumber;

    @NotNull(message = "Address cannot be null")
    @Size(min = 5, max = 255, message = "Address must be between 5 and 255 characters")
    private String address;

    // Conversion DTO --> Domain
    public static Client convert(final ClientInput input) {
        return Client.builder()
                .lastName(input.getLastName())
                .firstName(input.getFirstName())
                .dateOfBirth(input.getDateOfBirth())
                .licenseNumber(input.getLicenseNumber())
                .address(input.getAddress())
                .build();
    }
}