package com.flowable.lab.converter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
public class NodeJson {
    private String id;
    private String type;
    private String subtype;
    private String label;
    private double x;
    private double y;
    private Double width;
    private Double height;
    private boolean collapsed;
    @Builder.Default
    private Map<String, Object> properties = new LinkedHashMap<>();
    @Builder.Default
    private Map<String, Object> extensions = new LinkedHashMap<>();
    private ProcessJson process;
}
