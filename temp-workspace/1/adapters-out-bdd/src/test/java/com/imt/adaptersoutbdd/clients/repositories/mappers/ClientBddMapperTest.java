package com.imt.adaptersoutbdd.clients.repositories.mappers;

import com.imt.adaptersoutbdd.clients.repositories.entities.ClientEntity;
import com.imt.clients.model.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientBddMapper - Tests unitaires")
class ClientBddMapperTest {

    private ClientBddMapper clientMapper;

    @BeforeEach
    void setUp() {
        clientMapper = new ClientBddMapper();
    }

    @Test
    @DisplayName("from() - Doit convertir Entity vers Domain")
    void from_shouldConvertEntityToBusinessObject() {
        ClientEntity entity = ClientEntity.builder()
                .id("mongo-id-123")
                .lastName("Doe")
                .firstName("John")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .licenseNumber("AB123CD")
                .address("1 Rue de la Paix")
                .build();

        // When
        Client result = clientMapper.from(entity);

        // Then
        assertNotNull(result);
        assertEquals("mongo-id-123", result.getId());
        assertEquals("Doe", result.getLastName());
        assertEquals("John", result.getFirstName());
        assertEquals(LocalDate.of(1990, 1, 1), result.getDateOfBirth());
        assertEquals("AB123CD", result.getLicenseNumber());
        assertEquals("1 Rue de la Paix", result.getAddress());
    }

    @Test
    @DisplayName("from() - Doit retourner null si l'entrée est null")
    void from_shouldReturnNull_whenInputIsNull() {
        assertNull(clientMapper.from(null));
    }

    @Test
    @DisplayName("to() - Doit convertir Domain vers Entity")
    void to_shouldConvertBusinessObjectToEntity() {
        // Given
        Client client = Client.builder()
                .id("domain-id-456")
                .lastName("Martin")
                .firstName("Alice")
                .dateOfBirth(LocalDate.of(1985, 5, 20))
                .licenseNumber("XY987ZT")
                .address("10 Avenue Foch")
                .build();

        // When
        ClientEntity result = clientMapper.to(client);

        // Then
        assertNotNull(result);
        assertEquals("domain-id-456", result.getId());
        assertEquals("Martin", result.getLastName());
        assertEquals("Alice", result.getFirstName());
        assertEquals(LocalDate.of(1985, 5, 20), result.getDateOfBirth());
        assertEquals("XY987ZT", result.getLicenseNumber());
        assertEquals("10 Avenue Foch", result.getAddress());
    }

    @Test
    @DisplayName("to() - Doit retourner null si l'entrée est null")
    void to_shouldReturnNull_whenInputIsNull() {
        assertNull(clientMapper.to(null));
    }
}
