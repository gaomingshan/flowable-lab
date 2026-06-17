package com.flowable.lab.rest.mapper;

import com.flowable.lab.common.dto.DmnDecisionDTO;
import org.flowable.dmn.api.DmnDecision;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DmnMapper {

    DmnDecisionDTO toDto(DmnDecision decision);

    List<DmnDecisionDTO> toDtoList(List<? extends DmnDecision> list);
}
