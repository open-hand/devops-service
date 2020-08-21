package io.choerodon.devops.infra.util;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.dto.DevopsPrometheusDTO;
import io.choerodon.devops.infra.enums.ClusterResourceType;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ComponentValuesTemplateUtil {
    private static final String TEMPLATE = "/component/template/%s.yml";

    private ComponentValuesTemplateUtil() {
    }

    public static String convert(ClusterResourceType type, Object object, Map<String, Object> extraData) {
        InputStream in = Optional.ofNullable(ComponentValuesTemplateUtil.class.getResourceAsStream(String.format(TEMPLATE, type.getType())))
                .orElseThrow(() -> new CommonException("error.template.config.file.not.exist"));
        switch (type) {
            case PROMETHEUS:
                return convertPrometheus((DevopsPrometheusDTO) object, in, extraData);
        }
        return null;
    }

    /***
     *
     * @param devopsPrometheusDTO 普罗米修斯配置数据
     * @param in                  yaml文件输入流
     * @return 替换完毕的values文件
     */
    public static String convertPrometheus(DevopsPrometheusDTO devopsPrometheusDTO, InputStream in, Map<String, Object> extraData) {
        String apiHost = (String) Optional.ofNullable(extraData.get("apiHost")).orElseThrow(() -> new CommonException("error.api.host"));
        Map<String, String> map = new HashMap<>();
        map.put("{{adminPassword}}", devopsPrometheusDTO.getAdminPassword());
        map.put("{{host}}", devopsPrometheusDTO.getGrafanaDomain());
        map.put("{{clusterName}}", devopsPrometheusDTO.getClusterCode());

        map.put("{{prometheus-pv}}", devopsPrometheusDTO.getPrometheusPv().getName());
        map.put("{{prometheusAccessMode}}", devopsPrometheusDTO.getPrometheusPv().getAccessModes());
        map.put("{{prometheusStorage}}", devopsPrometheusDTO.getPrometheusPv().getRequestResource());

        map.put("{{alertmanager-pv}}", devopsPrometheusDTO.getAltermanagerPv().getName());
        map.put("{{altermanagerAccesssMode}}", devopsPrometheusDTO.getAltermanagerPv().getAccessModes());
        map.put("{{altermanagerStorage}}", devopsPrometheusDTO.getAltermanagerPv().getRequestResource());


        map.put("{{grafana-pv}}", devopsPrometheusDTO.getGrafanaPv().getName());
        map.put("{{grafanaAccessMode}}", devopsPrometheusDTO.getGrafanaPv().getAccessModes());
        map.put("{{grafanaStorage}}", devopsPrometheusDTO.getGrafanaPv().getRequestResource());
        map.put("{{grafana-client-id}}", devopsPrometheusDTO.getClientName());

        map.put("{{api-host}}", apiHost);

        String values = FileUtil.replaceReturnString(in, map);
        return filterTls(devopsPrometheusDTO.getEnableTls(), values);
    }

    public static String filterTls(Boolean enableTls, String values) {
        if (enableTls) {
            return values;
        } else {
            Yaml yaml = new Yaml();
            HashMap load = yaml.load(values);
            ((HashMap) ((HashMap) load.get("grafana")).get("ingress")).remove("tls");
            ((HashMap) ((HashMap) ((HashMap) load.get("grafana")).get("ingress")).get("annotations")).remove("kubernetes.io/tls-acme");
            ((HashMap) ((HashMap) ((HashMap) load.get("grafana")).get("ingress")).get("annotations")).remove("certmanager.k8s.io/cluster-issuer");
            ((HashMap) ((HashMap) ((HashMap) load.get("grafana")).get("ingress")).get("annotations")).remove("kubernetes.io/ingress.class");
            return yaml.dumpAsMap(load);
        }
    }
}
