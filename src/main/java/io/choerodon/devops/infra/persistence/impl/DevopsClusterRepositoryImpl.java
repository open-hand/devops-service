package io.choerodon.devops.infra.persistence.impl;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.kubernetes.client.JSON;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsClusterE;
import io.choerodon.devops.domain.application.entity.DevopsEnvPodE;
import io.choerodon.devops.domain.application.repository.DevopsClusterRepository;
import io.choerodon.devops.infra.common.util.GenerateUUID;
import io.choerodon.devops.infra.common.util.PageRequestUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.DevopsClusterDO;
import io.choerodon.devops.infra.mapper.DevopsClusterMapper;

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
    public PageInfo<DevopsClusterE> pageClusters(Long organizationId, Boolean doPage, PageRequest pageRequest, String params) {
        PageInfo<DevopsClusterDO> devopsClusterEPage;
        if (!StringUtils.isEmpty(params)) {
            Map<String, Object> searchParamMap = json.deserialize(params, Map.class);
            devopsClusterEPage = PageHelper
                    .startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> devopsClusterMapper.listClusters(organizationId, TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)), TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM))));
        } else {
            devopsClusterEPage = PageHelper.startPage(
                    pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> devopsClusterMapper.listClusters(organizationId, null, null));
        }
        return ConvertPageHelper.convertPageInfo(devopsClusterEPage, DevopsClusterE.class);
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

    @Override
    public List<DevopsClusterE> list() {
        return ConvertHelper.convertList(devopsClusterMapper.selectAll(), DevopsClusterE.class);
    }

    @Override
    public PageInfo<DevopsEnvPodE> pageQueryPodsByNodeName(Long clusterId, String nodeName, PageRequest pageRequest, String searchParam) {
        return ConvertPageHelper.convertPageInfo(PageHelper.startPage(pageRequest.getPage(),pageRequest.getSize(),PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo( () -> devopsClusterMapper.pageQueryPodsByNodeName(clusterId, nodeName, searchParam)), DevopsEnvPodE.class);
    }

    @Override
    public DevopsClusterE queryByCode(Long organizationId, String code) {
        DevopsClusterDO devopsClusterDO = new DevopsClusterDO();
        devopsClusterDO.setOrganizationId(organizationId);
        devopsClusterDO.setCode(code);
        return ConvertHelper.convert(devopsClusterMapper.selectOne(devopsClusterDO),DevopsClusterE.class);
    }

    @Override
    public void updateProjectId(Long orgId, Long proId) {
        devopsClusterMapper.updateProjectId(orgId, proId);
    }
}
