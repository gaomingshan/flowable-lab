package com.flowable.lab.rest.mapper;

import com.flowable.lab.common.dto.CaseDefinitionDTO;
import com.flowable.lab.common.dto.CaseInstanceDTO;
import org.flowable.cmmn.api.repository.CaseDefinition;
import org.flowable.cmmn.api.runtime.CaseInstance;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CmmnMapper {

    CaseDefinitionDTO toDefDto(CaseDefinition def);

    CaseInstanceDTO toCaseDto(CaseInstance ci);

    List<CaseDefinitionDTO> toDefDtoList(List<? extends CaseDefinition> list);

    List<CaseInstanceDTO> toCaseDtoList(List<? extends CaseInstance> list);
}
