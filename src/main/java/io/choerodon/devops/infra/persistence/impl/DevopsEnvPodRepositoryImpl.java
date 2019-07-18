package io.choerodon.devops.infra.persistence.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import io.choerodon.base.domain.Sort;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvPodE;
import io.choerodon.devops.domain.application.repository.DevopsEnvPodRepository;
<<<<<<< HEAD
import io.choerodon.devops.infra.dto.DevopsEnvPodDTO;
=======
>>>>>>> [IMP]修改后端代码结构
import io.choerodon.devops.infra.util.TypeUtil;
<<<<<<< HEAD
=======
>>>>>>> [IMP] 重构Repository
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
    public DevopsEnvPodE baseQueryById(Long id) {
        return ConvertHelper.convert(devopsEnvPodMapper.selectByPrimaryKey(id), DevopsEnvPodE.class);
    }

    @Override
<<<<<<< HEAD
    public DevopsEnvPodE get(DevopsEnvPodE pod) {
        List<DevopsEnvPodDTO> devopsEnvPodDTOS =
                devopsEnvPodMapper.select(ConvertHelper.convert(pod, DevopsEnvPodDTO.class));
        if (devopsEnvPodDTOS.isEmpty()) {
=======
    public DevopsEnvPodE baseQueryByPod(DevopsEnvPodE pod) {
        List<DevopsEnvPodDTO> devopsEnvPodDOS =
                devopsEnvPodMapper.select(ConvertHelper.convert(pod, DevopsEnvPodDTO.class));
        if (devopsEnvPodDOS.isEmpty()) {
>>>>>>> [IMP] 重构Repository
            return null;
        }
        return ConvertHelper.convert(devopsEnvPodDTOS.get(0),
                DevopsEnvPodE.class);
    }

    @Override
<<<<<<< HEAD
    public void insert(DevopsEnvPodE devopsEnvPodE) {
        DevopsEnvPodDTO devopsEnvPodDTO = new DevopsEnvPodDTO();
        devopsEnvPodDTO.setName(devopsEnvPodE.getName());
        devopsEnvPodDTO.setNamespace(devopsEnvPodE.getNamespace());
        if (devopsEnvPodMapper.selectOne(devopsEnvPodDTO) == null) {
            DevopsEnvPodDTO pod = ConvertHelper.convert(devopsEnvPodE, DevopsEnvPodDTO.class);
=======
    public void baseCreate(DevopsEnvPodE devopsEnvPodE) {
        DevopsEnvPodDTO devopsEnvPodDO = new DevopsEnvPodDTO();
        devopsEnvPodDO.setName(devopsEnvPodE.getName());
        devopsEnvPodDO.setNamespace(devopsEnvPodE.getNamespace());
        if (devopsEnvPodMapper.selectOne(devopsEnvPodDO) == null) {
            DevopsEnvPodDTO pod = ConvertHelper.convert(devopsEnvPodE, DevopsEnvPodDTO.class);
>>>>>>> [IMP] 重构Repository
            devopsEnvPodMapper.insert(pod);
        }
    }

    @Override
<<<<<<< HEAD
    public void update(DevopsEnvPodE devopsEnvPodE) {
        devopsEnvPodMapper.updateByPrimaryKey(ConvertHelper.convert(devopsEnvPodE, DevopsEnvPodDTO.class));
    }

    @Override
    public List<DevopsEnvPodE> selectByInstanceId(Long instanceId) {
        DevopsEnvPodDTO devopsEnvPodDTO = new DevopsEnvPodDTO();
        devopsEnvPodDTO.setAppInstanceId(instanceId);
        return ConvertHelper.convertList(devopsEnvPodMapper.select(devopsEnvPodDTO), DevopsEnvPodE.class);
=======
    public void baseUpdate(DevopsEnvPodE devopsEnvPodE) {
        devopsEnvPodMapper.updateByPrimaryKey(ConvertHelper.convert(devopsEnvPodE, DevopsEnvPodDTO.class));
    }

    @Override
    public List<DevopsEnvPodE> baseListByInstanceId(Long instanceId) {
        DevopsEnvPodDTO devopsEnvPodDO = new DevopsEnvPodDTO();
        devopsEnvPodDO.setAppInstanceId(instanceId);
        return ConvertHelper.convertList(devopsEnvPodMapper.select(devopsEnvPodDO), DevopsEnvPodE.class);
>>>>>>> [IMP] 重构Repository
    }

    @Override
    public PageInfo<DevopsEnvPodE> basePageByIds(Long projectId, Long envId, Long appId, Long instanceId, PageRequest pageRequest, String searchParam) {

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
<<<<<<< HEAD
        PageInfo<DevopsEnvPodDTO> devopsEnvPodDOPage;
=======
        PageInfo<DevopsEnvPodDTO> devopsEnvPodDOPage;
>>>>>>> [IMP] 重构Repository
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
<<<<<<< HEAD
    public void deleteByName(String name, String namespace) {
        DevopsEnvPodDTO devopsEnvPodDTO = new DevopsEnvPodDTO();
        devopsEnvPodDTO.setName(name);
        devopsEnvPodDTO.setNamespace(namespace);
        List<DevopsEnvPodDTO> devopsEnvPodDTOS = devopsEnvPodMapper.select(devopsEnvPodDTO);
        if (!devopsEnvPodDTOS.isEmpty()) {
            devopsEnvPodMapper.delete(devopsEnvPodDTOS.get(0));
=======
    public void baseDeleteByName(String name, String namespace) {
        DevopsEnvPodDTO devopsEnvPodDO = new DevopsEnvPodDTO();
        devopsEnvPodDO.setName(name);
        devopsEnvPodDO.setNamespace(namespace);
        List<DevopsEnvPodDTO> devopsEnvPodDOs = devopsEnvPodMapper.select(devopsEnvPodDO);
        if (!devopsEnvPodDOs.isEmpty()) {
            devopsEnvPodMapper.delete(devopsEnvPodDOs.get(0));
>>>>>>> [IMP] 重构Repository
        }
    }

    @Override
<<<<<<< HEAD
    public DevopsEnvPodE getByNameAndEnv(String name, String namespace) {
        DevopsEnvPodDTO devopsEnvPodDTO = new DevopsEnvPodDTO();
        devopsEnvPodDTO.setName(name);
        devopsEnvPodDTO.setNamespace(namespace);
        return ConvertHelper.convert(devopsEnvPodMapper.selectOne(devopsEnvPodDTO), DevopsEnvPodE.class);
=======
    public DevopsEnvPodE queryByNameAndEnvName(String name, String namespace) {
        DevopsEnvPodDTO devopsEnvPodDO = new DevopsEnvPodDTO();
        devopsEnvPodDO.setName(name);
        devopsEnvPodDO.setNamespace(namespace);
        return ConvertHelper.convert(devopsEnvPodMapper.selectOne(devopsEnvPodDO), DevopsEnvPodE.class);
>>>>>>> [IMP] 重构Repository
    }
}
