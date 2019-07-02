package io.choerodon.devops.app.service.impl;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.dto.ApplicationRepDTO;
import io.choerodon.devops.api.dto.DevopsEnvApplicationDTO;
import io.choerodon.devops.api.dto.DevopsEnvLabelDTO;
import io.choerodon.devops.api.dto.DevopsEnvPortDTO;
import io.choerodon.devops.app.service.ApplicationInstanceService;
import io.choerodon.devops.app.service.ApplicationService;
import io.choerodon.devops.app.service.DevopsEnvApplicationService;
import io.choerodon.devops.domain.application.entity.DevopsEnvApplicationE;
import io.choerodon.devops.domain.application.entity.DevopsEnvMessageE;
import io.choerodon.devops.domain.application.repository.DevopsEnvApplicationRepostitory;
import io.choerodon.devops.infra.dataobject.DevopsEnvApplicationDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author lizongwei
 * @date 2019/7/1
 */
@Service
public class DevopsEnvApplicationServiceImpl implements DevopsEnvApplicationService {

    private static Gson gson = new Gson();

    @Autowired
    DevopsEnvApplicationRepostitory devopsEnvApplicationRepostitory;

    @Autowired
    ApplicationService applicationService;

    @Autowired
    ApplicationInstanceService instanceService;

    @Override
    public DevopsEnvApplicationDTO create(DevopsEnvApplicationDTO devopsEnvApplicationDTO) {
        return ConvertHelper.convert(devopsEnvApplicationRepostitory.create(
                ConvertHelper.convert(devopsEnvApplicationDTO,DevopsEnvApplicationE.class)),DevopsEnvApplicationDTO.class);
    }

    @Override
    public List<ApplicationRepDTO> queryAppByEnvId(Long envId) {
        List<Long> appIds =  devopsEnvApplicationRepostitory.queryAppByEnvId(envId);
        return applicationService.queryApps(appIds);
    }

    @Override
    public void syncEnvAppRelevance() {
        List<DevopsEnvApplicationE> envApplicationES = instanceService.listAllEnvApp();
        envApplicationES.stream().distinct().forEach(v->devopsEnvApplicationRepostitory.create(v));
    }

    @Override
    public List<DevopsEnvLabelDTO> queryLabelByAppEnvId(Long envId, Long appId) {
        List<DevopsEnvMessageE> messageES = devopsEnvApplicationRepostitory.listResourceByEnvAndApp(envId,appId);
        List<DevopsEnvLabelDTO> labelDTOS = new ArrayList<>();
        messageES.forEach(v->{
            DevopsEnvLabelDTO labelDTO = new DevopsEnvLabelDTO();
            labelDTO.setResourceName(v.getResourceName());
            Map map = gson.fromJson(v.getDetail(),Map.class);
            labelDTO.setLabels((LinkedTreeMap) ((Map)((Map)map.get("spec")).get("selector")).get("matchLabels"));
            labelDTOS.add(labelDTO);
        });
        return labelDTOS;
    }

    @Override
    public List<DevopsEnvPortDTO> queryPortByAppEnvId(Long envId, Long appId) {
        List<DevopsEnvMessageE> messageES = devopsEnvApplicationRepostitory.listResourceByEnvAndApp(envId,appId);
        List<DevopsEnvPortDTO> portDTOS = new ArrayList<>();
        messageES.forEach(v->{
            Map map = gson.fromJson(v.getDetail(),Map.class);
            List<LinkedTreeMap> containers = (ArrayList<LinkedTreeMap>)((Map)((Map)((Map)map.get("spec")).get("template")).get("spec")).get("containers");

            for(LinkedTreeMap container:containers) {
                List<LinkedTreeMap> ports = (ArrayList<LinkedTreeMap>)container.get("ports");

                Optional.ofNullable(ports).ifPresent(portList -> {
                    for (LinkedTreeMap port : portList) {
                        DevopsEnvPortDTO portDTO = new DevopsEnvPortDTO();
                        portDTO.setResourceName(v.getResourceName());
                        portDTO.setPortName((String) port.get("name"));
                        portDTO.setPortValue((Double) port.get("containerPort"));
                        portDTOS.add(portDTO);
                    }
                });
            }
        });
        return portDTOS;
    }

}
