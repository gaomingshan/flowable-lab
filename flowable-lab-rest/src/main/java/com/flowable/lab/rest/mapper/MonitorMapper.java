package com.flowable.lab.rest.mapper;

import com.flowable.lab.common.dto.HistoricProcessInstanceDTO;
import com.flowable.lab.common.dto.JobDTO;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.job.api.Job;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MonitorMapper {

    @Mapping(target = "ended", expression = "java(hpi.getEndTime() != null)")
    HistoricProcessInstanceDTO toHistoricDto(HistoricProcessInstance hpi);

    @Mapping(source = "duedate", target = "dueDate")
    JobDTO toJobDto(Job job);

    List<HistoricProcessInstanceDTO> toHistoricDtoList(List<? extends HistoricProcessInstance> list);

    List<JobDTO> toJobDtoList(List<? extends Job> list);
}
