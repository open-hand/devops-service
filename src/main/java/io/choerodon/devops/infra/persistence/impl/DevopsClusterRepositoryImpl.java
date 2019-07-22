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
import io.choerodon.devops.domain.application.repository.DevopsClusterRepository;
import io.choerodon.devops.infra.util.GenerateUUID;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.dto.DevopsClusterDTO;
import io.choerodon.devops.infra.mapper.DevopsClusterMapper;

@Service
public class DevopsClusterRepositoryImpl implements DevopsClusterRepository {

    private JSON json = new JSON();


    @Autowired
    private DevopsClusterMapper devopsClusterMapper;

    @Override
    public DevopsClusterE baseCreateCluster(DevopsClusterE devopsClusterE) {
        DevopsClusterDTO devopsClusterDTO = ConvertHelper.convert(devopsClusterE, DevopsClusterDTO.class);
        List<DevopsClusterDTO> devopsClusterDTOS = devopsClusterMapper.selectAll();
        String choerodonId = GenerateUUID.generateUUID().split("-")[0];
        if (!devopsClusterDTOS.isEmpty()) {
            devopsClusterDTO.setChoerodonId(devopsClusterDTOS.get(0).getChoerodonId());
        } else {
            devopsClusterDTO.setChoerodonId(choerodonId);
        }
        if (devopsClusterMapper.insert(devopsClusterDTO) != 1) {
            throw new CommonException("error.devops.cluster.insert");
        }
        return ConvertHelper.convert(devopsClusterDTO, DevopsClusterE.class);
    }

    @Override
    public void baseCheckName(DevopsClusterE devopsClusterE) {
        DevopsClusterDTO devopsClusterDTO = ConvertHelper.convert(devopsClusterE, DevopsClusterDTO.class);
        if (devopsClusterMapper.selectOne(devopsClusterDTO) != null) {
            throw new CommonException("error.cluster.name.exist");
        }
    }

    @Override
    public void baseCheckCode(DevopsClusterE devopsClusterE) {
        DevopsClusterDTO devopsClusterDTO = ConvertHelper.convert(devopsClusterE, DevopsClusterDTO.class);
        if (devopsClusterMapper.selectOne(devopsClusterDTO) != null) {
            throw new CommonException("error.cluster.code.exist");
        }
    }

    @Override
    public List<DevopsClusterE> baseListByProjectId(Long projectId, Long organizationId) {
        return ConvertHelper.convertList(devopsClusterMapper.listByProjectId(projectId, organizationId), DevopsClusterE.class);
    }

    @Override
    public DevopsClusterE baseQuery(Long clusterId) {
        return ConvertHelper.convert(devopsClusterMapper.selectByPrimaryKey(clusterId), DevopsClusterE.class);
    }

    @Override
    public void baseUpdate(DevopsClusterE devopsClusterE) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterMapper.selectByPrimaryKey(devopsClusterE.getId());
        DevopsClusterDTO updateDevopsCluster = ConvertHelper.convert(devopsClusterE, DevopsClusterDTO.class);
        updateDevopsCluster.setObjectVersionNumber(devopsClusterDTO.getObjectVersionNumber());
        devopsClusterMapper.updateByPrimaryKeySelective(updateDevopsCluster);
        devopsClusterMapper.updateSkipCheckPro(devopsClusterE.getId(), devopsClusterE.getSkipCheckProjectPermission());
    }

    @Override
    public PageInfo<DevopsClusterE> basePageClustersByOptions(Long organizationId, Boolean doPage, PageRequest pageRequest, String params) {
        PageInfo<DevopsClusterDTO> devopsClusterEPage;
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
    public void baseDelete(Long clusterId) {
        devopsClusterMapper.deleteByPrimaryKey(clusterId);
    }

    @Override
    public DevopsClusterE baseQueryByToken(String token) {
        DevopsClusterDTO devopsClusterDTO = new DevopsClusterDTO();
        devopsClusterDTO.setToken(token);
        return ConvertHelper.convert(devopsClusterMapper.selectOne(devopsClusterDTO), DevopsClusterE.class);
    }

    @Override
    public List<DevopsClusterE> baseList() {
        return ConvertHelper.convertList(devopsClusterMapper.selectAll(), DevopsClusterE.class);
    }

    @Override
    public PageInfo<DevopsEnvPodE> basePageQueryPodsByNodeName(Long clusterId, String nodeName, PageRequest pageRequest, String searchParam) {
        return ConvertPageHelper.convertPageInfo(PageHelper.startPage(pageRequest.getPage(),pageRequest.getSize(),PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo( () -> devopsClusterMapper.pageQueryPodsByNodeName(clusterId, nodeName, searchParam)), DevopsEnvPodE.class);
    }

    @Override
    public DevopsClusterE baseQueryByCode(Long organizationId, String code) {
        DevopsClusterDTO devopsClusterDTO = new DevopsClusterDTO();
        devopsClusterDTO.setOrganizationId(organizationId);
        devopsClusterDTO.setCode(code);
        return ConvertHelper.convert(devopsClusterMapper.selectOne(devopsClusterDTO),DevopsClusterE.class);
    }

    @Override
    public void baseUpdateProjectId(Long orgId, Long proId) {
        devopsClusterMapper.updateProjectId(orgId, proId);
    }
}
