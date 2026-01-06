package com.imt.adaptersinrest.vehicle.model.input;

import com.imt.adaptersinrest.common.model.input.AbstractUpdateInput;
import com.imt.adaptersinrest.common.model.input.UpdatableProperty;
import com.imt.vehicle.model.EngineTypeEnum;
import com.imt.vehicle.model.Vehicle;
import com.imt.vehicle.model.VehicleStateEnum;
import lombok.*;

import java.io.Serial;
import java.time.LocalDate;
/**
 * DTO pour la mise à jour partielle (PATCH) d'un véhicule.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class VehicleUpdateInput extends AbstractUpdateInput {
    @Serial
    private static final long serialVersionUID = 123456780L;

    private UpdatableProperty<String> brand = UpdatableProperty.empty();
    private UpdatableProperty<String> model = UpdatableProperty.empty();
    private UpdatableProperty<EngineTypeEnum> engineType = UpdatableProperty.empty();
    private UpdatableProperty<String> color = UpdatableProperty.empty();
    // License plate update is not allowed
    // private UpdatableProperty<String> licensePlate = UpdatableProperty.empty();
    private UpdatableProperty<LocalDate> acquisitionDate = UpdatableProperty.empty();
    private UpdatableProperty<VehicleStateEnum> state = UpdatableProperty.empty();

    // PATCH conversion
    public static Vehicle from(final VehicleUpdateInput input, final Vehicle alreadySaved) {
        return alreadySaved.toBuilder()
                .brand(input.getBrand().defaultIfNotOverwrite(alreadySaved.getBrand()))
                .model(input.getModel().defaultIfNotOverwrite(alreadySaved.getModel()))
                .engineType(input.getEngineType().defaultIfNotOverwrite(alreadySaved.getEngineType()))
                .color(input.getColor().defaultIfNotOverwrite(alreadySaved.getColor()))
                // .licensePlate(input.getLicensePlate().defaultIfNotOverwrite(alreadySaved.getLicensePlate()))
                .acquisitionDate(input.getAcquisitionDate().defaultIfNotOverwrite(alreadySaved.getAcquisitionDate()))
                .state(input.getState().defaultIfNotOverwrite(alreadySaved.getState()))
                .build();
    }
}

