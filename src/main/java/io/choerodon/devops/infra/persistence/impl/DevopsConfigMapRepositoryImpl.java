package io.choerodon.devops.infra.persistence.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsConfigMapE;
import io.choerodon.devops.domain.application.repository.DevopsConfigMapRepository;
import io.choerodon.devops.infra.common.util.PageRequestUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.DevopsConfigMapDO;
import io.choerodon.devops.infra.mapper.DevopsConfigMapMapper;

@Service
public class DevopsConfigMapRepositoryImpl implements DevopsConfigMapRepository {

    Gson gson = new Gson();
    @Autowired
    private DevopsConfigMapMapper devopsConfigMapMapper;

    @Override
    public DevopsConfigMapE queryByEnvIdAndName(Long envId, String name) {
        DevopsConfigMapDO devopsConfigMapDO = new DevopsConfigMapDO();
        devopsConfigMapDO.setName(name);
        devopsConfigMapDO.setEnvId(envId);
        return ConvertHelper.convert(devopsConfigMapMapper.selectOne(devopsConfigMapDO), DevopsConfigMapE.class);
    }

    @Override
    public DevopsConfigMapE create(DevopsConfigMapE devopsConfigMapE) {
        DevopsConfigMapDO devopsConfigMapDO = ConvertHelper.convert(devopsConfigMapE, DevopsConfigMapDO.class);
        if (devopsConfigMapMapper.insert(devopsConfigMapDO) != 1) {
            throw new CommonException("error.configMap.create");
        }
        return ConvertHelper.convert(devopsConfigMapDO, DevopsConfigMapE.class);
    }

    @Override
    public DevopsConfigMapE update(DevopsConfigMapE devopsConfigMapE) {
        DevopsConfigMapDO oldDevopsConfigMapDO = devopsConfigMapMapper.selectByPrimaryKey(devopsConfigMapE.getId());
        DevopsConfigMapDO updateDevopsConfigMapDO = ConvertHelper.convert(devopsConfigMapE, DevopsConfigMapDO.class);
        updateDevopsConfigMapDO.setObjectVersionNumber(oldDevopsConfigMapDO.getObjectVersionNumber());
        if (devopsConfigMapMapper.updateByPrimaryKeySelective(updateDevopsConfigMapDO) != 1) {
            throw new CommonException("error.configMap.update");
        }
        return ConvertHelper.convert(updateDevopsConfigMapDO, DevopsConfigMapE.class);
    }

    @Override
    public DevopsConfigMapE queryById(Long id) {
        return ConvertHelper.convert(devopsConfigMapMapper.selectByPrimaryKey(id), DevopsConfigMapE.class);
    }

    @Override
    public void delete(Long id) {
        devopsConfigMapMapper.deleteByPrimaryKey(id);
    }

    @Override
    public PageInfo<DevopsConfigMapE> pageByEnv(Long envId, PageRequest pageRequest, String params, Long appId) {
        Map maps = gson.fromJson(params, Map.class);
        PageInfo<DevopsConfigMapDO> devopsConfigMapDOS = PageHelper
                .startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> devopsConfigMapMapper.listByEnv(envId,
                        TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)),
                        TypeUtil.cast(maps.get(TypeUtil.PARAM)),
                        appId));
        return ConvertPageHelper.convertPageInfo(devopsConfigMapDOS, DevopsConfigMapE.class);
    }

    @Override
    public List<DevopsConfigMapE> listByEnv(Long envId) {
        DevopsConfigMapDO devopsConfigMapDO = new DevopsConfigMapDO();
        devopsConfigMapDO.setEnvId(envId);
        return ConvertHelper.convertList(devopsConfigMapMapper.select(devopsConfigMapDO), DevopsConfigMapE.class);
    }


}
