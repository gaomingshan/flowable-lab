package com.flowable.lab.converter;

import com.flowable.lab.converter.model.EdgeJson;
import com.flowable.lab.converter.model.NodeJson;
import com.flowable.lab.converter.model.ProcessJson;
import com.flowable.lab.converter.strategy.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BpmnEditorJsonConverterTest {

    private BpmnEditorJsonConverter converter;

    @BeforeEach
    void setUp() {
        EventNodeStrategy eventStrategy = new EventNodeStrategy();
        TaskNodeStrategy taskStrategy = new TaskNodeStrategy();
        GatewayNodeStrategy gatewayStrategy = new GatewayNodeStrategy();
        SubProcessNodeStrategy subProcessStrategy = new SubProcessNodeStrategy();
        converter = new BpmnEditorJsonConverter(eventStrategy, taskStrategy, gatewayStrategy, subProcessStrategy);
        converter.init();
    }

    @Test
    void shouldRoundTripSimpleProcess() {
        ProcessJson original = new ProcessJson()
                .setProcess(new ProcessJson.ProcessInfo()
                        .setId("testProcess")
                        .setName("测试流程")
                        .setDocumentation("A simple test"))
                .setNodes(List.of(
                        node("start_1", "event", "start", "开始", 100, 200),
                        node("user_1", "task", "user", "审批", 300, 200),
                        node("end_1", "event", "end", "结束", 500, 200)
                ))
                .setEdges(List.of(
                        edge("flow_1", "start_1", "user_1"),
                        edge("flow_2", "user_1", "end_1")
                ));

        String xml = converter.toBpmnXml(original);
        System.out.println("=== Generated XML ===");
        System.out.println(xml);

        assertThat(xml).contains("<definitions");
        assertThat(xml).contains("<startEvent");
        assertThat(xml).contains("id=\"start_1\"");
        assertThat(xml).contains("<userTask");
        assertThat(xml).contains("id=\"user_1\"");
        assertThat(xml).contains("<endEvent");
        assertThat(xml).contains("<sequenceFlow");

        ProcessJson result = converter.fromBpmnXml(xml);
        assertThat(result.getProcess().getId()).isEqualTo("testProcess");
        assertThat(result.getProcess().getName()).isEqualTo("测试流程");
        assertThat(result.getNodes()).hasSize(3);
        assertThat(result.getEdges()).hasSize(2);
    }

    @Test
    void shouldConvertUserTaskWithProperties() {
        ProcessJson json = new ProcessJson()
                .setProcess(new ProcessJson.ProcessInfo().setId("props").setName("属性测试"))
                .setNodes(List.of(
                        node("start", "event", "start", "开始", 100, 100),
                        NodeJson.builder()
                                .id("task").type("task").subtype("user").label("审批人")
                                .x(300).y(100)
                                .properties(Map.of(
                                        "assignee", "${manager}",
                                        "candidateGroups", "hr,finance",
                                        "formKey", "leave-form",
                                        "priority", 50
                                ))
                                .build(),
                        node("end", "event", "end", "结束", 500, 100)
                ))
                .setEdges(List.of(
                        edge("f1", "start", "task"),
                        edge("f2", "task", "end")
                ));

        String xml = converter.toBpmnXml(json);
        System.out.println("=== XML with properties ===");
        System.out.println(xml);

        assertThat(xml).contains("flowable:assignee=\"${manager}\"");
        assertThat(xml).contains("flowable:formKey=\"leave-form\"");

        ProcessJson result = converter.fromBpmnXml(xml);
        var taskNode = result.getNodes().stream()
                .filter(n -> "task".equals(n.getType())).findFirst().orElseThrow();
        assertThat(taskNode.getProperties().get("candidateUsers")).isNull();
    }

    @Test
    void shouldConvertAllGatewayTypes() {
        ProcessJson json = new ProcessJson()
                .setProcess(new ProcessJson.ProcessInfo().setId("gateways").setName("网关测试"))
                .setNodes(List.of(
                        node("s", "event", "start", "开始", 100, 250),
                        node("xor", "gateway", "exclusive", "排他", 300, 150),
                        node("and", "gateway", "parallel", "并行", 300, 350),
                        node("or", "gateway", "inclusive", "包容", 500, 250),
                        node("e", "event", "end", "结束", 700, 250)
                ))
                .setEdges(List.of(
                        edge("f1", "s", "xor"),
                        edge("f2", "s", "and"),
                        edge("f3", "xor", "or"),
                        edge("f4", "and", "or"),
                        edge("f5", "or", "e")
                ));

        String xml = converter.toBpmnXml(json);
        System.out.println("=== XML with gateways ===");
        System.out.println(xml);

        assertThat(xml).contains("<exclusiveGateway");
        assertThat(xml).contains("<parallelGateway");
        assertThat(xml).contains("<inclusiveGateway");
    }

    @Test
    void shouldRoundTripExistingBpmnXml() {
        String inputXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                                  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
                                  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
                                  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
                                  xmlns:flowable="http://flowable.org/bpmn"
                                  targetNamespace="http://flowable.org/bpmn">
                  <bpmn:process id="leave" name="请假审批流程" isExecutable="true">
                    <bpmn:startEvent id="startEvent1" name="开始"/>
                    <bpmn:userTask id="userTask1" name="填写申请" flowable:assignee="${initiator}"/>
                    <bpmn:endEvent id="endEvent1" name="结束"/>
                    <bpmn:sequenceFlow id="flow1" sourceRef="startEvent1" targetRef="userTask1"/>
                    <bpmn:sequenceFlow id="flow2" sourceRef="userTask1" targetRef="endEvent1"/>
                  </bpmn:process>
                  <bpmndi:BPMNDiagram id="Diagram_1">
                    <bpmndi:BPMNPlane bpmnElement="leave">
                      <bpmndi:BPMNShape bpmnElement="startEvent1">
                        <dc:Bounds x="50" y="50" width="40" height="40"/>
                      </bpmndi:BPMNShape>
                      <bpmndi:BPMNShape bpmnElement="userTask1">
                        <dc:Bounds x="200" y="40" width="100" height="80"/>
                      </bpmndi:BPMNShape>
                      <bpmndi:BPMNShape bpmnElement="endEvent1">
                        <dc:Bounds x="420" y="50" width="40" height="40"/>
                      </bpmndi:BPMNShape>
                      <bpmndi:BPMNEdge bpmnElement="flow1">
                        <di:waypoint x="70" y="70"/>
                        <di:waypoint x="250" y="80"/>
                      </bpmndi:BPMNEdge>
                      <bpmndi:BPMNEdge bpmnElement="flow2">
                        <di:waypoint x="250" y="80"/>
                        <di:waypoint x="440" y="70"/>
                      </bpmndi:BPMNEdge>
                    </bpmndi:BPMNPlane>
                  </bpmndi:BPMNDiagram>
                </bpmn:definitions>
                """;

        ProcessJson result = converter.fromBpmnXml(inputXml);

        assertThat(result.getProcess().getId()).isEqualTo("leave");
        assertThat(result.getProcess().getName()).isEqualTo("请假审批流程");

        assertThat(result.getNodes()).hasSize(3);
        assertThat(result.getNodes().get(0).getId()).isEqualTo("startEvent1");
        assertThat(result.getNodes().get(0).getX()).isGreaterThan(0);

        assertThat(result.getEdges()).hasSize(2);

        String reExport = converter.toBpmnXml(result);
        assertThat(reExport).contains("<startEvent");
        assertThat(reExport).contains("flowable:assignee=\"${initiator}\"");
    }

    @Test
    void shouldRoundTripSubProcess() {
        ProcessJson subProcess = new ProcessJson()
                .setProcess(new ProcessJson.ProcessInfo().setId("sub").setName("子流程"))
                .setNodes(List.of(
                        node("sub_s", "event", "start", "子开始", 100, 100),
                        node("sub_e", "event", "end", "子结束", 300, 100)
                ))
                .setEdges(List.of(
                        edge("sub_f1", "sub_s", "sub_e")
                ));

        NodeJson subNode = NodeJson.builder()
                .id("sub1").type("subProcess").subtype("embedded").label("内部子流程")
                .x(400).y(200)
                .process(subProcess)
                .build();

        ProcessJson main = new ProcessJson()
                .setProcess(new ProcessJson.ProcessInfo().setId("main").setName("主流程"))
                .setNodes(List.of(
                        node("main_s", "event", "start", "开始", 100, 200),
                        subNode,
                        node("main_e", "event", "end", "结束", 700, 200)
                ))
                .setEdges(List.of(
                        edge("f1", "main_s", "sub1"),
                        edge("f2", "sub1", "main_e")
                ));

        String xml = converter.toBpmnXml(main);
        System.out.println("=== SubProcess XML ===");
        System.out.println(xml);

        assertThat(xml).contains("<subProcess");
        assertThat(xml).contains("id=\"sub1\"");
        assertThat(xml).contains("id=\"sub_s\"");
        assertThat(xml).contains("id=\"sub_e\"");

        ProcessJson result = converter.fromBpmnXml(xml);
        assertThat(result.getNodes()).hasSize(3);

        var subNodeResult = result.getNodes().stream()
                .filter(n -> "subProcess".equals(n.getType())).findFirst().orElseThrow();
        assertThat(subNodeResult.getId()).isEqualTo("sub1");
        assertThat(subNodeResult.getProcess()).isNotNull();
        assertThat(subNodeResult.getProcess().getNodes()).hasSize(2);
        assertThat(subNodeResult.getProcess().getEdges()).hasSize(1);
    }

    // ---- helpers ----

    private NodeJson node(String id, String type, String subtype, String label, double x, double y) {
        return NodeJson.builder().id(id).type(type).subtype(subtype).label(label).x(x).y(y).build();
    }

    private EdgeJson edge(String id, String source, String target) {
        return EdgeJson.builder().id(id).source(source).target(target).build();
    }
}
