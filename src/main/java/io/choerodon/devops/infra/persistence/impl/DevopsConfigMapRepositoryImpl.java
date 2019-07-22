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
import io.choerodon.devops.domain.application.repository.DevopsConfigMapRepository;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.dto.DevopsConfigMapDTO;
import io.choerodon.devops.infra.mapper.DevopsConfigMapMapper;

@Service
public class DevopsConfigMapRepositoryImpl implements DevopsConfigMapRepository {

    Gson gson = new Gson();
    @Autowired
    private DevopsConfigMapMapper devopsConfigMapMapper;

    @Override
    public DevopsConfigMapE baseQueryByEnvIdAndName(Long envId, String name) {
        DevopsConfigMapDTO devopsConfigMapDTO = new DevopsConfigMapDTO();
        devopsConfigMapDTO.setName(name);
        devopsConfigMapDTO.setEnvId(envId);
        return ConvertHelper.convert(devopsConfigMapMapper.selectOne(devopsConfigMapDTO), DevopsConfigMapE.class);
    }

    @Override
    public DevopsConfigMapE baseCreate(DevopsConfigMapE devopsConfigMapE) {
        DevopsConfigMapDTO devopsConfigMapDTO = ConvertHelper.convert(devopsConfigMapE, DevopsConfigMapDTO.class);
        if (devopsConfigMapMapper.insert(devopsConfigMapDTO) != 1) {
            throw new CommonException("error.configMap.create");
        }
        return ConvertHelper.convert(devopsConfigMapDTO, DevopsConfigMapE.class);
    }

    @Override
    public DevopsConfigMapE baseUpdate(DevopsConfigMapE devopsConfigMapE) {
        DevopsConfigMapDTO oldDevopsConfigMapDTO = devopsConfigMapMapper.selectByPrimaryKey(devopsConfigMapE.getId());
        DevopsConfigMapDTO updateDevopsConfigMapDTO = ConvertHelper.convert(devopsConfigMapE, DevopsConfigMapDTO.class);
        updateDevopsConfigMapDTO.setObjectVersionNumber(oldDevopsConfigMapDTO.getObjectVersionNumber());
        if (devopsConfigMapMapper.updateByPrimaryKeySelective(updateDevopsConfigMapDTO) != 1) {
            throw new CommonException("error.configMap.update");
        }
        return ConvertHelper.convert(updateDevopsConfigMapDTO, DevopsConfigMapE.class);
    }

    @Override
    public DevopsConfigMapE baseQueryById(Long id) {
        return ConvertHelper.convert(devopsConfigMapMapper.selectByPrimaryKey(id), DevopsConfigMapE.class);
    }

    @Override
    public void baseDelete(Long id) {
        devopsConfigMapMapper.deleteByPrimaryKey(id);
    }

    @Override
    public PageInfo<DevopsConfigMapE> basePageByEnv(Long envId, PageRequest pageRequest, String params, Long appId) {
        Map maps = gson.fromJson(params, Map.class);
        PageInfo<DevopsConfigMapDTO> devopsConfigMapDOS = PageHelper
                .startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> devopsConfigMapMapper.listByEnv(envId,
                        TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)),
                        TypeUtil.cast(maps.get(TypeUtil.PARAM)),
                        appId));
        return ConvertPageHelper.convertPageInfo(devopsConfigMapDOS, DevopsConfigMapE.class);
    }

    @Override
    public List<DevopsConfigMapE> baseListByEnv(Long envId) {
        DevopsConfigMapDTO devopsConfigMapDTO = new DevopsConfigMapDTO();
        devopsConfigMapDTO.setEnvId(envId);
        return ConvertHelper.convertList(devopsConfigMapMapper.select(devopsConfigMapDTO), DevopsConfigMapE.class);
    }


}
