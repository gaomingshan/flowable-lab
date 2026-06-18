package com.flowable.lab.service.bpmn;

import com.flowable.lab.converter.BpmnEditorJsonConverter;
import com.flowable.lab.converter.model.ProcessJson;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BpmnEditorService {

    private static final Logger log = LoggerFactory.getLogger(BpmnEditorService.class);

    private final RepositoryService repositoryService;
    private final BpmnDeployService deployService;
    private final BpmnEditorJsonConverter converter;

    public BpmnEditorService(RepositoryService repositoryService,
                              BpmnDeployService deployService,
                              BpmnEditorJsonConverter converter) {
        this.repositoryService = repositoryService;
        this.deployService = deployService;
        this.converter = converter;
    }

    public ProcessJson loadProcess(String processDefinitionId) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        if (bpmnModel == null) return null;

        BpmnDiagramService.ensureGraphicInfo(bpmnModel);
        String xml = new String(new BpmnXMLConverter().convertToXML(bpmnModel));
        log.info("Loaded BPMN XML for process: {}, length={}", processDefinitionId, xml.length());
        return converter.fromBpmnXml(xml);
    }

    public org.flowable.engine.repository.Deployment deployProcess(ProcessJson processJson,
                                                                    String deploymentName) {
        String bpmnXml = converter.toBpmnXml(processJson);
        log.info("Generated BPMN XML for process '{}', length={}",
                processJson.getProcess().getName(), bpmnXml.length());
        return deployService.deploy(deploymentName, bpmnXml);
    }
}
