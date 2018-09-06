package io.choerodon.devops.infra.persistence.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.kubernetes.client.JSON;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.DevopsEnvPodE;
import io.choerodon.devops.domain.application.repository.DevopsEnvPodRepository;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.DevopsEnvPodDO;
import io.choerodon.devops.infra.mapper.DevopsEnvPodMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Zenger on 2018/4/17.
 */
@Service
public class DevopsEnvPodRepositoryImpl implements DevopsEnvPodRepository {

    private static JSON json = new JSON();
    private DevopsEnvPodMapper devopsEnvPodMapper;

    public DevopsEnvPodRepositoryImpl(DevopsEnvPodMapper devopsEnvPodMapper) {
        this.devopsEnvPodMapper = devopsEnvPodMapper;
    }

    @Override
    public DevopsEnvPodE get(Long id) {
        return ConvertHelper.convert(devopsEnvPodMapper.selectByPrimaryKey(id), DevopsEnvPodE.class);
    }

    @Override
    public DevopsEnvPodE get(DevopsEnvPodE pod) {
        List<DevopsEnvPodDO> devopsEnvPodDOS =
                devopsEnvPodMapper.select(ConvertHelper.convert(pod, DevopsEnvPodDO.class));
        if (devopsEnvPodDOS.isEmpty()) {
            return null;
        }
        return ConvertHelper.convert(devopsEnvPodDOS.get(0),
                DevopsEnvPodE.class);
    }

    @Override
    public void insert(DevopsEnvPodE devopsEnvPodE) {
        DevopsEnvPodDO pod = ConvertHelper.convert(devopsEnvPodE, DevopsEnvPodDO.class);
        devopsEnvPodMapper.insert(pod);
    }

    @Override
    public void update(DevopsEnvPodE devopsEnvPodE) {
        devopsEnvPodMapper.updateByPrimaryKey(ConvertHelper.convert(devopsEnvPodE, DevopsEnvPodDO.class));
    }

    @Override
    public List<DevopsEnvPodE> selectByInstanceId(Long instanceId) {
        DevopsEnvPodDO devopsEnvPodDO = new DevopsEnvPodDO();
        devopsEnvPodDO.setAppInstanceId(instanceId);
        return ConvertHelper.convertList(devopsEnvPodMapper.select(devopsEnvPodDO), DevopsEnvPodE.class);
    }

    @Override
    public Page<DevopsEnvPodE> listAppPod(Long projectId, Long envId, Long appId, PageRequest pageRequest, String searchParam) {
        if (pageRequest.getSort() != null) {
            Map<String, String> map = new HashMap<>();
            map.put("name", "dp.`name`");
            map.put("appName", "appName");
            map.put("appVersion", "appVersion");
            map.put("ip", "dp.ip");
            map.put("creationDate", "dp.creation_date");
            pageRequest.resetOrder("dp", map);
        }
        Page<DevopsEnvPodDO> devopsEnvPodDOPage;
        if (!StringUtils.isEmpty(searchParam)) {
            Map<String, Object> searchParamMap = json.deserialize(searchParam, Map.class);
            devopsEnvPodDOPage = PageHelper.doPageAndSort(
                    pageRequest, () -> devopsEnvPodMapper.listAppPod(
                            projectId,
                            envId,
                            appId,
                            TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                            TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM))));
        } else {
            devopsEnvPodDOPage = PageHelper.doPageAndSort(
                    pageRequest, () -> devopsEnvPodMapper.listAppPod(projectId, envId, appId, null, null));
        }

        return ConvertPageHelper.convertPage(devopsEnvPodDOPage, DevopsEnvPodE.class);
    }

    @Override
    public void deleteByName(String name, String namespace) {
        DevopsEnvPodDO devopsEnvPodDO = new DevopsEnvPodDO();
        devopsEnvPodDO.setName(name);
        devopsEnvPodDO.setNamespace(namespace);
        List<DevopsEnvPodDO> devopsEnvPodDOs = devopsEnvPodMapper.select(devopsEnvPodDO);
        if (!devopsEnvPodDOs.isEmpty()) {
            devopsEnvPodMapper.delete(devopsEnvPodDOs.get(0));
        }
    }
}
