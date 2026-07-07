package com.flowablelab.workflow.runtime.api;

import com.flowablelab.shared.api.ApiResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/platform/workflow-instances")
public class WorkflowInstanceController {

    @PostMapping
    public ApiResponse<Map<String, Object>> startWorkflow(@RequestBody StartWorkflowRequest request) {
        return ApiResponse.success(Map.of(
                "instanceId", "instance-1",
                "engineProcessInstanceId", "engine-instance-1",
                "currentNodeName", "开始",
                "status", "running",
                "definitionId", request.getDefinitionId(),
                "businessKey", request.getBusinessKey(),
                "title", request.getTitle()
        ));
    }

    @Getter
    @Setter
    public static class StartWorkflowRequest {
        private String definitionId;
        private String businessKey;
        private String title;
        private String starterId;
        private String starterDeptId;
        private Map<String, Object> variables;
    }
}
