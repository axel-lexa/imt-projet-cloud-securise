package com.imt.adaptersinrest.vehicle;

import com.imt.adaptersinrest.common.model.input.UpdatableProperty;
import com.imt.adaptersinrest.vehicle.model.input.VehicleInput;
import com.imt.adaptersinrest.vehicle.model.input.VehicleUpdateInput;
import com.imt.adaptersinrest.vehicle.model.output.VehicleOutput;
import com.imt.common.exceptions.BadRequestException;
import com.imt.common.exceptions.ConflictException;
import com.imt.common.exceptions.ImtException;
import com.imt.contracts.ContractsServiceValidator;
import com.imt.vehicle.VehicleServiceValidator;
import com.imt.vehicle.model.EngineTypeEnum;
import com.imt.vehicle.model.Vehicle;
import com.imt.vehicle.model.VehicleStateEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleController - Tests unitaires")
class VehicleControllerTest {

    @Mock
    private VehicleServiceValidator vehicleService;

    @Mock
    private ContractsServiceValidator contractsService;

    private VehicleController vehicleController;

    private Vehicle vehicleDomain;
    private VehicleInput vehicleInput;
    private final String vehicleId = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        // Instanciation manuelle du contrôleur
        vehicleController = new VehicleController(vehicleService, contractsService);

        // Données de test
        vehicleInput = new VehicleInput();
        vehicleInput.setBrand("Renault");
        vehicleInput.setModel("Clio");
        vehicleInput.setLicensePlate("AA-123-BB");
        vehicleInput.setEngineType(EngineTypeEnum.GASOLINE);
        vehicleInput.setColor("Blue");
        vehicleInput.setAcquisitionDate(LocalDate.now());
        vehicleInput.setState(VehicleStateEnum.AVAILABLE);

        vehicleDomain = Vehicle.builder()
                .id(vehicleId)
                .brand("Renault")
                .model("Clio")
                .licensePlate("AA-123-BB")
                .engineType(EngineTypeEnum.GASOLINE)
                .color("Blue")
                .acquisitionDate(LocalDate.now())
                .state(VehicleStateEnum.AVAILABLE)
                .build();
    }

    @Nested
    @DisplayName("getAll (GET)")
    class GetAllTests {

        @Test
        @DisplayName("Doit retourner la liste des véhicules (200 OK)")
        void shouldReturnListOfVehicles() {
            // Given
            when(vehicleService.getAll()).thenReturn(List.of(vehicleDomain));

            // When
            ResponseEntity<Collection<VehicleOutput>> response = vehicleController.getAll();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().iterator().next().getIdentifier()).isEqualTo(vehicleId);
            verify(vehicleService).getAll();
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucun véhicule n'existe (200 OK)")
        void shouldReturnEmptyListWhenNoVehicles() {
            // Given
            when(vehicleService.getAll()).thenReturn(Collections.emptyList());

            // When
            ResponseEntity<Collection<VehicleOutput>> response = vehicleController.getAll();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEmpty();
            verify(vehicleService).getAll();
        }
    }

    @Nested
    @DisplayName("getOne (GET)")
    class GetOneTests {

        @Test
        @DisplayName("Doit retourner un véhicule existant")
        void shouldReturnVehicleWhenFound() {
            // Given
            when(vehicleService.getOne(vehicleId)).thenReturn(Optional.of(vehicleDomain));

            // When
            VehicleOutput result = vehicleController.getOne(vehicleId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getIdentifier()).isEqualTo(vehicleId);
            assertThat(result.getBrand()).isEqualTo("Renault");
        }

        @Test
        @DisplayName("Doit lever une exception si le véhicule n'est pas trouvé")
        void shouldThrowExceptionWhenNotFound() {
            // Given
            when(vehicleService.getOne(vehicleId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> vehicleController.getOne(vehicleId))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessage("Vehicule non trouvé");
        }
    }

    @Nested
    @DisplayName("create (POST)")
    class CreateTests {

        @Test
        @DisplayName("Doit créer un véhicule avec succès")
        void shouldCreateVehicleSuccessfully() throws ImtException {
            // Given
            when(vehicleService.create(any(Vehicle.class))).thenReturn(vehicleDomain);

            // When
            VehicleOutput result = vehicleController.create(vehicleInput);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getIdentifier()).isEqualTo(vehicleId);
            assertThat(result.getBrand()).isEqualTo("Renault");
            verify(vehicleService).create(any(Vehicle.class));
        }

        @Test
        @DisplayName("Doit propager ConflictException si la plaque d'immatriculation existe déjà")
        void shouldPropagateConflictExceptionWhenLicensePlateExists() throws ImtException {
            // Given
            String errorMessage = "Un véhicule avec la plaque d'immatriculation 'AA-123-BB' existe déjà.";
            when(vehicleService.create(any(Vehicle.class))).thenThrow(new ConflictException(errorMessage));

            // When & Then
            assertThatThrownBy(() -> vehicleController.create(vehicleInput))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage(errorMessage);
        }

        @Test
        @DisplayName("Doit propager BadRequestException pour un état invalide")
        void shouldPropagateBadRequestExceptionForInvalidState() throws ImtException {
            // Given
            String errorMessage = "L'état communiqué ne correspond pas aux valeurs acceptables.";
            when(vehicleService.create(any(Vehicle.class))).thenThrow(new BadRequestException(errorMessage));

            // When & Then
            assertThatThrownBy(() -> vehicleController.create(vehicleInput))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(errorMessage);
        }
    }

    @Nested
    @DisplayName("delete (DELETE)")
    class DeleteTests {

        @Test
        @DisplayName("Doit supprimer un véhicule avec succès")
        void shouldDeleteVehicleSuccessfully() throws ImtException {
            // Given
            doNothing().when(vehicleService).delete(vehicleId);

            // When
            vehicleController.delete(vehicleId);

            // Then
            verify(vehicleService).delete(vehicleId);
        }
    }

    @Nested
    @DisplayName("update (PATCH)")
    class UpdateTests {

        private VehicleUpdateInput updateInput;

        @BeforeEach
        void setUpUpdate() {
            updateInput = new VehicleUpdateInput();
        }

        @Test
        @DisplayName("Doit mettre à jour un véhicule sans annuler de contrats")
        void shouldUpdateVehicleWithoutCancellingContracts() throws ImtException {
            // Given
            updateInput.setColor(UpdatableProperty.makesChanges("Red"));
            when(vehicleService.getOne(vehicleId)).thenReturn(Optional.of(vehicleDomain));
            doNothing().when(vehicleService).update(any(Vehicle.class));

            // When
            vehicleController.update(vehicleId, updateInput);

            // Then
            verify(vehicleService).getOne(vehicleId);
            verify(vehicleService).update(any(Vehicle.class));
            verifyNoInteractions(contractsService);
        }

        @Test
        @DisplayName("Doit mettre à jour un véhicule et annuler les contrats si l'état est BROKEN")
        void shouldUpdateVehicleAndCancelContractsWhenStateIsBroken() throws ImtException {
            // Given
            updateInput.setState(UpdatableProperty.makesChanges(VehicleStateEnum.BROKEN));
            when(vehicleService.getOne(vehicleId)).thenReturn(Optional.of(vehicleDomain));
            doNothing().when(vehicleService).update(any(Vehicle.class));
            doNothing().when(contractsService).cancelContractsForBrokenVehicule(any(UUID.class));

            // When
            vehicleController.update(vehicleId, updateInput);

            // Then
            verify(vehicleService).getOne(vehicleId);
            verify(vehicleService).update(any(Vehicle.class));
            verify(contractsService).cancelContractsForBrokenVehicule(UUID.fromString(vehicleId));
        }

        @Test
        @DisplayName("Ne doit pas annuler les contrats si l'état n'est pas mis à jour à BROKEN")
        void shouldNotCancelContractsWhenStateIsNotUpdatedToBroken() throws ImtException {
            // Given
            updateInput.setState(UpdatableProperty.makesChanges(VehicleStateEnum.AVAILABLE));
            when(vehicleService.getOne(vehicleId)).thenReturn(Optional.of(vehicleDomain));
            doNothing().when(vehicleService).update(any(Vehicle.class));

            // When
            vehicleController.update(vehicleId, updateInput);

            // Then
            verify(vehicleService).update(any(Vehicle.class));
            verifyNoInteractions(contractsService);
        }

        @Test
        @DisplayName("Doit lever une exception si le véhicule à mettre à jour n'existe pas")
        void shouldThrowExceptionWhenVehicleToUpdateNotFound() throws ImtException {
            // Given
            updateInput.setColor(UpdatableProperty.makesChanges("Red"));
            when(vehicleService.getOne(vehicleId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> vehicleController.update(vehicleId, updateInput))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessage("Vehicule non trouvé.");

            verify(vehicleService, never()).update(any(Vehicle.class));
            verifyNoInteractions(contractsService);
        }
    }
}
