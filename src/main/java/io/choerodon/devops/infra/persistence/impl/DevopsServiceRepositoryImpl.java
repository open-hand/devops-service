package io.choerodon.devops.infra.persistence.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import io.kubernetes.client.JSON;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.domain.PageInfo;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsServiceE;
import io.choerodon.devops.domain.application.repository.DevopsServiceRepository;
import io.choerodon.devops.domain.application.valueobject.DevopsServiceV;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.DevopsServiceDO;
import io.choerodon.devops.infra.dataobject.DevopsServiceQueryDO;
import io.choerodon.devops.infra.mapper.DevopsServiceMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Zenger on 2018/4/13.
 */
@Component
public class DevopsServiceRepositoryImpl implements DevopsServiceRepository {

    private static JSON json = new JSON();
    private DevopsServiceMapper devopsServiceMapper;

    public DevopsServiceRepositoryImpl(DevopsServiceMapper devopsServiceMapper) {
        this.devopsServiceMapper = devopsServiceMapper;
    }

    @Override
    public Boolean checkName(Long projectId, Long envId, String name) {
        int selectCount = devopsServiceMapper.selectCountByOptions(projectId, envId, name);
        return selectCount <= 0;
    }

    @Override
    public Page<DevopsServiceV> listDevopsServiceByPage(Long projectId, Long envId, PageRequest pageRequest,
                                                        String searchParam) {
        String sort = Lists.newArrayList(pageRequest.getSort().iterator()).stream()
                .filter(t -> checkServiceParam(t.getProperty()))
                .map(t -> t.getProperty() + " " + t.getDirection())
                .collect(Collectors.joining(","));
        if (pageRequest.getSort() != null) {
            Map<String, String> map = new HashMap<>();
            map.put("name", "ds.`name`");
            map.put("envName", "env_name");
            map.put("externalIp", "ds.external_ip");
            map.put("targetPort", "ds.target_port");
            map.put("appName", "app_name");
            map.put("version", "version");
            pageRequest.resetOrder("ds", map);
        }
        int page = pageRequest.getPage();
        int size = pageRequest.getSize();
        int start = page * size;
        //分页组件暂不支持级联查询，只能手写分页
        PageInfo pageInfo = new PageInfo(page, size);
        int count;
        List<DevopsServiceQueryDO> devopsServiceQueryDOList;
        if (!StringUtils.isEmpty(searchParam)) {
            Map<String, Object> searchParamMap = json.deserialize(searchParam, Map.class);
            count = devopsServiceMapper.selectCountByName(
                    projectId, envId, TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                    TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM)));
            devopsServiceQueryDOList = PageHelper.doSort(
                    pageRequest.getSort(), () -> devopsServiceMapper.listDevopsServiceByPage(
                            projectId, envId, TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                            TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM)), start, size, sort));
        } else {
            count = devopsServiceMapper
                    .selectCountByName(projectId, envId, null, null);
            devopsServiceQueryDOList = PageHelper.doSort(pageRequest.getSort(), () ->
                    devopsServiceMapper.listDevopsServiceByPage(
                            projectId, envId, null, null, start, size, sort));
        }
        return ConvertPageHelper.convertPage(
                new Page<>(devopsServiceQueryDOList, pageInfo, count), DevopsServiceV.class);
    }

    private Boolean checkServiceParam(String key) {
        return key.equals("id") || key.equals("name") || key.equals("status");
    }

    @Override
    public List<DevopsServiceV> listDevopsService(Long envId) {
        List<DevopsServiceQueryDO> devopsServiceQueryDOList = devopsServiceMapper.listDevopsService(envId);
        return ConvertHelper.convertList(devopsServiceQueryDOList, DevopsServiceV.class);
    }

    @Override
    public DevopsServiceV selectById(Long id) {
        return ConvertHelper.convert(devopsServiceMapper.selectById(id), DevopsServiceV.class);
    }

    @Override
    public DevopsServiceE insert(DevopsServiceE devopsServiceE) {
        DevopsServiceDO devopsServiceDO = ConvertHelper.convert(
                devopsServiceE, DevopsServiceDO.class);
        if (devopsServiceMapper.insert(devopsServiceDO) != 1) {
            throw new CommonException("error.k8s.service.create");
        }
        return ConvertHelper.convert(devopsServiceDO, DevopsServiceE.class);
    }

    @Override
    public DevopsServiceE query(Long id) {
        return ConvertHelper.convert(devopsServiceMapper.selectByPrimaryKey(id), DevopsServiceE.class);
    }

    @Override
    public void delete(Long id) {
        devopsServiceMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void update(DevopsServiceE devopsServiceE) {
        DevopsServiceDO devopsServiceDO = devopsServiceMapper.selectByPrimaryKey(devopsServiceE.getId());
        DevopsServiceDO devopsServiceDOUpdate = ConvertHelper.convert(devopsServiceE, DevopsServiceDO.class);
        if (devopsServiceE.getLabels() == null) {
            devopsServiceMapper.setLablesToNull(devopsServiceE.getId());
        }
        if(devopsServiceE.getExternalIp() == null) {
            devopsServiceMapper.setExternalIpNull(devopsServiceE.getId());
        }
        devopsServiceDOUpdate.setObjectVersionNumber(devopsServiceDO.getObjectVersionNumber());
        if (devopsServiceMapper.updateByPrimaryKeySelective(devopsServiceDOUpdate) != 1) {
            throw new CommonException("error.k8s.service.update");
        }
    }

    @Override
    public void setLablesToNull(Long id) {
        devopsServiceMapper.setLablesToNull(id);
    }

    @Override
    public void setEndPointToNull(Long id) {
        devopsServiceMapper.setEndPointToNull(id);
    }

    @Override
    public List<Long> selectDeployedEnv() {
        return devopsServiceMapper.selectDeployedEnv();
    }

    @Override
    public DevopsServiceE selectByNameAndEnvId(String name, Long envId) {
        DevopsServiceDO devopsServiceDO = new DevopsServiceDO();
        devopsServiceDO.setName(name);
        devopsServiceDO.setEnvId(envId);
        return ConvertHelper.convert(devopsServiceMapper.selectOne(devopsServiceDO), DevopsServiceE.class);
    }

    @Override
    public Boolean checkEnvHasService(Long envId) {
        return devopsServiceMapper.checkEnvHasService(envId);
    }

    @Override
    public List<DevopsServiceE> list() {
        return ConvertHelper.convertList(devopsServiceMapper.selectAll(), DevopsServiceE.class);
    }

    @Override
    public List<DevopsServiceE> selectByEnvId(Long envId) {
        DevopsServiceDO devopsServiceDO = new DevopsServiceDO();
        devopsServiceDO.setEnvId(envId);
        return ConvertHelper.convertList(devopsServiceMapper.select(devopsServiceDO), DevopsServiceE.class);
    }

    @Override
    public void deleteServiceAndInstanceByEnvId(Long envId) {
        DevopsServiceDO devopsServiceDO = new DevopsServiceDO();
        devopsServiceDO.setEnvId(envId);
        // 环境下的serviceIds
        List<Long> serviceIds = devopsServiceMapper.select(devopsServiceDO).stream().map(DevopsServiceDO::getId)
                .collect(Collectors.toList());
        devopsServiceMapper.delete(devopsServiceDO);
        if (!serviceIds.isEmpty()) {
            devopsServiceMapper.deleteServiceInstance(serviceIds);
        }
    }
}
