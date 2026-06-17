package com.flowable.lab.service.bpmn.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("logDelegate")
public class LogDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(LogDelegate.class);

    @Override
    public void execute(DelegateExecution execution) {
        log.info("=== LogDelegate ===");
        log.info("ProcessInstanceId: {}", execution.getProcessInstanceId());
        log.info("ProcessDefinitionId: {}", execution.getProcessDefinitionId());
        log.info("ActivityId: {}", execution.getCurrentActivityId());
        log.info("Variables: {}", execution.getVariables());
        log.info("===================");
    }
}
