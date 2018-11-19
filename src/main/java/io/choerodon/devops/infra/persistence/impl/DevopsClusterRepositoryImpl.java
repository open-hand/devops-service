package io.choerodon.devops.infra.persistence.impl;

import java.util.List;
import java.util.Map;

import io.kubernetes.client.JSON;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsClusterE;
import io.choerodon.devops.domain.application.repository.DevopsClusterRepository;
import io.choerodon.devops.infra.common.util.GenerateUUID;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.DevopsClusterDO;
import io.choerodon.devops.infra.mapper.DevopsClusterMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Service
public class DevopsClusterRepositoryImpl implements DevopsClusterRepository {

    private JSON json = new JSON();


    @Autowired
    private DevopsClusterMapper devopsClusterMapper;

    @Override
    public DevopsClusterE create(DevopsClusterE devopsClusterE) {
        DevopsClusterDO devopsClusterDO = ConvertHelper.convert(devopsClusterE, DevopsClusterDO.class);
        List<DevopsClusterDO> devopsClusterDOS = devopsClusterMapper.selectAll();
        String choerodonId = GenerateUUID.generateUUID().split("-")[0];
        if (!devopsClusterDOS.isEmpty()) {
            devopsClusterDO.setChoerodonId(devopsClusterDOS.get(0).getChoerodonId());
        } else {
            devopsClusterDO.setChoerodonId(choerodonId);
        }
        if (devopsClusterMapper.insert(devopsClusterDO) != 1) {
            throw new CommonException("error.devops.cluster.insert");
        }
        return ConvertHelper.convert(devopsClusterDO, DevopsClusterE.class);
    }

    @Override
    public void checkName(DevopsClusterE devopsClusterE) {
        DevopsClusterDO devopsClusterDO = ConvertHelper.convert(devopsClusterE, DevopsClusterDO.class);
        if (devopsClusterMapper.selectOne(devopsClusterDO) != null) {
            throw new CommonException("error.cluster.name.exist");
        }
    }

    @Override
    public void checkCode(DevopsClusterE devopsClusterE) {
        DevopsClusterDO devopsClusterDO = ConvertHelper.convert(devopsClusterE, DevopsClusterDO.class);
        if (devopsClusterMapper.selectOne(devopsClusterDO) != null) {
            throw new CommonException("error.cluster.code.exist");
        }
    }

    @Override
    public List<DevopsClusterE> listByProjectId(Long projectId, Long organizationId) {
        return ConvertHelper.convertList(devopsClusterMapper.listByProjectId(projectId, organizationId), DevopsClusterE.class);
    }

    @Override
    public DevopsClusterE query(Long clusterId) {
        return ConvertHelper.convert(devopsClusterMapper.selectByPrimaryKey(clusterId), DevopsClusterE.class);
    }

    @Override
    public void update(DevopsClusterE devopsClusterE) {
        DevopsClusterDO devopsClusterDO = devopsClusterMapper.selectByPrimaryKey(devopsClusterE.getId());
        DevopsClusterDO updateDevopsCluster = ConvertHelper.convert(devopsClusterE, DevopsClusterDO.class);
        updateDevopsCluster.setObjectVersionNumber(devopsClusterDO.getObjectVersionNumber());
        devopsClusterMapper.updateByPrimaryKeySelective(updateDevopsCluster);
        devopsClusterMapper.updateSkipCheckPro(devopsClusterE.getId(), devopsClusterE.getSkipCheckProjectPermission());
    }

    @Override
    public Page<DevopsClusterE> pageClusters(Long organizationId, PageRequest pageRequest, String params) {
        DevopsClusterDO devopsClusterDO = new DevopsClusterDO();
        devopsClusterDO.setOrganizationId(organizationId);
        Map<String, Object> maps = json.deserialize(params, Map.class);
        return ConvertPageHelper.convertPage(PageHelper.doPageAndSort(pageRequest, () -> devopsClusterMapper.listClusters(organizationId, TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)), TypeUtil.cast(maps.get(TypeUtil.PARAM)))), DevopsClusterE.class);
    }

    @Override
    public void delete(Long clusterId) {
        devopsClusterMapper.deleteByPrimaryKey(clusterId);
    }

    @Override
    public DevopsClusterE queryByToken(String token) {
        DevopsClusterDO devopsClusterDO = new DevopsClusterDO();
        devopsClusterDO.setToken(token);
        return ConvertHelper.convert(devopsClusterMapper.selectOne(devopsClusterDO), DevopsClusterE.class);
    }
}
