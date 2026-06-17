package com.flowable.lab.rest.controller;

import com.flowable.lab.common.dto.CaseDefinitionDTO;
import com.flowable.lab.common.dto.CaseInstanceDTO;
import com.flowable.lab.common.response.Result;
import com.flowable.lab.rest.mapper.CmmnMapper;
import com.flowable.lab.service.cmmn.CmmnService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cmmn")
public class CmmnController {

    private final CmmnService cmmnService;
    private final CmmnMapper mapper;

    public CmmnController(CmmnService cmmnService, CmmnMapper mapper) {
        this.cmmnService = cmmnService;
        this.mapper = mapper;
    }

    @GetMapping("/definition")
    public Result<List<CaseDefinitionDTO>> list() {
        return Result.success(mapper.toDefDtoList(cmmnService.listCaseDefinitions()));
    }

    @PostMapping("/case/start/{caseKey}")
    public Result<CaseInstanceDTO> start(@PathVariable String caseKey,
                                          @RequestBody(required = false) Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return Result.success(mapper.toCaseDto(cmmnService.startCase(caseKey)));
        }
        return Result.success(mapper.toCaseDto(cmmnService.startCase(caseKey, variables)));
    }

    @GetMapping("/case")
    public Result<List<CaseInstanceDTO>> listCases() {
        return Result.success(mapper.toCaseDtoList(cmmnService.listRunningCases()));
    }

    @GetMapping("/case/{id}")
    public Result<CaseInstanceDTO> getById(@PathVariable String id) {
        return Result.success(mapper.toCaseDto(cmmnService.getCaseInstance(id)));
    }

    @DeleteMapping("/case/{id}")
    public Result<Void> terminate(@PathVariable String id) {
        cmmnService.terminateCase(id);
        return Result.success();
    }
}
