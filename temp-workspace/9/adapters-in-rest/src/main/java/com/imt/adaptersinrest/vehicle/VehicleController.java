package com.imt.adaptersinrest.vehicle;


import com.imt.adaptersinrest.vehicle.model.input.VehicleInput;
import com.imt.adaptersinrest.vehicle.model.input.VehicleUpdateInput;
import com.imt.adaptersinrest.vehicle.model.output.VehicleOutput;
import com.imt.common.exceptions.ImtException;
import com.imt.contracts.ContractsServiceValidator;
import com.imt.vehicle.VehicleServiceValidator;
import com.imt.vehicle.model.VehicleStateEnum;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST Controller for vehicle management.
 * Entry point: /api/imt/v1/vehicles
 */
@RestController
@RequestMapping("api/v1/vehicles")
public class VehicleController {

    private final VehicleServiceValidator vehicleService;
    private final ContractsServiceValidator contractsService;

    public VehicleController(VehicleServiceValidator vehicleService, ContractsServiceValidator contractsService) {
        this.vehicleService = vehicleService;
        this.contractsService = contractsService;
    }


    // Get all vehicles
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Collection<VehicleOutput>> getAll() {
        Collection<VehicleOutput> dtos = vehicleService.getAll()
                .stream()
                .map(VehicleOutput::from) // Utilisation de VehicleOutput::from
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Create a new vehicle
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleOutput create(@RequestBody final VehicleInput input) throws ImtException {
        return VehicleOutput.from(vehicleService.create(VehicleInput.convert(input)));
    }

    // Get a vehicle by ID
    @GetMapping(value = "/{vehicleId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public VehicleOutput getOne(@PathVariable String vehicleId) {
        return vehicleService.getOne(vehicleId)
                .map(VehicleOutput::from)
                .orElseThrow(() -> new NoSuchElementException("Vehicule non trouvé"));
    }

//    // Update a vehicle
//    @PatchMapping(value = "/{vehicleId}", consumes = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void update(@PathVariable String vehicleId, @RequestBody VehicleUpdateInput input) throws ImtException {
//        vehicleService.update(
//                vehicleService.getOne(UUID.fromString(vehicleId)).map(
//                        alreadySaved -> VehicleUpdateInput.from(input, alreadySaved)
//                ).orElseThrow(() -> new NoSuchElementException("Vehicle does not exist."))
//        );
//    }

    // Delete a vehicle
    @DeleteMapping(value = "/{vehicleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String vehicleId) throws ImtException {
        vehicleService.delete(vehicleId);
    }

    // Update a vehicle with business rule enforcement
    @PatchMapping(value = "/{vehicleId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable String vehicleId, @RequestBody VehicleUpdateInput input) throws ImtException {


        // 1. Mise à jour du véhicule
        vehicleService.update(
                vehicleService.getOne(vehicleId).map(
                        alreadySaved -> VehicleUpdateInput.from(input, alreadySaved)
                ).orElseThrow(() -> new NoSuchElementException("Vehicule non trouvé."))
        );

        // 2. Règle Métier : Si l'état a été modifié ET qu'il est "En panne" (BROKEN)
        // Grâce à la modification de l'étape 1, isUpdated() est maintenant accessible
        if (input.getState().isUpdated() && input.getState().getValue() == VehicleStateEnum.BROKEN) {
            contractsService.cancelContractsForBrokenVehicule(UUID.fromString(vehicleId));
        }
    }
}

