package com.flowable.lab.rest.controller;

import com.flowable.lab.common.dto.DmnDecisionDTO;
import com.flowable.lab.common.response.Result;
import com.flowable.lab.rest.mapper.DmnMapper;
import com.flowable.lab.service.dmn.DmnService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dmn")
public class DmnController {

    private static final Logger log = LoggerFactory.getLogger(DmnController.class);

    private final DmnService dmnService;
    private final DmnMapper mapper;

    public DmnController(DmnService dmnService, DmnMapper mapper) {
        this.dmnService = dmnService;
        this.mapper = mapper;
    }

    @GetMapping("/decision")
    public Result<List<DmnDecisionDTO>> list() {
        return Result.success(mapper.toDtoList(dmnService.listDecisions()));
    }

    @PostMapping("/evaluate/{decisionKey}")
    public Result<Map<String, Object>> evaluate(@PathVariable String decisionKey,
                                                @RequestBody Map<String, Object> variables) {
        Map<String, Object> result = dmnService.evaluateDecision(decisionKey, variables);
        log.info("DMN evaluate: key={}, variables={}, result={}", decisionKey, variables, result);
        return Result.success(result);
    }

    @PostMapping("/redeploy-test")
    public Result<Map<String, Object>> redeployAndTest() {
        String dmnContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<definitions xmlns=\"http://www.omg.org/spec/DMN/20151101\""
                + " xmlns:flowable=\"http://flowable.org/dmn\""
                + " id=\"test-decision-def\" name=\"test-decision-def\""
                + " namespace=\"http://flowable.org/dmn\">"
                + "<decision id=\"test-decision\" name=\"\u6D4B\u8BD5\u51B3\u7B56\">"
                + "<decisionTable id=\"decisionTable\" hitPolicy=\"UNIQUE\">"
                + "<input id=\"input1\" label=\"\u503C\" flowable:type=\"number\">"
                + "<inputExpression id=\"inputExpression1\" typeRef=\"number\">"
                + "<text>x</text></inputExpression></input>"
                + "<output id=\"output1\" label=\"\u7ED3\u679C\" name=\"result\" typeRef=\"string\"/>"
                + "<rule id=\"rule1\">"
                + "<inputEntry id=\"inputEntry1\"><text>&lt; 100</text></inputEntry>"
                + "<outputEntry id=\"outputEntry1\"><text>\"small\"</text></outputEntry>"
                + "</rule>"
                + "<rule id=\"rule2\">"
                + "<inputEntry id=\"inputEntry2\"><text>&gt;= 100</text></inputEntry>"
                + "<outputEntry id=\"outputEntry2\"><text>\"large\"</text></outputEntry>"
                + "</rule></decisionTable></decision></definitions>";
        dmnService.deploy("test-deployment", dmnContent);
        Map<String, Object> result = dmnService.evaluateDecision("test-decision", Map.of("x", 50));
        return Result.success(result);
    }

    @PostMapping("/redeploy-expense")
    public Result<Map<String, Object>> redeployExpense() {
        String dmnContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<definitions xmlns=\"http://www.omg.org/spec/DMN/20151101\""
                + " xmlns:flowable=\"http://flowable.org/dmn\""
                + " id=\"expense-approval-def\" name=\"expense-approval\""
                + " namespace=\"http://flowable.org/dmn\">"
                + "<decision id=\"expense-approval\" name=\"\u8D39\u7528\u5BA1\u6279\u51B3\u7B56\">"
+ "<decisionTable id=\"decisionTable\" hitPolicy=\"FIRST\">"
+ "<input id=\"input1\" label=\"\u91D1\u989D\" flowable:type=\"number\">"
+ "<inputExpression id=\"inputExpression1\" typeRef=\"number\">"
+ "<text>amount</text></inputExpression></input>"
+ "<input id=\"input2\" label=\"\u804C\u7EA7\" flowable:type=\"string\">"
+ "<inputExpression id=\"inputExpression2\" typeRef=\"string\">"
+ "<text>level</text></inputExpression></input>"
+ "<output id=\"output1\" label=\"\u51B3\u7B56\u7ED3\u679C\" name=\"decision\" typeRef=\"string\"/>"
+ "<output id=\"output2\" label=\"\u5BA1\u6279\u4EBA\" name=\"approver\" typeRef=\"string\"/>"
+ "<rule id=\"rule1\">"
+ "<inputEntry id=\"inputEntry1\"><text>&lt; 1000</text></inputEntry>"
+ "<inputEntry id=\"inputEntry2\"><text>level != null</text></inputEntry>"
+ "<outputEntry id=\"outputEntry1\"><text>\"auto_approve\"</text></outputEntry>"
+ "<outputEntry id=\"outputEntry2\"><text>\"\"</text></outputEntry>"
+ "</rule>"
+ "<rule id=\"rule2\">"
+ "<inputEntry id=\"inputEntry3\"><text>&lt; 5000</text></inputEntry>"
+ "<inputEntry id=\"inputEntry4\"><text>\"manager\"</text></inputEntry>"
+ "<outputEntry id=\"outputEntry3\"><text>\"auto_approve\"</text></outputEntry>"
+ "<outputEntry id=\"outputEntry4\"><text>\"\"</text></outputEntry>"
+ "</rule>"
+ "<rule id=\"rule3\">"
+ "<inputEntry id=\"inputEntry5\"><text>&lt; 5000</text></inputEntry>"
+ "<inputEntry id=\"inputEntry6\"><text>\"employee\"</text></inputEntry>"
+ "<outputEntry id=\"outputEntry5\"><text>\"need_approval\"</text></outputEntry>"
+ "<outputEntry id=\"outputEntry6\"><text>\"manager\"</text></outputEntry>"
+ "</rule>"
+ "<rule id=\"rule4\">"
+ "<inputEntry id=\"inputEntry7\"><text>&gt;= 5000</text></inputEntry>"
+ "<inputEntry id=\"inputEntry8\"><text>level != null</text></inputEntry>"
+ "<outputEntry id=\"outputEntry7\"><text>\"need_approval\"</text></outputEntry>"
+ "<outputEntry id=\"outputEntry8\"><text>\"director\"</text></outputEntry>"
+ "</rule></decisionTable></decision></definitions>";
        dmnService.deploy("expense-fresh", dmnContent);
        Map<String, Object> result = dmnService.evaluateDecision("expense-approval", Map.of("amount", 500, "level", "employee"));
        return Result.success(result);
    }
    @PostMapping("/deploy")
    public Result<Map<String, Object>> deploy(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String content = body.get("content");
        if (name == null || content == null) {
            return Result.error(400, "name and content required");
        }
        dmnService.deploy(name, content);
        List<DmnDecisionDTO> decisions = mapper.toDtoList(dmnService.listDecisions());
        return Result.success(Map.of(
            "deployed", name,
            "decisions", decisions
        ));
    }

    public Result<Map<String, Object>> debug() {
        // Test 1: simple 2-input 2-output with null
        String d1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<definitions xmlns=\"http://www.omg.org/spec/DMN/20151101\""
                + " xmlns:flowable=\"http://flowable.org/dmn\""
                + " id=\"d1\" name=\"d1\" namespace=\"http://flowable.org/dmn\">"
                + "<decision id=\"d1\" name=\"d1\">"
                + "<decisionTable id=\"dt\" hitPolicy=\"UNIQUE\">"
                + "<input id=\"i1\" label=\"a\" flowable:type=\"number\">"
                + "<inputExpression typeRef=\"number\"><text>a</text></inputExpression></input>"
                + "<input id=\"i2\" label=\"b\" flowable:type=\"string\">"
                + "<inputExpression typeRef=\"string\"><text>b</text></inputExpression></input>"
                + "<output id=\"o1\" label=\"out1\" name=\"out1\" typeRef=\"string\"/>"
                + "<output id=\"o2\" label=\"out2\" name=\"out2\" typeRef=\"string\"/>"
                + "<rule id=\"r1\">"
                + "<inputEntry><text>&lt; 100</text></inputEntry>"
                + "<inputEntry><text>\"x\"</text></inputEntry>"
                + "<outputEntry><text>\"small\"</text></outputEntry>"
                + "<outputEntry><text>\"auto\"</text></outputEntry>"
                + "</rule></decisionTable></decision></definitions>";
        dmnService.deploy("d1-deploy", d1);
        Map<String, Object> r1 = dmnService.evaluateDecision("d1", Map.of("a", 50, "b", "x"));

        // Test 2: 2-input 1-output with - and null
        String d2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<definitions xmlns=\"http://www.omg.org/spec/DMN/20151101\""
                + " xmlns:flowable=\"http://flowable.org/dmn\""
                + " id=\"d2\" name=\"d2\" namespace=\"http://flowable.org/dmn\">"
                + "<decision id=\"d2\" name=\"d2\">"
                + "<decisionTable id=\"dt\" hitPolicy=\"UNIQUE\">"
                + "<input id=\"i1\" label=\"a\" flowable:type=\"number\">"
                + "<inputExpression typeRef=\"number\"><text>a</text></inputExpression></input>"
                + "<input id=\"i2\" label=\"b\" flowable:type=\"string\">"
                + "<inputExpression typeRef=\"string\"><text>b</text></inputExpression></input>"
                + "<output id=\"o1\" label=\"out\" name=\"out\" typeRef=\"string\"/>"
                + "<rule id=\"r1\">"
                + "<inputEntry><text>&lt; 100</text></inputEntry>"
                + "<inputEntry><text>-</text></inputEntry>"
                + "<outputEntry><text>null</text></outputEntry>"
                + "</rule></decisionTable></decision></definitions>";
        dmnService.deploy("d2-deploy", d2);
        Map<String, Object> r2 = dmnService.evaluateDecision("d2", Map.of("a", 50, "b", "x"));

        return Result.success(Map.of("test1_2in_2out_noNull", r1, "test2_hyphen_null", r2));
    }
}
