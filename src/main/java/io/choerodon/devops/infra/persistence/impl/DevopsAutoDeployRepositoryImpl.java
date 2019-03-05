package io.choerodon.devops.infra.persistence.impl;

import com.google.gson.Gson;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsAutoDeployE;
import io.choerodon.devops.domain.application.repository.DevopsAutoDeployRepository;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.DevopsAutoDeployDO;
import io.choerodon.devops.infra.mapper.DevopsAutoDeployMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:28 2019/2/26
 * Description:
 */
@Component
public class DevopsAutoDeployRepositoryImpl implements DevopsAutoDeployRepository {
    private Gson gson = new Gson();

    @Autowired
    private DevopsAutoDeployMapper devopsAutoDeployMapper;

    @Override
    public void checkTaskName(Long projectId, String taskName) {
        DevopsAutoDeployDO devopsAutoDeployDO = new DevopsAutoDeployDO();
        devopsAutoDeployDO.setTaskName(taskName);
        devopsAutoDeployDO.setProjectId(projectId);
        if (devopsAutoDeployMapper.selectOne(devopsAutoDeployDO) != null) {
            throw new CommonException("error.auto.deploy.name.exist");
        }
    }

    @Override
    public DevopsAutoDeployE createOrUpdate(DevopsAutoDeployE devopsAutoDeployE) {
        DevopsAutoDeployDO devopsAutoDeployDO = ConvertHelper.convert(devopsAutoDeployE, DevopsAutoDeployDO.class);
        if (devopsAutoDeployDO.getId() == null) {
            devopsAutoDeployDO.setIsEnabled(1);
            if (devopsAutoDeployMapper.insert(devopsAutoDeployDO) != 1) {
                throw new CommonException("error.auto.deploy.create.error");
            }
            return ConvertHelper.convert(devopsAutoDeployMapper.selectOne(devopsAutoDeployDO), DevopsAutoDeployE.class);
        } else {
            if (devopsAutoDeployMapper.updateByPrimaryKeySelective(devopsAutoDeployDO) != 1) {
                throw new CommonException("error.auto.deploy.update.error");
            }
            return devopsAutoDeployE;
        }
    }

    @Override
    public void delete(Long autoDeployId) {
        devopsAutoDeployMapper.deleteByPrimaryKey(autoDeployId);
    }

    @Override
    public Page<DevopsAutoDeployE> listByOptions(Long projectId, Long appId, Long envId, Boolean doPage, PageRequest pageRequest, String params) {
        Page<DevopsAutoDeployDO> devopsAutoDeployDOS = new Page<>();
        String param = null;

        Map<String, Object> mapParams = new HashMap<>();
        mapParams.put("searchParam", null);
        mapParams.put("param", null);
        if (!StringUtils.isEmpty(params)) {
            Map maps = gson.fromJson(params, Map.class);
            mapParams.put("searchParam", TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)));
            mapParams.put("param", TypeUtil.cast(maps.get(TypeUtil.PARAM)));
        }
        //是否需要分页
        if (doPage != null && !doPage) {
            devopsAutoDeployDOS.setContent(devopsAutoDeployMapper.list(projectId, appId, envId,
                    (Map<String, Object>) mapParams.get("searchParam"),
                    mapParams.get("param").toString()));
        } else {
            devopsAutoDeployDOS = PageHelper
                    .doPageAndSort(pageRequest, () -> devopsAutoDeployMapper.list(projectId, appId, envId,
                            (Map<String, Object>) mapParams.get("searchParam"),
                            mapParams.get("param").toString()));
        }
        return ConvertPageHelper.convertPage(devopsAutoDeployDOS, DevopsAutoDeployE.class);
    }

    @Override
    public List<DevopsAutoDeployE> queryByProjectId(Long projectId) {
        DevopsAutoDeployDO devopsAutoDeployDO = new DevopsAutoDeployDO();
        devopsAutoDeployDO.setProjectId(projectId);
        return ConvertHelper.convertList(devopsAutoDeployMapper.select(devopsAutoDeployDO), DevopsAutoDeployE.class);
    }

    @Override
    public DevopsAutoDeployE queryById(Long autoDeployId) {
        return ConvertHelper.convert(devopsAutoDeployMapper.queryById(autoDeployId), DevopsAutoDeployE.class);
    }

    @Override
    public DevopsAutoDeployE updateIsEnabled(Long autoDeployId, Integer isEnabled) {
        DevopsAutoDeployDO devopsAutoDeployDO = new DevopsAutoDeployDO();
        devopsAutoDeployDO.setId(autoDeployId);
        devopsAutoDeployDO.setObjectVersionNumber(devopsAutoDeployMapper.selectByPrimaryKey(devopsAutoDeployDO).getObjectVersionNumber());
        devopsAutoDeployDO.setIsEnabled(isEnabled);
        devopsAutoDeployMapper.updateByPrimaryKeySelective(devopsAutoDeployDO);
        return ConvertHelper.convert(devopsAutoDeployMapper.selectByPrimaryKey(devopsAutoDeployDO), DevopsAutoDeployE.class);
    }

    @Override
    public List<DevopsAutoDeployE> queryByVersion(Long versionId, String branch) {
        return ConvertHelper.convertList(devopsAutoDeployMapper.queryByVersion(versionId, branch), DevopsAutoDeployE.class);
    }
}
