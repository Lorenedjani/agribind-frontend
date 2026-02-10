package cm.agribind.usermanagement.mapper;

import cm.agribind.usermanagement.dto.command.RegisterFarmerCommand;
import cm.agribind.usermanagement.dto.response.FarmerResponse;
import cm.agribind.usermanagement.entity.Cooperative;
import cm.agribind.usermanagement.entity.FarmDetails;
import cm.agribind.usermanagement.entity.Farmer;
import cm.agribind.usermanagement.enums.CropType;
import cm.agribind.usermanagement.enums.LivestockType;
import cm.agribind.usermanagement.enums.UserType;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-10T01:49:50+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260128-0750, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class FarmerMapperImpl implements FarmerMapper {

    @Override
    public Farmer toEntity(RegisterFarmerCommand command) {
        if ( command == null ) {
            return null;
        }

        Farmer farmer = new Farmer();

        farmer.setFarmDetails( registerFarmerCommandToFarmDetails( command ) );
        farmer.setAddress( toAddress( command ) );
        farmer.setName( command.getName() );
        farmer.setPhoneNumber( command.getPhoneNumber() );
        farmer.setAgriculturalType( command.getAgriculturalType() );
        Set<CropType> set = command.getCropTypes();
        if ( set != null ) {
            farmer.setCropTypes( new LinkedHashSet<CropType>( set ) );
        }
        farmer.setEducationLevel( command.getEducationLevel() );
        farmer.setHasBankAccount( command.getHasBankAccount() );
        Set<LivestockType> set1 = command.getLivestockTypes();
        if ( set1 != null ) {
            farmer.setLivestockTypes( new LinkedHashSet<LivestockType>( set1 ) );
        }
        farmer.setYearsFarming( command.getYearsFarming() );

        farmer.setType( UserType.FARMER );

        afterFarmerMapping( command, farmer );

        return farmer;
    }

    @Override
    public FarmerResponse toResponse(Farmer farmer) {
        if ( farmer == null ) {
            return null;
        }

        FarmerResponse farmerResponse = new FarmerResponse();

        farmerResponse.setDisplayDetails( toDisplayDetails( farmer ) );
        farmerResponse.setCropTypesDisplay( cropsToString( farmer.getCropTypes() ) );
        farmerResponse.setLivestockTypesDisplay( livestockToString( farmer.getLivestockTypes() ) );
        farmerResponse.setTotalLandArea( farmerFarmDetailsTotalLandArea( farmer ) );
        farmerResponse.setCultivatedArea( farmerFarmDetailsCultivatedArea( farmer ) );
        farmerResponse.setSoilType( farmerFarmDetailsSoilType( farmer ) );
        farmerResponse.setIrrigationType( farmerFarmDetailsIrrigationType( farmer ) );
        farmerResponse.setOwnsLand( farmerFarmDetailsOwnsLand( farmer ) );
        farmerResponse.setLandOwnershipType( farmerFarmDetailsLandOwnershipType( farmer ) );
        farmerResponse.setCooperativeName( farmerCooperativeName( farmer ) );
        farmerResponse.setAgriculturalType( farmer.getAgriculturalType() );
        Set<CropType> set = farmer.getCropTypes();
        if ( set != null ) {
            farmerResponse.setCropTypes( new LinkedHashSet<CropType>( set ) );
        }
        farmerResponse.setEducationLevel( farmer.getEducationLevel() );
        farmerResponse.setHasBankAccount( farmer.getHasBankAccount() );
        farmerResponse.setHasMobileMoney( farmer.getHasMobileMoney() );
        Set<LivestockType> set1 = farmer.getLivestockTypes();
        if ( set1 != null ) {
            farmerResponse.setLivestockTypes( new LinkedHashSet<LivestockType>( set1 ) );
        }
        farmerResponse.setYearsFarming( farmer.getYearsFarming() );

        return farmerResponse;
    }

    @Override
    public void updateEntityFromCommand(RegisterFarmerCommand command, Farmer farmer) {
        if ( command == null ) {
            return;
        }

        if ( farmer.getFarmDetails() == null ) {
            farmer.setFarmDetails( new FarmDetails() );
        }
        registerFarmerCommandToFarmDetails1( command, farmer.getFarmDetails() );
        if ( command.getName() != null ) {
            farmer.setName( command.getName() );
        }
        if ( command.getPhoneNumber() != null ) {
            farmer.setPhoneNumber( command.getPhoneNumber() );
        }
        if ( command.getAgriculturalType() != null ) {
            farmer.setAgriculturalType( command.getAgriculturalType() );
        }
        if ( farmer.getCropTypes() != null ) {
            Set<CropType> set = command.getCropTypes();
            if ( set != null ) {
                farmer.getCropTypes().clear();
                farmer.getCropTypes().addAll( set );
            }
        }
        else {
            Set<CropType> set = command.getCropTypes();
            if ( set != null ) {
                farmer.setCropTypes( new LinkedHashSet<CropType>( set ) );
            }
        }
        if ( command.getEducationLevel() != null ) {
            farmer.setEducationLevel( command.getEducationLevel() );
        }
        if ( command.getHasBankAccount() != null ) {
            farmer.setHasBankAccount( command.getHasBankAccount() );
        }
        if ( farmer.getLivestockTypes() != null ) {
            Set<LivestockType> set1 = command.getLivestockTypes();
            if ( set1 != null ) {
                farmer.getLivestockTypes().clear();
                farmer.getLivestockTypes().addAll( set1 );
            }
        }
        else {
            Set<LivestockType> set1 = command.getLivestockTypes();
            if ( set1 != null ) {
                farmer.setLivestockTypes( new LinkedHashSet<LivestockType>( set1 ) );
            }
        }
        if ( command.getYearsFarming() != null ) {
            farmer.setYearsFarming( command.getYearsFarming() );
        }

        afterFarmerMapping( command, farmer );
    }

    protected FarmDetails registerFarmerCommandToFarmDetails(RegisterFarmerCommand registerFarmerCommand) {
        if ( registerFarmerCommand == null ) {
            return null;
        }

        FarmDetails farmDetails = new FarmDetails();

        return farmDetails;
    }

    private Double farmerFarmDetailsTotalLandArea(Farmer farmer) {
        if ( farmer == null ) {
            return null;
        }
        FarmDetails farmDetails = farmer.getFarmDetails();
        if ( farmDetails == null ) {
            return null;
        }
        Double totalLandArea = farmDetails.getTotalLandArea();
        if ( totalLandArea == null ) {
            return null;
        }
        return totalLandArea;
    }

    private Double farmerFarmDetailsCultivatedArea(Farmer farmer) {
        if ( farmer == null ) {
            return null;
        }
        FarmDetails farmDetails = farmer.getFarmDetails();
        if ( farmDetails == null ) {
            return null;
        }
        Double cultivatedArea = farmDetails.getCultivatedArea();
        if ( cultivatedArea == null ) {
            return null;
        }
        return cultivatedArea;
    }

    private String farmerFarmDetailsSoilType(Farmer farmer) {
        if ( farmer == null ) {
            return null;
        }
        FarmDetails farmDetails = farmer.getFarmDetails();
        if ( farmDetails == null ) {
            return null;
        }
        String soilType = farmDetails.getSoilType();
        if ( soilType == null ) {
            return null;
        }
        return soilType;
    }

    private String farmerFarmDetailsIrrigationType(Farmer farmer) {
        if ( farmer == null ) {
            return null;
        }
        FarmDetails farmDetails = farmer.getFarmDetails();
        if ( farmDetails == null ) {
            return null;
        }
        String irrigationType = farmDetails.getIrrigationType();
        if ( irrigationType == null ) {
            return null;
        }
        return irrigationType;
    }

    private Boolean farmerFarmDetailsOwnsLand(Farmer farmer) {
        if ( farmer == null ) {
            return null;
        }
        FarmDetails farmDetails = farmer.getFarmDetails();
        if ( farmDetails == null ) {
            return null;
        }
        Boolean ownsLand = farmDetails.getOwnsLand();
        if ( ownsLand == null ) {
            return null;
        }
        return ownsLand;
    }

    private String farmerFarmDetailsLandOwnershipType(Farmer farmer) {
        if ( farmer == null ) {
            return null;
        }
        FarmDetails farmDetails = farmer.getFarmDetails();
        if ( farmDetails == null ) {
            return null;
        }
        String landOwnershipType = farmDetails.getLandOwnershipType();
        if ( landOwnershipType == null ) {
            return null;
        }
        return landOwnershipType;
    }

    private String farmerCooperativeName(Farmer farmer) {
        if ( farmer == null ) {
            return null;
        }
        Cooperative cooperative = farmer.getCooperative();
        if ( cooperative == null ) {
            return null;
        }
        String name = cooperative.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    protected void registerFarmerCommandToFarmDetails1(RegisterFarmerCommand registerFarmerCommand, FarmDetails mappingTarget) {
        if ( registerFarmerCommand == null ) {
            return;
        }

        if ( registerFarmerCommand.getLandArea() != null ) {
            mappingTarget.setTotalLandArea( registerFarmerCommand.getLandArea() );
        }
    }
}
