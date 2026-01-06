package com.imt.adaptersoutbdd.vehicle.repositories;

import com.imt.adaptersoutbdd.vehicle.repositories.entities.VehicleEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends MongoRepository<VehicleEntity, String> {

    Optional<VehicleEntity> findByLicensePlate(String LicensePlate);
    Optional<VehicleEntity> findById(UUID id);
}