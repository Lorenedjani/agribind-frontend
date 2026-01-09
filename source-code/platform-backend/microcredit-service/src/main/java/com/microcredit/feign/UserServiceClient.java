package com.microcredit.feign;

import com.microcredit.dto.FarmerDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/users/farmers/{farmerId}")
    FarmerDTO getFarmerById(
            @PathVariable String farmerId,
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/api/users/farmers/search")
    List<FarmerDTO> searchFarmersByName(
            @RequestParam String name,
            @RequestHeader("Authorization") String token
    );
}