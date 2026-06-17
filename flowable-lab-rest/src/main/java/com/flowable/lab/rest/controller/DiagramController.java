package com.flowable.lab.rest.controller;

import com.flowable.lab.service.bpmn.BpmnDiagramService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;

@RestController
@RequestMapping("/api/diagram")
public class DiagramController {

    private static final Logger log = LoggerFactory.getLogger(DiagramController.class);

    private final BpmnDiagramService bpmnDiagramService;

    public DiagramController(BpmnDiagramService bpmnDiagramService) {
        this.bpmnDiagramService = bpmnDiagramService;
    }

    @GetMapping(value = "/{processDefinitionId}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getDiagram(@PathVariable String processDefinitionId) {
        InputStream is = bpmnDiagramService.generateDiagram(processDefinitionId);
        return toByteArray(is);
    }

    @GetMapping(value = "/{processDefinitionId}/{processInstanceId}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getHighlightedDiagram(@PathVariable String processDefinitionId,
                                        @PathVariable String processInstanceId) {
        InputStream is = bpmnDiagramService.generateHighlightedDiagram(processDefinitionId, processInstanceId);
        return toByteArray(is);
    }

    private byte[] toByteArray(InputStream is) {
        if (is == null) {
            log.warn("Diagram input stream is null for request");
            return new byte[0];
        }
        try {
            return is.readAllBytes();
        } catch (Exception e) {
            log.error("Failed to read diagram stream", e);
            return new byte[0];
        }
    }
}
