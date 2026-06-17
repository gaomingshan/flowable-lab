package com.flowable.lab.rest.mapper;

import com.flowable.lab.common.dto.*;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BpmnMapper {

    ProcessDefinitionDTO toDto(ProcessDefinition pd);

    DeploymentDTO toDto(Deployment d);

    ProcessInstanceDTO toDto(ProcessInstance pi);

    @Mapping(target = "ended", expression = "java(hpi.getEndTime() != null)")
    HistoricProcessInstanceDTO toHistoricDto(HistoricProcessInstance hpi);

    TaskDTO toDto(Task task);

    HistoricTaskInstanceDTO toHistoricDto(HistoricTaskInstance task);

    List<ProcessDefinitionDTO> toPdDtoList(List<? extends ProcessDefinition> list);

    List<DeploymentDTO> toDeployDtoList(List<? extends Deployment> list);

    List<ProcessInstanceDTO> toPiDtoList(List<? extends ProcessInstance> list);

    List<HistoricProcessInstanceDTO> toHistoricPiDtoList(List<? extends HistoricProcessInstance> list);

    List<TaskDTO> toTaskDtoList(List<? extends Task> list);

    List<HistoricTaskInstanceDTO> toHistoricTaskDtoList(List<? extends HistoricTaskInstance> list);
}
