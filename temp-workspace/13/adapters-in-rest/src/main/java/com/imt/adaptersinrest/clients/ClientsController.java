package com.imt.adaptersinrest.clients;

import com.imt.adaptersinrest.clients.model.input.ClientInput;
import com.imt.adaptersinrest.clients.model.input.ClientUpdateInput;
import com.imt.adaptersinrest.clients.model.output.ClientOutput;
import com.imt.clients.ClientsService;
import com.imt.clients.ClientsServiceValidator;
import com.imt.common.exceptions.ImtException;
import com.imt.common.exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Controleur REST Clients
 */
@RestController
@RequestMapping("/api/v1/clients")
public class ClientsController {

    private final ClientsServiceValidator clientsServiceValidator;
    private final ClientsService clientsService;

    public ClientsController(ClientsServiceValidator clientsServiceValidator, ClientsService clientsService){
        this.clientsServiceValidator = clientsServiceValidator;
        this.clientsService = clientsService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClientOutput> create(@Valid @RequestBody ClientInput clientInput)
            throws ImtException {
        // Utilisation de ClientInput.convert
        return new ResponseEntity<>(
                ClientOutput.from(clientsServiceValidator.create(ClientInput.convert(clientInput))),
                HttpStatus.CREATED
        );
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClientOutput> update(
            @PathVariable String id,
            @RequestBody ClientUpdateInput clientUpdateInput
    ) throws ImtException {

        // Logique condensée comme dans VehicleController
        return ResponseEntity.ok(
                ClientOutput.from(
                        clientsServiceValidator.update(
                                clientsService.getOne(id)
                                        .map(existing -> ClientUpdateInput.from(clientUpdateInput, existing))
                                        .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'id " + id))
                        )
                )
        );
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClientOutput> getById(@PathVariable String id) throws ImtException {
        return ResponseEntity.ok(
                clientsService.getOne(id)
                        .map(ClientOutput::from) // Utilisation de ClientOutput::from
                        .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé"))
        );
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<ClientOutput>> getAll() {
        Collection<ClientOutput> dtos = clientsService.getAll()
                .stream()
                .map(ClientOutput::from) // Utilisation de ClientOutput::from
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) throws ImtException {
        clientsService.delete(id);
        return ResponseEntity.noContent().build();
    }
}