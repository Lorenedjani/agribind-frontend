package cm.agribind.usermanagement.service.query;

import cm.agribind.usermanagement.dto.pagination.PageResponse;
import cm.agribind.usermanagement.dto.response.FarmerResponse;
import cm.agribind.usermanagement.dto.response.UserResponse;
import cm.agribind.usermanagement.enums.AgriculturalType;
import cm.agribind.usermanagement.enums.CropType;
import cm.agribind.usermanagement.enums.LivestockType;

import java.util.List;
import java.util.Map;

public interface FarmerQueryService {

    List<FarmerResponse> getFarmersByAgriculturalType(AgriculturalType type);
    List<FarmerResponse> getFarmersByCropType(CropType cropType);
    List<FarmerResponse> getFarmersByLivestockType(LivestockType livestockType);
    List<FarmerResponse> getFarmersByCooperative(Long cooperativeId);
    PageResponse<UserResponse> getFarmersByCooperative(Long cooperativeId, int page, int size);
    List<FarmerResponse> getFarmersByLandAreaRange(Double minArea, Double maxArea);
    Map<String, Long> getCropDistribution();
    Map<String, Long> getLivestockDistribution();
    Map<String, Long> getAgriculturalTypeDistribution();
    long countFarmersByCropType(CropType cropType);
    long countFarmersByLivestockType(LivestockType livestockType);
}