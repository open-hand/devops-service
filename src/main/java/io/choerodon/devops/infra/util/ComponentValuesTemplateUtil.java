package io.choerodon.devops.infra.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.dto.DevopsPrometheusDTO;
import io.choerodon.devops.infra.dto.DevopsPvcDTO;
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
        List<String> pvcNames = devopsPrometheusDTO.getDevopsPvcDTO().stream().map(DevopsPvcDTO::getName).collect(Collectors.toList());
        if (pvcNames.size() == 3) {
            map.put("{{prometheus-pvc}}", pvcNames.get(0));
            map.put("{{alertmanager-pvc}}", pvcNames.get(1));
            map.put("{{grafana-pvc}}", pvcNames.get(2));
        }else {
            throw new CommonException("error.pvc.name.size");
        }

        return FileUtil.replaceReturnString(in, map);
    }

}
