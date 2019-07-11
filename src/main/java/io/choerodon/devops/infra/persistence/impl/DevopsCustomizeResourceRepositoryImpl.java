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
import io.choerodon.devops.infra.dto.DevopsCustomizeResourceDO;
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
    public DevopsCustomizeResourceE create(DevopsCustomizeResourceE devopsCustomizeResourceE) {
        DevopsCustomizeResourceDO devopsCustomizeResourceDO = ConvertHelper.convert(devopsCustomizeResourceE, DevopsCustomizeResourceDO.class);
        if (devopsCustomizeResourceMapper.insert(devopsCustomizeResourceDO) != 1) {
            throw new CommonException("error.customize.resource.create.error");
        }
        return ConvertHelper.convert(devopsCustomizeResourceDO, DevopsCustomizeResourceE.class);
    }

    @Override
    public DevopsCustomizeResourceE query(Long resourceId) {
        return ConvertHelper.convert(devopsCustomizeResourceMapper.selectByPrimaryKey(resourceId), DevopsCustomizeResourceE.class);
    }

    @Override
    public void update(DevopsCustomizeResourceE devopsCustomizeResourceE) {
        DevopsCustomizeResourceDO devopsCustomizeResourceDO = ConvertHelper.convert(devopsCustomizeResourceE, DevopsCustomizeResourceDO.class);
        devopsCustomizeResourceDO.setObjectVersionNumber(devopsCustomizeResourceMapper.selectByPrimaryKey(devopsCustomizeResourceE.getId()).getObjectVersionNumber());
        if (devopsCustomizeResourceMapper.updateByPrimaryKey(devopsCustomizeResourceDO) != 1) {
            throw new CommonException("error.customize.resource.update.error");
        }
    }

    @Override
    public void delete(Long resourceId) {
        devopsCustomizeResourceMapper.deleteByPrimaryKey(resourceId);
    }


    @Override
    public List<DevopsCustomizeResourceE> listByEnvAndFilePath(Long envId, String filePath) {
        DevopsCustomizeResourceDO devopsCustomizeResourceDO = new DevopsCustomizeResourceDO();
        devopsCustomizeResourceDO.setEnvId(envId);
        devopsCustomizeResourceDO.setFilePath(filePath);
        return ConvertHelper.convertList(devopsCustomizeResourceMapper.select(devopsCustomizeResourceDO), DevopsCustomizeResourceE.class);
    }

    @Override
    public DevopsCustomizeResourceE queryByEnvIdAndKindAndName(Long envId, String kind, String name) {
        DevopsCustomizeResourceDO devopsCustomizeResourceDO = new DevopsCustomizeResourceDO();
        devopsCustomizeResourceDO.setEnvId(envId);
        devopsCustomizeResourceDO.setName(name);
        devopsCustomizeResourceDO.setK8sKind(kind);
        return ConvertHelper.convert(devopsCustomizeResourceMapper.selectOne(devopsCustomizeResourceDO), DevopsCustomizeResourceE.class);
    }

    @Override
    public DevopsCustomizeResourceE queryDetail(Long resourceId) {
        return ConvertHelper.convert(devopsCustomizeResourceMapper.queryDetail(resourceId), DevopsCustomizeResourceE.class);
    }

    @Override
    public PageInfo<DevopsCustomizeResourceE> pageDevopsCustomizeResourceE(Long envId, PageRequest pageRequest, String params) {
        Map maps;
        if (params == null) {
            maps = null;
        } else {
            maps = gson.fromJson(params, Map.class);
        }
        PageInfo<DevopsCustomizeResourceDO> devopsCustomizeResourceDOPageInfo = PageHelper
                .startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> devopsCustomizeResourceMapper.pageResources(envId,
                        maps == null ? null : TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)),
                        maps == null ? null : TypeUtil.cast(maps.get(TypeUtil.PARAM))));
        return ConvertPageHelper.convertPageInfo(devopsCustomizeResourceDOPageInfo, DevopsCustomizeResourceE.class);
    }

    @Override
    public void checkExist(Long envId, String kind, String name) {
        DevopsCustomizeResourceDO devopsCustomizeResourceDO = new DevopsCustomizeResourceDO();
        devopsCustomizeResourceDO.setK8sKind(kind);
        devopsCustomizeResourceDO.setName(name);
        devopsCustomizeResourceDO.setEnvId(envId);
        if (devopsCustomizeResourceMapper.selectOne(devopsCustomizeResourceDO) != null) {
            throw new CommonException("error.kind.name.exist");
        }
    }
}
