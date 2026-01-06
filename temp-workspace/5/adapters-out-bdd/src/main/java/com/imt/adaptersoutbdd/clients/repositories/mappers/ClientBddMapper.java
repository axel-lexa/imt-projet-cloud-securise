package com.imt.adaptersoutbdd.clients.repositories.mappers;

import com.imt.adaptersoutbdd.clients.repositories.entities.ClientEntity;
import com.imt.adaptersoutbdd.common.model.mappers.AbstractBddMapper;
import com.imt.clients.model.Client;
import org.springframework.stereotype.Component;

@Component
public class ClientBddMapper extends AbstractBddMapper<Client, ClientEntity> {

    @Override
    public Client from(ClientEntity input) {
        if (input == null) return null;

        return Client.builder()
                .id(input.getId())
                .firstName(input.getFirstName())
                .lastName(input.getLastName())
                .dateOfBirth(input.getDateOfBirth())
                .licenseNumber(input.getLicenseNumber())
                .address(input.getAddress())
                .build();
    }

    @Override
    public ClientEntity to(Client object) {
        if (object == null) return null;

        ClientEntity entity = new ClientEntity();
        entity.setId(object.getId());
        entity.setFirstName(object.getFirstName());
        entity.setLastName(object.getLastName());
        entity.setDateOfBirth(object.getDateOfBirth());
        entity.setLicenseNumber(object.getLicenseNumber());
        entity.setAddress(object.getAddress());
        return entity;
    }
}
