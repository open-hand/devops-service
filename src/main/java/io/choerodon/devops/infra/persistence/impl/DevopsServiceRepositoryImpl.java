package io.choerodon.devops.infra.persistence.impl;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import io.kubernetes.client.JSON;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.repository.DevopsServiceRepository;
import io.choerodon.devops.domain.application.valueobject.DevopsServiceV;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.dto.DevopsServiceDTO;
import io.choerodon.devops.infra.dto.DevopsServiceQueryDTO;
import io.choerodon.devops.infra.mapper.DevopsServiceMapper;

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
    public Boolean baseCheckName(Long envId, String name) {
        DevopsServiceDTO devopsServiceDTO = new DevopsServiceDTO();
        devopsServiceDTO.setEnvId(envId);
        devopsServiceDTO.setName(name);
        if (devopsServiceMapper.selectOne(devopsServiceDTO) != null) {
            return false;
        }
        return true;
    }

    @Override
    public PageInfo<DevopsServiceV> basePageByOptions(Long projectId, Long envId, Long instanceId, PageRequest pageRequest,
                                                      String searchParam, Long appId) {

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

        int start = getBegin(pageRequest.getPage(), pageRequest.getSize());
        int stop = start + pageRequest.getSize();
        //分页组件暂不支持级联查询，只能手写分页
        PageInfo<DevopsServiceQueryDTO> result = new PageInfo();
        result.setPageSize(pageRequest.getSize());
        result.setPageNum(pageRequest.getPage());
        int count;
        List<DevopsServiceQueryDTO> devopsServiceQueryDTOList;
        if (!StringUtils.isEmpty(searchParam)) {
            Map<String, Object> searchParamMap = json.deserialize(searchParam, Map.class);
            count = devopsServiceMapper.selectCountByName(
                    projectId, envId, instanceId, TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                    TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM)), appId);

            result.setTotal(count);
            devopsServiceQueryDTOList = devopsServiceMapper.listDevopsServiceByPage(
                    projectId, envId, instanceId, TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                    TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM)), sortResult, appId);
            result.setList(devopsServiceQueryDTOList.subList(start, stop > devopsServiceQueryDTOList.size() ? devopsServiceQueryDTOList.size() : stop));
        } else {
            count = devopsServiceMapper
                    .selectCountByName(projectId, envId, instanceId, null, null, appId);
            result.setTotal(count);
            devopsServiceQueryDTOList =
                    devopsServiceMapper.listDevopsServiceByPage(
                            projectId, envId, instanceId, null, null, sortResult, appId);
            result.setList(devopsServiceQueryDTOList.subList(start, stop > devopsServiceQueryDTOList.size() ? devopsServiceQueryDTOList.size() : stop));
        }
        if (devopsServiceQueryDTOList.size() < pageRequest.getSize() * pageRequest.getPage()) {
            result.setSize(TypeUtil.objToInt(devopsServiceQueryDTOList.size()) - (pageRequest.getSize() * (pageRequest.getPage() - 1)));
        } else {
            result.setSize(pageRequest.getSize());
        }
        return ConvertPageHelper.convertPageInfo(
                result, DevopsServiceV.class);
    }

    private Boolean checkServiceParam(String key) {
        return key.equals("id") || key.equals("name") || key.equals("status");
    }

    @Override
    public List<DevopsServiceV> baseListByEnvId(Long envId) {
        List<DevopsServiceQueryDTO> devopsServiceQueryDTOList = devopsServiceMapper.listByEnvId(envId);
        return ConvertHelper.convertList(devopsServiceQueryDTOList, DevopsServiceV.class);
    }

    @Override
    public DevopsServiceV baseQueryById(Long id) {
        return ConvertHelper.convert(devopsServiceMapper.queryById(id), DevopsServiceV.class);
    }

    @Override
    public DevopsServiceE insert(DevopsServiceE devopsServiceE) {
        DevopsServiceDTO devopsServiceDTO = ConvertHelper.convert(
                devopsServiceE, DevopsServiceDTO.class);
        if (devopsServiceMapper.insert(devopsServiceDTO) != 1) {
            throw new CommonException("error.k8s.service.create");
        }
        return ConvertHelper.convert(devopsServiceDTO, DevopsServiceE.class);
    }

    @Override
    public DevopsServiceE baseQuery(Long id) {
        return ConvertHelper.convert(devopsServiceMapper.selectByPrimaryKey(id), DevopsServiceE.class);
    }

    @Override
    public void baseDelete(Long id) {
        devopsServiceMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void baseUpdate(DevopsServiceE devopsServiceE) {
        DevopsServiceDTO devopsServiceDTO = devopsServiceMapper.selectByPrimaryKey(devopsServiceE.getId());
        DevopsServiceDTO devopsServiceDTOUpdate = ConvertHelper.convert(devopsServiceE, DevopsServiceDTO.class);
        if (devopsServiceE.getLabels() == null) {
            devopsServiceMapper.updateLables(devopsServiceE.getId());
        }
        if (devopsServiceE.getExternalIp() == null) {
            devopsServiceMapper.setExternalIpNull(devopsServiceE.getId());
        }
        devopsServiceDTOUpdate.setObjectVersionNumber(devopsServiceDTO.getObjectVersionNumber());
        if (devopsServiceMapper.updateByPrimaryKeySelective(devopsServiceDTOUpdate) != 1) {
            throw new CommonException("error.k8s.service.update");
        }
    }

    @Override
    public void baseUpdateLables(Long id) {
        devopsServiceMapper.updateLables(id);
    }

    @Override
    public void baseUpdateEndPoint(Long id) {
        devopsServiceMapper.updateEndPoint(id);
    }

    @Override
    public List<Long> baseListEnvByRunningService() {
        return devopsServiceMapper.selectDeployedEnv();
    }

    @Override
    public DevopsServiceE baseQueryByNameAndEnvId(String name, Long envId) {
        DevopsServiceDTO devopsServiceDTO = new DevopsServiceDTO();
        devopsServiceDTO.setName(name);
        devopsServiceDTO.setEnvId(envId);
        return ConvertHelper.convert(devopsServiceMapper.selectOne(devopsServiceDTO), DevopsServiceE.class);
    }

    @Override
    public Boolean baseCheckServiceByEnv(Long envId) {
        return devopsServiceMapper.checkServiceByEnv(envId);
    }

    @Override
    public List<DevopsServiceE> baseList() {
        return ConvertHelper.convertList(devopsServiceMapper.selectAll(), DevopsServiceE.class);
    }

    @Override
    public void baseDeleteServiceAndInstanceByEnvId(Long envId) {
        DevopsServiceDTO devopsServiceDTO = new DevopsServiceDTO();
        devopsServiceDTO.setEnvId(envId);
        // 环境下的serviceIds
        List<Long> serviceIds = devopsServiceMapper.select(devopsServiceDTO).stream().map(DevopsServiceDTO::getId)
                .collect(Collectors.toList());
        devopsServiceMapper.delete(devopsServiceDTO);
        if (!serviceIds.isEmpty()) {
            devopsServiceMapper.deleteServiceInstance(serviceIds);
        }
    }


    public static int getBegin(int page, int size) {
        page = page <= 1 ? 1 : page;
        return (page - 1) * size;
    }
}
