package com.imt.adaptersoutbdd.clients;

import com.imt.adaptersoutbdd.clients.repositories.ClientRepository;
import com.imt.adaptersoutbdd.clients.repositories.entities.ClientEntity;
import com.imt.adaptersoutbdd.clients.repositories.mappers.ClientBddMapper;
import com.imt.clients.ClientStorageProvider;
import com.imt.clients.model.Client;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service d'accès à la base de données.
 * Implémente le port de sortie du domaine 'ClientStorageProvider'.
 */
@Service
@AllArgsConstructor
public class ClientsBddService implements ClientStorageProvider {

    private final ClientRepository repository;
    private final ClientBddMapper mapper;

    @Override
    public boolean exist(String id) {
        return repository.existsById(id);
    }

    @Override
    public Collection<Client> getAll() {
        return repository.findAll()
                .stream()
                .map(mapper::from)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Client> get(String id) {
        return repository.findById(id)
                .map(mapper::from);
    }

    @Override
    public Client save(Client client) {
        ClientEntity entityToSave = mapper.to(client);
        ClientEntity savedEntity = repository.save(entityToSave);

        return mapper.from(savedEntity);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    // --- Méthodes spécifiques demandées par les validateurs ---

    @Override
    public Optional<Client> findByLicenseNumber(String licenseNumber) {
        return repository.findByLicenseNumber(licenseNumber)
                .map(mapper::from);
    }

    @Override
    public Optional<Client> findByLastNameAndFirstNameAndBirthDate(String lastName, String firstName, LocalDate birthDate) {
        return repository.findByLastNameAndFirstNameAndDateOfBirth(lastName, firstName, birthDate)
                .map(mapper::from);
    }
}
