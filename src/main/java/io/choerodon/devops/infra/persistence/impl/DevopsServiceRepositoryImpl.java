package io.choerodon.devops.infra.persistence.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        if (selectCount > 0) {
            throw new CommonException("error.service.name.check");
        }
        return true;
    }

    @Override
    public Page<DevopsServiceV> listDevopsServiceByPage(Long projectId, PageRequest pageRequest, String searchParam) {
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
                    projectId, TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                    TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM)));
            devopsServiceQueryDOList = PageHelper.doSort(
                    pageRequest.getSort(), () -> devopsServiceMapper.listDevopsServiceByPage(
                            projectId, TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                            TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM)), start, size));
        } else {
            count = devopsServiceMapper
                    .selectCountByName(projectId, null, null);
            devopsServiceQueryDOList = PageHelper.doSort(pageRequest.getSort(), () ->
                    devopsServiceMapper.listDevopsServiceByPage(
                            projectId, null, null, start, size));
        }

        return ConvertPageHelper.convertPage(
                new Page<>(devopsServiceQueryDOList, pageInfo, count), DevopsServiceV.class);
    }

    @Override
    public List<DevopsServiceV> listDevopsService(Long envId) {
        List<DevopsServiceQueryDO> devopsServiceQueryDOList =
                devopsServiceMapper.listDevopsService(envId);
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
        return ConvertHelper.convert(
                devopsServiceDO, DevopsServiceE.class);
    }

    @Override
    public DevopsServiceE query(Long id) {
        return ConvertHelper.convert(
                devopsServiceMapper.selectByPrimaryKey(id), DevopsServiceE.class);
    }

    @Override
    public void delete(Long id) {
        devopsServiceMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void update(DevopsServiceE devopsServiceE) {
        if (devopsServiceMapper.updateByPrimaryKey(
                ConvertHelper.convert(devopsServiceE, DevopsServiceDO.class)) != 1) {
            throw new CommonException("error.k8s.service.update");
        }
    }

    @Override
    public List<Long> selectDeployedEnv() {
        return devopsServiceMapper.selectDeployedEnv();
    }

    @Override
    public DevopsServiceE selectByNameAndNamespace(String name, String namespace) {
        DevopsServiceDO devopsServiceDO = new DevopsServiceDO();
        devopsServiceDO.setName(name);
        devopsServiceDO.setNamespace(namespace);
        return ConvertHelper.convert(devopsServiceMapper.selectOne(devopsServiceDO), DevopsServiceE.class);
    }

    @Override
    public Boolean checkEnvHasService(Long envId) {
        return devopsServiceMapper.checkEnvHasService(envId);
    }
}
