package cm.agribind.usermanagement.service.query.impl;

import cm.agribind.usermanagement.dto.pagination.PageResponse;
import cm.agribind.usermanagement.dto.response.FarmerResponse;
import cm.agribind.usermanagement.dto.response.UserResponse;
import cm.agribind.usermanagement.entity.Farmer;
import cm.agribind.usermanagement.enums.AgriculturalType;
import cm.agribind.usermanagement.enums.CropType;
import cm.agribind.usermanagement.enums.LivestockType;
import cm.agribind.usermanagement.mapper.FarmerMapper;
import cm.agribind.usermanagement.mapper.UserMapper;
import cm.agribind.usermanagement.repository.FarmerRepository;
import cm.agribind.usermanagement.repository.spec.FarmerSpecification;
import cm.agribind.usermanagement.service.query.FarmerQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FarmerQueryServiceImpl implements FarmerQueryService {

    private final FarmerRepository farmerRepository;
    private final FarmerMapper farmerMapper;
    private final UserMapper userMapper;

    @Override
    public List<FarmerResponse> getFarmersByAgriculturalType(AgriculturalType type) {
        log.debug("Fetching farmers by agricultural type: {}", type);

        List<Farmer> farmers = farmerRepository.findByAgriculturalType(type);
        return farmers.stream()
                .map(farmerMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FarmerResponse> getFarmersByCropType(CropType cropType) {
        log.debug("Fetching farmers by crop type: {}", cropType);

        List<Farmer> farmers = farmerRepository.findByCropType(cropType);
        return farmers.stream()
                .map(farmerMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FarmerResponse> getFarmersByLivestockType(LivestockType livestockType) {
        log.debug("Fetching farmers by livestock type: {}", livestockType);

        List<Farmer> farmers = farmerRepository.findByLivestockType(livestockType);
        return farmers.stream()
                .map(farmerMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FarmerResponse> getFarmersByCooperative(Long cooperativeId) {
        log.debug("Fetching farmers by cooperative: {}", cooperativeId);

        List<Farmer> farmers = farmerRepository.findByCooperativeId(cooperativeId);
        return farmers.stream()
                .map(farmerMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<UserResponse> getFarmersByCooperative(Long cooperativeId, int page, int size) {
        log.debug("Fetching farmers by cooperative with pagination: {}", cooperativeId);

        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<Farmer> farmerPage = farmerRepository.findByCooperativeId(cooperativeId, pageable);

        List<UserResponse> content = farmerPage.getContent()
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());

        return PageResponse.of(
                content,
                farmerPage.getNumber(),
                farmerPage.getSize(),
                farmerPage.getTotalElements()
        );
    }

    @Override
    public List<FarmerResponse> getFarmersByLandAreaRange(Double minArea, Double maxArea) {
        log.debug("Fetching farmers by land area range: {} - {}", minArea, maxArea);

        List<Farmer> farmers = farmerRepository.findByLandAreaRange(minArea, maxArea);
        return farmers.stream()
                .map(farmerMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getCropDistribution() {
        log.debug("Fetching crop distribution statistics");

        return farmerRepository.findAll().stream()
                .flatMap(farmer -> farmer.getCropTypes().stream())
                .collect(Collectors.groupingBy(
                        Enum::name,
                        Collectors.counting()
                ));
    }

    @Override
    public Map<String, Long> getLivestockDistribution() {
        log.debug("Fetching livestock distribution statistics");

        return farmerRepository.findAll().stream()
                .flatMap(farmer -> farmer.getLivestockTypes().stream())
                .collect(Collectors.groupingBy(
                        Enum::name,
                        Collectors.counting()
                ));
    }

    @Override
    public Map<String, Long> getAgriculturalTypeDistribution() {
        log.debug("Fetching agricultural type distribution");

        return farmerRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        farmer -> farmer.getAgriculturalType().name(),
                        Collectors.counting()
                ));
    }

    @Override
    public long countFarmersByCropType(CropType cropType) {
        return farmerRepository.countByCropType(cropType);
    }

    @Override
    public long countFarmersByLivestockType(LivestockType livestockType) {
        // Fixed: This should be countByLivestockType, but since we don't have that method,
        // let's implement it using the repository method we have
        return farmerRepository.findByLivestockType(livestockType).size();
    }
}