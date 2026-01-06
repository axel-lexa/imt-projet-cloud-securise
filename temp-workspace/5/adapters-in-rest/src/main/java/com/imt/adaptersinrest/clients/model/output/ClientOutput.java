package com.imt.adaptersinrest.clients.model.output;

import com.imt.adaptersinrest.common.model.output.AbstractOutput;
import com.imt.clients.model.Client;
import lombok.*;

import java.io.Serial;
import java.time.LocalDate;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class ClientOutput extends AbstractOutput {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String lastName;
    private final String firstName;
    private final LocalDate dateOfBirth;
    private final String licenseNumber;
    private final String address;

    // Domain --> DTO
    public static ClientOutput from(final Client client) {
        return ClientOutput.builder()
                .id(client.getId())
                .lastName(client.getLastName())
                .firstName(client.getFirstName())
                .dateOfBirth(client.getDateOfBirth())
                .licenseNumber(client.getLicenseNumber())
                .address(client.getAddress())
                .build();
    }
}