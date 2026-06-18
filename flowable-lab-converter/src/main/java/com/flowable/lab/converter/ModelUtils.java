package com.flowable.lab.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class ModelUtils {

    private static final Logger log = LoggerFactory.getLogger(ModelUtils.class);
    private static final ObjectMapper OM = new ObjectMapper();

    private ModelUtils() {}

    public static String getString(Map<String, Object> props, String key) {
        Object v = props.get(key);
        return v == null ? null : v.toString();
    }

    public static Integer getInteger(Map<String, Object> props, String key) {
        Object v = props.get(key);
        if (v instanceof Number n) return n.intValue();
        if (v instanceof String s) try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
        return null;
    }

    public static Boolean getBoolean(Map<String, Object> props, String key, boolean defaultValue) {
        Object v = props.get(key);
        if (v instanceof Boolean b) return b;
        if (v instanceof String s) return Boolean.parseBoolean(s);
        return defaultValue;
    }

    public static void applyExtensionElements(BaseElement element, Map<String, Object> extensions) {
        if (extensions == null || extensions.isEmpty()) return;

        ExtensionElement root = new ExtensionElement();
        root.setName("extensionElements");
        root.setNamespace("http://flowable.org/modeler");
        root.setNamespacePrefix("modeler");

        for (Map.Entry<String, Object> entry : extensions.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();

            ExtensionElement ee = new ExtensionElement();
            ee.setName(name);
            ee.setNamespace("http://flowable.org/modeler");
            ee.setNamespacePrefix("modeler");

            if (value instanceof Map || value instanceof List) {
                try {
                    ee.setElementText(OM.writeValueAsString(value));
                } catch (Exception e) {
                    ee.setElementText(String.valueOf(value));
                }
            } else {
                ee.setElementText(String.valueOf(value));
            }
            root.addChildElement(ee);
        }

        element.addExtensionElement(root);

        ExtensionAttribute extAttr = new ExtensionAttribute();
        extAttr.setName("modelerExtensions");
        extAttr.setValue("true");
        element.addAttribute(extAttr);
    }

    public static Map<String, Object> readExtensionElements(BaseElement element) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (element == null) return result;

        Map<String, List<ExtensionElement>> extMap = element.getExtensionElements();
        if (extMap == null || extMap.isEmpty()) return result;

        for (List<ExtensionElement> extList : extMap.values()) {
            if (extList == null) continue;
            for (ExtensionElement ext : extList) {
                Map<String, List<ExtensionElement>> children = ext.getChildElements();
                if (children == null) continue;
                for (List<ExtensionElement> childList : children.values()) {
                    if (childList == null) continue;
                    for (ExtensionElement child : childList) {
                        String text = child.getElementText();
                        if (text != null) {
                            try {
                                result.put(child.getName(), OM.readTree(text));
                            } catch (Exception e) {
                                result.put(child.getName(), text);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}
