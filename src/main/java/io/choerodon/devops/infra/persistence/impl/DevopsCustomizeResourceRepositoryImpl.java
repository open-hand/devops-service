package io.choerodon.devops.infra.persistence.impl;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.DevopsCustomizeResourceE;
import io.choerodon.devops.domain.application.repository.DevopsCustomizeResourceRepository;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.dto.DevopsCustomizeResourceDTO;
import io.choerodon.devops.infra.mapper.DevopsCustomizeResourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Sheep on 2019/6/26.
 */


@Component
public class DevopsCustomizeResourceRepositoryImpl implements DevopsCustomizeResourceRepository {

    Gson gson = new Gson();

    @Autowired
    DevopsCustomizeResourceMapper devopsCustomizeResourceMapper;


    @Override
    public DevopsCustomizeResourceDTO baseCreate(DevopsCustomizeResourceDTO devopsCustomizeResourceDTO) {
        if (devopsCustomizeResourceMapper.insert(devopsCustomizeResourceDTO) != 1) {
            throw new CommonException("error.customize.resource.create.error");
        }
        return devopsCustomizeResourceDTO;
    }

    @Override
    public DevopsCustomizeResourceDTO baseQuery(Long resourceId) {
        return devopsCustomizeResourceMapper.selectByPrimaryKey(resourceId);
    }

    @Override
    public void baseUpdate(DevopsCustomizeResourceDTO devopsCustomizeResourceDTO) {
        devopsCustomizeResourceDTO.setObjectVersionNumber(devopsCustomizeResourceMapper.selectByPrimaryKey(devopsCustomizeResourceDTO.getId()).getObjectVersionNumber());
        if (devopsCustomizeResourceMapper.updateByPrimaryKey(devopsCustomizeResourceDTO) != 1) {
            throw new CommonException("error.customize.resource.update.error");
        }
    }

    @Override
    public void baseDelete(Long resourceId) {
        devopsCustomizeResourceMapper.deleteByPrimaryKey(resourceId);
    }


    @Override
    public List<DevopsCustomizeResourceDTO> listByEnvAndFilePath(Long envId, String filePath) {
        DevopsCustomizeResourceDTO devopsCustomizeResourceDO = new DevopsCustomizeResourceDTO();
        devopsCustomizeResourceDO.setEnvId(envId);
        devopsCustomizeResourceDO.setFilePath(filePath);
        return devopsCustomizeResourceMapper.select(devopsCustomizeResourceDO);
    }

    @Override
    public DevopsCustomizeResourceDTO queryByEnvIdAndKindAndName(Long envId, String kind, String name) {
        DevopsCustomizeResourceDTO devopsCustomizeResourceDO = new DevopsCustomizeResourceDTO();
        devopsCustomizeResourceDO.setEnvId(envId);
        devopsCustomizeResourceDO.setName(name);
        devopsCustomizeResourceDO.setK8sKind(kind);
        return devopsCustomizeResourceMapper.selectOne(devopsCustomizeResourceDO);
    }

    @Override
    public DevopsCustomizeResourceDTO queryDetail(Long resourceId) {
        return devopsCustomizeResourceMapper.queryDetail(resourceId);
    }

    @Override
    public PageInfo<DevopsCustomizeResourceDTO> pageDevopsCustomizeResourceE(Long envId, PageRequest pageRequest, String params) {
        Map maps;
        if (params == null) {
            maps = null;
        } else {
            maps = gson.fromJson(params, Map.class);
        }
        return PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest))
                .doSelectPageInfo(
                        () -> devopsCustomizeResourceMapper.pageResources(envId, maps == null ? null : TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)), maps == null ? null : TypeUtil.cast(maps.get(TypeUtil.PARAM)))
                );
    }

    @Override
    public void checkExist(Long envId, String kind, String name) {
        if (queryByEnvIdAndKindAndName(envId, kind, name) != null) {
            throw new CommonException("error.kind.name.exist");
        }
    }
}
