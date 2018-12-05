package io.choerodon.devops.infra.common.util

import spock.lang.Specification

/**
 *
 * @author zmf
 *
 */
class JsonYamlConversionUtilSpec extends Specification {
    def "Yaml2json"() {
        given: "准备数据"
        String yaml = "---\n" +
                "replicaCount: 1\n" +
                "image:\n" +
                "  repository: \"registry.cnhangzhou.aliyuncs.com/choerodon/manager-service\"\n" +
                "  pullPolicy: \"Always\""
        String json = "{\"replicaCount\":1,\"image\":{\"repository\":\"registry.cnhangzhou.aliyuncs.com/choerodon/manager-service\",\"pullPolicy\":\"Always\"}}"

        when: "准备数据"
        def result = JsonYamlConversionUtil.yaml2json(yaml)

        then: "校验结果"
        result == json
    }

    def "Json2yaml"() {
        given: "准备数据"
        String yaml = "---\n" +
                "replicaCount: 1\n" +
                "image:\n" +
                "  repository: \"registry.cnhangzhou.aliyuncs.com/choerodon/manager-service\"\n" +
                "  pullPolicy: \"Always\""
        String json = "{\"replicaCount\":1,\"image\":{\"repository\":\"registry.cnhangzhou.aliyuncs.com/choerodon/manager-service\",\"pullPolicy\":\"Always\"}}"

        when: "准备数据"
        def result = JsonYamlConversionUtil.json2yaml(json)

        then: "校验结果"
        result.trim() == yaml.trim()
    }
}
