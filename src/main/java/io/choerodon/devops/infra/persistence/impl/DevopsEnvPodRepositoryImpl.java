package io.choerodon.devops.infra.persistence.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvPodE;
import io.choerodon.devops.domain.application.repository.DevopsEnvPodRepository;
import io.choerodon.devops.infra.dto.DevopsEnvironmentPodDTO;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.mapper.DevopsEnvPodMapper;
import io.kubernetes.client.JSON;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

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
        List<DevopsEnvironmentPodDTO> devopsEnvironmentPodDTOS =
                devopsEnvPodMapper.select(ConvertHelper.convert(pod, DevopsEnvironmentPodDTO.class));
        if (devopsEnvironmentPodDTOS.isEmpty()) {
            return null;
        }
        return ConvertHelper.convert(devopsEnvironmentPodDTOS.get(0),
                DevopsEnvPodE.class);
    }

    @Override
    public void insert(DevopsEnvPodE devopsEnvPodE) {
        DevopsEnvironmentPodDTO devopsEnvironmentPodDTO = new DevopsEnvironmentPodDTO();
        devopsEnvironmentPodDTO.setName(devopsEnvPodE.getName());
        devopsEnvironmentPodDTO.setNamespace(devopsEnvPodE.getNamespace());
        if (devopsEnvPodMapper.selectOne(devopsEnvironmentPodDTO) == null) {
            DevopsEnvironmentPodDTO pod = ConvertHelper.convert(devopsEnvPodE, DevopsEnvironmentPodDTO.class);
            devopsEnvPodMapper.insert(pod);
        }
    }

    @Override
    public void update(DevopsEnvPodE devopsEnvPodE) {
        devopsEnvPodMapper.updateByPrimaryKey(ConvertHelper.convert(devopsEnvPodE, DevopsEnvironmentPodDTO.class));
    }

    @Override
    public List<DevopsEnvPodE> selectByInstanceId(Long instanceId) {
        DevopsEnvironmentPodDTO devopsEnvironmentPodDTO = new DevopsEnvironmentPodDTO();
        devopsEnvironmentPodDTO.setAppInstanceId(instanceId);
        return ConvertHelper.convertList(devopsEnvPodMapper.select(devopsEnvironmentPodDTO), DevopsEnvPodE.class);
    }

    @Override
    public PageInfo<DevopsEnvPodE> listAppPod(Long projectId, Long envId, Long appId, Long instanceId, PageRequest pageRequest, String searchParam) {

        Sort sort = pageRequest.getSort();
        String sortResult = "";
        if (sort != null) {
            sortResult = Lists.newArrayList(pageRequest.getSort().iterator()).stream()
                    .map(t -> {
                        String property = t.getProperty();
                        if (property.equals("name")) {
                            property = "dp.`name`";
                        } else if (property.equals("ip")) {
                            property = "dp.ip";
                        } else if (property.equals("creationDate")) {
                            property = "dp.creation_date";
                        }

                        return property + " " + t.getDirection();
                    })
                    .collect(Collectors.joining(","));
        }
        PageInfo<DevopsEnvironmentPodDTO> devopsEnvPodDOPage;
        if (!StringUtils.isEmpty(searchParam)) {
            Map<String, Object> searchParamMap = json.deserialize(searchParam, Map.class);
            devopsEnvPodDOPage = PageHelper.startPage(
                    pageRequest.getPage(), pageRequest.getSize(), sortResult).doSelectPageInfo(() -> devopsEnvPodMapper.listAppPod(
                    projectId,
                    envId,
                    appId,
                    instanceId,
                    TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                    TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM))));
        } else {
            devopsEnvPodDOPage = PageHelper.startPage(
                    pageRequest.getPage(), pageRequest.getSize(), sortResult).doSelectPageInfo(() -> devopsEnvPodMapper.listAppPod(projectId, envId, appId, instanceId, null, null));
        }

        return ConvertPageHelper.convertPageInfo(devopsEnvPodDOPage, DevopsEnvPodE.class);
    }

    @Override
    public void deleteByName(String name, String namespace) {
        DevopsEnvironmentPodDTO devopsEnvironmentPodDTO = new DevopsEnvironmentPodDTO();
        devopsEnvironmentPodDTO.setName(name);
        devopsEnvironmentPodDTO.setNamespace(namespace);
        List<DevopsEnvironmentPodDTO> devopsEnvironmentPodDTOS = devopsEnvPodMapper.select(devopsEnvironmentPodDTO);
        if (!devopsEnvironmentPodDTOS.isEmpty()) {
            devopsEnvPodMapper.delete(devopsEnvironmentPodDTOS.get(0));
        }
    }

    @Override
    public DevopsEnvPodE getByNameAndEnv(String name, String namespace) {
        DevopsEnvironmentPodDTO devopsEnvironmentPodDTO = new DevopsEnvironmentPodDTO();
        devopsEnvironmentPodDTO.setName(name);
        devopsEnvironmentPodDTO.setNamespace(namespace);
        return ConvertHelper.convert(devopsEnvPodMapper.selectOne(devopsEnvironmentPodDTO), DevopsEnvPodE.class);
    }
}
