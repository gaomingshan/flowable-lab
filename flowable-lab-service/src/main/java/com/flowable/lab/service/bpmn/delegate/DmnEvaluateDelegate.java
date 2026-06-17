package com.flowable.lab.service.bpmn.delegate;

import com.flowable.lab.service.dmn.DmnService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("dmnEvaluateDelegate")
public class DmnEvaluateDelegate implements JavaDelegate {

    private final DmnService dmnService;

    public DmnEvaluateDelegate(DmnService dmnService) {
        this.dmnService = dmnService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        String decisionKey = (String) execution.getVariable("decisionKey");
        if (decisionKey == null) {
            decisionKey = "expense-approval";
        }

        Map<String, Object> variables = execution.getVariables();
        Map<String, Object> result = dmnService.evaluateDecision(decisionKey, variables);

        execution.setVariables(result);
    }
}
