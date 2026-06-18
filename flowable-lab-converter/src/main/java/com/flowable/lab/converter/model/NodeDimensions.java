package com.flowable.lab.converter.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NodeDimensions {
    EVENT(40, 40),
    TASK(100, 80),
    GATEWAY(50, 50),
    SUB_PROCESS(400, 300),
    POOL(600, 200),
    TEXT_ANNOTATION(100, 40);

    private final int defaultWidth;
    private final int defaultHeight;

    public static NodeDimensions forNode(NodeJson node) {
        return switch (node.getType()) {
            case "event" -> EVENT;
            case "task" -> TASK;
            case "gateway" -> GATEWAY;
            case "subProcess" -> SUB_PROCESS;
            case "pool" -> POOL;
            case "textAnnotation" -> TEXT_ANNOTATION;
            default -> TASK;
        };
    }
}
