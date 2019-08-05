package io.choerodon.devops.infra.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.IOException;
import java.util.LinkedHashMap;

/**
 * Utility for conversion between json and yaml.
 *
 * @author zmf
 */
public class JsonYamlConversionUtil {
    /**
     * the error message code for failure of conversion from json to yaml
     */
    public static final String ERROR_JSON_TO_YAML_FAILED = "error.json.to.yaml.failed";
    /**
     * the error message code for failure of conversion from yaml to json
     */
    public static final String ERROR_YAML_TO_JSON_FAILED = "error.yaml.to.json.failed";

    private JsonYamlConversionUtil() {
    }

    /**
     * convert the yaml string to json string
     *
     * @param yaml the string with yaml format
     * @return the json string
     * @throws IOException if the process failed.
     */
    public static String yaml2json(String yaml) throws IOException {
        LinkedHashMap values = new ObjectMapper(new YAMLFactory()).readValue(yaml, LinkedHashMap.class);
        return new ObjectMapper().writeValueAsString(values);
    }

    /**
     * convert json string to yaml string
     *
     * @param json the string with json format
     * @return the yaml string
     * @throws IOException if the process failed.
     */
    public static String json2yaml(String json) throws IOException {
        JsonNode jsonNodeTree = new ObjectMapper().readTree(json);
        return new YAMLMapper().writeValueAsString(jsonNodeTree);
    }
}
