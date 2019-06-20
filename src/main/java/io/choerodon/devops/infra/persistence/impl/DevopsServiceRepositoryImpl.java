package io.choerodon.devops.infra.persistence.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsServiceE;
import io.choerodon.devops.domain.application.repository.DevopsServiceRepository;
import io.choerodon.devops.domain.application.valueobject.DevopsServiceV;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.DevopsServiceDO;
import io.choerodon.devops.infra.dataobject.DevopsServiceQueryDO;
import io.choerodon.devops.infra.mapper.DevopsServiceMapper;
import io.kubernetes.client.JSON;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

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
    public Boolean checkName(Long envId, String name) {
        DevopsServiceDO devopsServiceDO = new DevopsServiceDO();
        devopsServiceDO.setEnvId(envId);
        devopsServiceDO.setName(name);
        if (devopsServiceMapper.selectOne(devopsServiceDO) != null) {
            return false;
        }
        return true;
    }

    @Override
    public PageInfo<DevopsServiceV> listDevopsServiceByPage(Long projectId, Long envId, Long instanceId, PageRequest pageRequest,
                                                            String searchParam) {

        Sort sort = pageRequest.getSort();
        String sortResult = "";
        if (sort != null) {
            sortResult = Lists.newArrayList(pageRequest.getSort().iterator()).stream()
                    .map(t -> {
                        String property = t.getProperty();
                        if (property.equals("name")) {
                            property = "ds.`name`";
                        } else if (property.equals("envName")) {
                            property = "env_name";
                        } else if (property.equals("externalIp")) {
                            property = "ds.external_ip";
                        } else if (property.equals("targetPort")) {
                            property = "ds.target_port";
                        } else if (property.equals("appName")) {
                            property = "app_name";
                        }
                        return property + " " + t.getDirection();
                    })
                    .collect(Collectors.joining(","));
        }
        int page = pageRequest.getPage();
        int size = (pageRequest.getSize() * pageRequest.getPage()) - 1;
        int start = getBegin(page, size);
        //分页组件暂不支持级联查询，只能手写分页
        PageInfo<DevopsServiceQueryDO> result = new PageInfo();
        result.setPageSize(pageRequest.getSize());
        result.setPageNum(pageRequest.getPage());
        int count;
        List<DevopsServiceQueryDO> devopsServiceQueryDOList;
        if (!StringUtils.isEmpty(searchParam)) {
            Map<String, Object> searchParamMap = json.deserialize(searchParam, Map.class);
            count = devopsServiceMapper.selectCountByName(
                    projectId, envId, instanceId, TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                    TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM)));

            result.setTotal(count);
            devopsServiceQueryDOList = devopsServiceMapper.listDevopsServiceByPage(
                    projectId, envId, instanceId, TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                    TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM)), start, size, sortResult);
            result.setList(devopsServiceQueryDOList);
        } else {
            count = devopsServiceMapper
                    .selectCountByName(projectId, envId, instanceId, null, null);
            result.setTotal(count);
            devopsServiceQueryDOList =
                    devopsServiceMapper.listDevopsServiceByPage(
                            projectId, envId, instanceId, null, null, start, size, sortResult);
            result.setList(devopsServiceQueryDOList);
        }
        if (devopsServiceQueryDOList.size() < pageRequest.getSize()) {
            result.setSize(devopsServiceQueryDOList.size());
        }
        if (devopsServiceQueryDOList.size() < (pageRequest.getPage() * pageRequest.getSize())) {
            result.setSize(devopsServiceQueryDOList.size() - ((pageRequest.getPage() - 1) * pageRequest.getSize()));
        }
        return ConvertPageHelper.convertPageInfo(
                result, DevopsServiceV.class);
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
        if (devopsServiceE.getExternalIp() == null) {
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


    public static int getBegin(int page, int size) {
        page = page <= 1 ? 1 : page;
        return (page - 1) * size;
    }
}
