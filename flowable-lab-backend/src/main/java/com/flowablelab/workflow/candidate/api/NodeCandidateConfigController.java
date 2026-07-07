package com.flowablelab.workflow.candidate.api;

import com.flowablelab.shared.api.ApiResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/platform/workflow-definitions/{definitionId}/versions/{versionNo}/nodes/{nodeKey}/candidate-configs")
public class NodeCandidateConfigController {

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> listCandidateConfigs() {
        return ApiResponse.success(List.of());
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> createCandidateConfig(@PathVariable String nodeKey,
                                                                  @RequestBody CandidateConfigRequest request) {
        return ApiResponse.success(Map.of(
                "id", "candidate-config-1",
                "nodeKey", nodeKey,
                "policyType", request.getPolicyType(),
                "policyMode", request.getPolicyMode(),
                "policyName", request.getPolicyName(),
                "policyPayload", request.getPolicyPayload()
        ));
    }

    @PutMapping("/{configId}")
    public ApiResponse<Map<String, Object>> updateCandidateConfig(@PathVariable String configId,
                                                                  @RequestBody CandidateConfigRequest request) {
        return ApiResponse.success(Map.of(
                "id", configId,
                "policyType", request.getPolicyType(),
                "policyMode", request.getPolicyMode(),
                "policyName", request.getPolicyName(),
                "policyPayload", request.getPolicyPayload()
        ));
    }

    @DeleteMapping("/{configId}")
    public ApiResponse<Map<String, Object>> deleteCandidateConfig(@PathVariable String configId) {
        return ApiResponse.success(Map.of("deleted", configId));
    }

    @Getter
    @Setter
    public static class CandidateConfigRequest {
        private String policyType;
        private String policyMode;
        private String policyName;
        private Map<String, Object> policyPayload;
    }
}
