package io.choerodon.devops.infra.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.constant.PrometheusConstants;
import io.choerodon.devops.infra.dto.DevopsPrometheusDTO;
import io.choerodon.devops.infra.enums.ClusterResourceType;

public class ComponentValuesTemplateUtil {

    private static final String TEMPLATE = "/component/template/%s.yml";

    private ComponentValuesTemplateUtil() {
    }

    public static String convert(ClusterResourceType type, Object object) {
        InputStream in = Optional.ofNullable(ComponentValuesTemplateUtil.class.getResourceAsStream(String.format(TEMPLATE, type.getType())))
                .orElseThrow(() -> new CommonException("error.template.config.file.not.exist"));
        switch (type) {
            case PROMETHEUS:
                return convertPrometheus((DevopsPrometheusDTO) object, in);
        }
        return null;
    }

    /***
     *
     * @param devopsPrometheusDTO 普罗米修斯配置数据
     * @param in yaml文件输入流
     * @return
     */
    public static String convertPrometheus(DevopsPrometheusDTO devopsPrometheusDTO, InputStream in) {
        Map<String, String> map = new HashMap<>();
        map.put("{{adminPassword}}", devopsPrometheusDTO.getAdminPassword());
        map.put("{{host}}", devopsPrometheusDTO.getGrafanaDomain());
        map.put("{{clusterName}}", devopsPrometheusDTO.getClusterCode());
        devopsPrometheusDTO.getDevopsPvcDTO().stream().forEach(devopsPvcDTO -> {
            if (PrometheusConstants.PvcName.prometheus_pvcName.equals(devopsPvcDTO.getName())) {
                map.put("{{prometheus-pvc}}", devopsPvcDTO.getName());
            }
            if (PrometheusConstants.PvcName.alertManager_pvcName.equals(devopsPvcDTO.getName())) {
                map.put("{{alertmanager-pvc}}", devopsPvcDTO.getName());
            }
            if (PrometheusConstants.PvcName.grafana_pvcName.equals(devopsPvcDTO.getName())) {
                map.put("{{grafana-pvc}}", devopsPvcDTO.getName());
            }
        });


        return FileUtil.replaceReturnString(in, map);
    }

}
