package com.imt.config;

import com.imt.clients.ClientStorageProvider;
import com.imt.clients.ClientsServiceValidator;
import com.imt.contracts.ContractStorageProvider;
import com.imt.contracts.ContractsServiceValidator;
import com.imt.vehicle.VehicleServiceValidator;
import com.imt.vehicle.VehicleStorageProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration Spring pour instancier les services du Domaine.
 * Le module 'domain' ne contenant pas de Spring, nous devons déclarer nous-mêmes
 * ses classes comme des Beans pour pouvoir les injecter dans nos contrôleurs.
 */
@Configuration
public class BeanConfiguration {

    /**
     * Expose le service de gestion des clients (avec validation).
     * Ce Bean sera injecté partout où ClientsService ou ClientsServiceValidator est demandé.
     */
    @Bean
    public ClientsServiceValidator clientsServiceValidator(final ClientStorageProvider clientStorageProvider) {
        return new ClientsServiceValidator(clientStorageProvider);
    }

    /**
     * Expose le service de gestion des véhicules (avec validation).
     */
    @Bean
    public VehicleServiceValidator vehicleServiceValidator(final VehicleStorageProvider vehicleStorageProvider) {
        return new VehicleServiceValidator(vehicleStorageProvider);
    }

    /**
     * Expose le service de gestion des contrats (avec validation).
     */
    @Bean
    public ContractsServiceValidator contractsServiceValidator(final ContractStorageProvider contractStorageProvider) {
        return new ContractsServiceValidator(contractStorageProvider);
    }
}