package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvFileResourceVO;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.mapper.DevopsEnvFileResourceMapper;

/**
 * Creator: Runge
 * Date: 2018/7/25
 * Time: 17:21
 * Description:
 */
@Component
public class DevopsEnvFileResourceRepositoryImpl implements DevopsEnvFileResourceRepository {

    @Autowired
    private DevopsEnvFileResourceMapper devopsEnvFileResourceMapper;

    @Override
    public DevopsEnvFileResourceVO baseCreate(DevopsEnvFileResourceVO devopsEnvFileResourceE) {
        DevopsEnvFileResourceDTO devopsEnvFileResourceDO =
                ConvertHelper.convert(devopsEnvFileResourceE, DevopsEnvFileResourceDTO.class);
        devopsEnvFileResourceMapper.insert(devopsEnvFileResourceDO);
        return ConvertHelper.convert(devopsEnvFileResourceDO, DevopsEnvFileResourceVO.class);
    }

    @Override
    public DevopsEnvFileResourceVO baseQuery(Long fileResourceId) {
        return ConvertHelper.convert(
                devopsEnvFileResourceMapper.selectByPrimaryKey(fileResourceId),
                DevopsEnvFileResourceVO.class);
    }

    @Override
    public DevopsEnvFileResourceVO baseUpdate(DevopsEnvFileResourceVO devopsEnvFileResourceE) {
        DevopsEnvFileResourceDTO devopsEnvFileResourceDO = devopsEnvFileResourceMapper
                .selectByPrimaryKey(devopsEnvFileResourceE.getId());
        devopsEnvFileResourceDO.setFilePath(devopsEnvFileResourceE.getFilePath());
        devopsEnvFileResourceMapper.updateByPrimaryKeySelective(devopsEnvFileResourceDO);
        return devopsEnvFileResourceE;
    }

    @Override
    public void baseDelete(Long fileResourceId) {
        devopsEnvFileResourceMapper.deleteByPrimaryKey(fileResourceId);
    }

    @Override
    public DevopsEnvFileResourceVO baseQueryByEnvIdAndResourceId(Long envId, Long resourceId, String resourceType) {
        DevopsEnvFileResourceDTO devopsEnvFileResourceDO = new DevopsEnvFileResourceDTO();
        devopsEnvFileResourceDO.setEnvId(envId);
        devopsEnvFileResourceDO.setResourceId(resourceId);
        devopsEnvFileResourceDO.setResourceType(resourceType);
        return ConvertHelper.convert(
                devopsEnvFileResourceMapper.selectOne(devopsEnvFileResourceDO), DevopsEnvFileResourceVO.class);
    }

    @Override
    public List<DevopsEnvFileResourceVO> baseQueryByEnvIdAndPath(Long envId, String path) {
        DevopsEnvFileResourceDTO devopsEnvFileResourceDO = new DevopsEnvFileResourceDTO();
        devopsEnvFileResourceDO.setEnvId(envId);
        devopsEnvFileResourceDO.setFilePath(path);
        return ConvertHelper.convertList(
                devopsEnvFileResourceMapper.select(devopsEnvFileResourceDO), DevopsEnvFileResourceVO.class);
    }

    @Override
    public void baseDeleteByEnvIdAndResourceId(Long envId, Long resourceId, String resourceType) {
        DevopsEnvFileResourceDTO devopsEnvFileResourceDO = new DevopsEnvFileResourceDTO();
        devopsEnvFileResourceDO.setEnvId(envId);
        devopsEnvFileResourceDO.setResourceId(resourceId);
        devopsEnvFileResourceDO.setResourceType(resourceType);
        devopsEnvFileResourceMapper.delete(devopsEnvFileResourceDO);
    }
}
