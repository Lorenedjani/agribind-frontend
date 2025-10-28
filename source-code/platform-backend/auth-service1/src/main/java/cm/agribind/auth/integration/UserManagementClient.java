package cm.agribind.auth.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

@FeignClient(name = "user-management-service", url = "${user.service.url:http://localhost:8082}")
public interface UserManagementClient {

    @PostMapping("/api/v1/users/sync")
    Map<String, Object> syncUser(@RequestBody Map<String, Object> userData);
}
