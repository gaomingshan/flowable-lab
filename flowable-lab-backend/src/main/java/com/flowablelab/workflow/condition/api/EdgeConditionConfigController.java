package com.flowablelab.workflow.condition.api;

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
@RequestMapping("/api/platform/workflow-definitions/{definitionId}/versions/{versionNo}/edges/{edgeKey}/condition-configs")
public class EdgeConditionConfigController {

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> listConditionConfigs() {
        return ApiResponse.success(List.of());
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> createConditionConfig(@PathVariable String edgeKey,
                                                                  @RequestBody ConditionConfigRequest request) {
        return ApiResponse.success(Map.of(
                "id", "condition-config-1",
                "edgeKey", edgeKey,
                "conditionName", request.getConditionName(),
                "conditionType", request.getConditionType(),
                "expressionText", request.getExpressionText(),
                "isDefaultBranch", request.getIsDefaultBranch()
        ));
    }

    @PutMapping("/{configId}")
    public ApiResponse<Map<String, Object>> updateConditionConfig(@PathVariable String configId,
                                                                  @RequestBody ConditionConfigRequest request) {
        return ApiResponse.success(Map.of(
                "id", configId,
                "conditionName", request.getConditionName(),
                "conditionType", request.getConditionType(),
                "expressionText", request.getExpressionText(),
                "isDefaultBranch", request.getIsDefaultBranch()
        ));
    }

    @DeleteMapping("/{configId}")
    public ApiResponse<Map<String, Object>> deleteConditionConfig(@PathVariable String configId) {
        return ApiResponse.success(Map.of("deleted", configId));
    }

    @Getter
    @Setter
    public static class ConditionConfigRequest {
        private String conditionName;
        private String conditionType;
        private String expressionText;
        private Boolean isDefaultBranch;
    }
}
