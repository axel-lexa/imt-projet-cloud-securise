package com.imt.adaptersoutbdd.vehicle;

import com.imt.adaptersoutbdd.vehicle.repositories.VehicleRepository;
import com.imt.adaptersoutbdd.vehicle.repositories.mappers.VehicleBddMapper;
import com.imt.vehicle.VehicleStorageProvider;
import com.imt.vehicle.model.Vehicle;
import lombok.AllArgsConstructor; // <--- AJOUT
import org.springframework.stereotype.Service; // <--- AJOUT

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service // <--- Indispensable pour être détecté par le scanBasePackages de Application.java
@AllArgsConstructor // <--- Indispensable pour injecter le repository et le mapper
public class VehicleBddService implements VehicleStorageProvider {

    private final VehicleRepository vehicleRepository;
    private final VehicleBddMapper vehicleBddMapper;

    @Override
    public boolean exist(String identifier) {
        return vehicleRepository.existsById(identifier.toString());
    }

    @Override
    public Collection<Vehicle> getAll() {
        return vehicleRepository.findAll()
                .stream()
                .map(vehicleBddMapper::from)
                .toList();
    }

    @Override
    public Optional<Vehicle> get(String id) {
        return vehicleRepository.findById(id)
                .map(vehicleBddMapper::from);
    }

    @Override
    public Vehicle save(final Vehicle vehicle) {
        return this.vehicleBddMapper.from(
                this.vehicleRepository.save(
                        this.vehicleBddMapper.to(vehicle)
                )
        );
    }

    @Override
    public void delete(String identifier) {
        vehicleRepository.deleteById(identifier);
    }

    @Override
    public Optional<Vehicle> getByLicensePlate(String licensePlate) {
        return Optional.ofNullable(licensePlate)
                .flatMap(this.vehicleRepository::findByLicensePlate)
                .map(this.vehicleBddMapper::from);
    }
}