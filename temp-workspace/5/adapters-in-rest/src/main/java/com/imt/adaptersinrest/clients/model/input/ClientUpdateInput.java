package com.imt.adaptersinrest.clients.model.input;

import com.imt.adaptersinrest.common.model.input.AbstractUpdateInput;
import com.imt.adaptersinrest.common.model.input.UpdatableProperty;
import com.imt.clients.model.Client;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.time.LocalDate;

/**
 * DTO pour la mise à jour partielle (PATCH) d'un client.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ClientUpdateInput extends AbstractUpdateInput {
    @Serial
    private static final long serialVersionUID = 1L;

    private UpdatableProperty<String> lastName = UpdatableProperty.empty();
    private UpdatableProperty<String> firstName = UpdatableProperty.empty();
    private UpdatableProperty<LocalDate> dateOfBirth = UpdatableProperty.empty();
    private UpdatableProperty<String> address = UpdatableProperty.empty();

    // PATCH conversion
    public static Client from(final ClientUpdateInput input, final Client existingClient) {
        return existingClient.toBuilder()
                .lastName(input.getLastName().defaultIfNotOverwrite(existingClient.getLastName()))
                .firstName(input.getFirstName().defaultIfNotOverwrite(existingClient.getFirstName()))
                .dateOfBirth(input.getDateOfBirth().defaultIfNotOverwrite(existingClient.getDateOfBirth()))
                .address(input.getAddress().defaultIfNotOverwrite(existingClient.getAddress()))
                .build();
    }
}