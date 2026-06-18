package com.flowable.lab.converter.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class ProcessJson {
    private ProcessInfo process = new ProcessInfo();
    private List<NodeJson> nodes = new ArrayList<>();
    private List<EdgeJson> edges = new ArrayList<>();

    @Data
    @Accessors(chain = true)
    public static class ProcessInfo {
        private String id;
        private String name;
        private boolean isExecutable = true;
        private String documentation;
        private List<String> candidateStarterUsers = new ArrayList<>();
        private List<String> candidateStarterGroups = new ArrayList<>();
        private Map<String, Object> extensions = new LinkedHashMap<>();
    }
}
