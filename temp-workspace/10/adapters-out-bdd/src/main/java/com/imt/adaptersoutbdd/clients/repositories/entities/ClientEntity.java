package com.imt.adaptersoutbdd.clients.repositories.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

/**
 * Entité MongoDB représentant un client.
 * Stockée dans la collection "clients".
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "clients")
public class ClientEntity {
    @Id
    private String id;

    private String lastName;
    private String firstName;
    private LocalDate dateOfBirth;
    private String licenseNumber;
    private String address;
}
