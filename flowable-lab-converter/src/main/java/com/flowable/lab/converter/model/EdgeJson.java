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
public class EdgeJson {
    private String id;
    private String source;
    private String target;
    private String label;
    @Builder.Default
    private Map<String, Object> properties = new LinkedHashMap<>();
    @Builder.Default
    private Map<String, Object> extensions = new LinkedHashMap<>();
}
