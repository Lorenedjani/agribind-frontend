package cm.agribind.usermanagement.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class FarmDetails {

    private Double totalLandArea; // in hectares

    private Double cultivatedArea; // in hectares

    private Double pastureArea; // for livestock

    private String soilType;

    private String irrigationType; // NONE, DRIP, SPRINKLER, etc.

    private Boolean ownsLand = true;

    private String landOwnershipType; // OWNED, RENTED, COMMUNAL

    private Integer numberOfPlots;

    private Double averageYield; // kg per hectare

    private String farmingMethods; // TRADITIONAL, MODERN, MIXED

    private Boolean usesFertilizers = false;

    private Boolean usesPesticides = false;

    private Boolean usesOrganicMethods = true;

    private String equipmentOwned; // Comma-separated equipment list
}