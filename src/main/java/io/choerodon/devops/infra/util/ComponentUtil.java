package io.choerodon.devops.infra.util;

import io.choerodon.devops.infra.dto.DevopsPrometheusDTO;
import io.choerodon.devops.infra.enums.ComponentType;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ComponentUtil {

    private ComponentUtil(){

    }

    /***
     *
     * @param devopsPrometheusDTO 普罗米修斯配置数据
     * @param in yaml文件输入流
     * @return
     */
    public static String convertPrometheus(DevopsPrometheusDTO devopsPrometheusDTO, InputStream in){
        Map<String,String> map = new HashMap<>();
        map.put("{{adminPassoword}}",devopsPrometheusDTO.getAdminPassword());
        map.put("{{host}}",devopsPrometheusDTO.getGrafanaDomain());
        map.put("{{clustername}}",devopsPrometheusDTO.getClusterName());
        return FileUtil.replaceReturnString(in, map);
    }

}
