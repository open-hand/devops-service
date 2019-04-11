package io.choerodon.devops.infra.persistence.impl;

import com.google.gson.Gson;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsProjectConfigE;
import io.choerodon.devops.domain.application.repository.DevopsProjectConfigRepository;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.DevopsProjectConfigDO;
import io.choerodon.devops.infra.mapper.DevopsProjectConfigMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
@Component
public class DevopsProjectConfigRepositoryImpl implements DevopsProjectConfigRepository {

    private static final Gson gson = new Gson();

    @Autowired
    DevopsProjectConfigMapper configMapper;

    @Override
    public DevopsProjectConfigE create(DevopsProjectConfigE devopsProjectConfigE) {
        DevopsProjectConfigDO paramDO = ConvertHelper.convert(devopsProjectConfigE, DevopsProjectConfigDO.class);
        if (configMapper.insert(paramDO) != 1) {
            throw new CommonException("error.devops.project.config.create");
        }
        return ConvertHelper.convert(configMapper.selectOne(paramDO), DevopsProjectConfigE.class);
    }

    /**
     * @param devopsProjectConfigE
     * @return true为不存在同名值  false存在
     */
    @Override
    public Boolean checkNameWithProjectUniqueness(DevopsProjectConfigE devopsProjectConfigE) {
        DevopsProjectConfigDO checkParamDO = ConvertHelper.convert(devopsProjectConfigE, DevopsProjectConfigDO.class);
        return ObjectUtils.isEmpty(configMapper.selectOne(checkParamDO));
    }

    @Override
    public DevopsProjectConfigE updateByPrimaryKeySelective(DevopsProjectConfigE devopsProjectConfigE) {
        DevopsProjectConfigDO paramDO = ConvertHelper.convert(devopsProjectConfigE, DevopsProjectConfigDO.class);
        if (configMapper.updateByPrimaryKeySelective(paramDO) != 1) {
            throw new CommonException("error.devops.project.config.update");
        }
        return ConvertHelper.convert(configMapper.selectByPrimaryKey(paramDO), DevopsProjectConfigE.class);
    }

    @Override
    public DevopsProjectConfigE queryByPrimaryKey(Long id) {
        DevopsProjectConfigDO paramDO = new DevopsProjectConfigDO();
        paramDO.setId(id);
        return ConvertHelper.convert(configMapper.selectByPrimaryKey(paramDO), DevopsProjectConfigE.class);
    }

    @Override
    public DevopsProjectConfigE queryByName(Long projectId, String name) {
        DevopsProjectConfigDO paramDO = new DevopsProjectConfigDO();
        paramDO.setProjectId(projectId);
        paramDO.setName(name);
        return ConvertHelper.convert(configMapper.selectOne(paramDO), DevopsProjectConfigE.class);
    }

    @Override
    public Page<DevopsProjectConfigE> listByOptions(Long projectId, PageRequest pageRequest, String params) {
        Map<String, Object> mapParams = TypeUtil.castMapParams(params);

        Page<DevopsProjectConfigDO> configDOPage = PageHelper
                .doPageAndSort(pageRequest, () -> configMapper.list(projectId,
                        (Map<String, Object>) mapParams.get(TypeUtil.SEARCH_PARAM),
                        (String) mapParams.get(TypeUtil.PARAM), checkSortIsEmpty(pageRequest)));
        return ConvertPageHelper.convertPage(configDOPage, DevopsProjectConfigE.class);
    }

    private String checkSortIsEmpty(PageRequest pageRequest) {
        String index = "";
        if (pageRequest.getSort() == null) {
            index = "true";
        }
        return index;
    }

    @Override
    public void delete(Long id) {
        DevopsProjectConfigDO paramDO = new DevopsProjectConfigDO();
        paramDO.setId(id);
        if (configMapper.deleteByPrimaryKey(paramDO) != 1) {
            throw new CommonException("error.devops.project.config.delete");
        }
    }

    @Override
    public List<DevopsProjectConfigE> queryByIdAndType(Long projectId, String type) {
        return ConvertHelper.convertList(configMapper.queryByIdAndType(projectId, type), DevopsProjectConfigE.class);
    }

    @Override
    public void checkName(Long projectId, String name) {
        DevopsProjectConfigDO projectConfigDO = new DevopsProjectConfigDO();
        projectConfigDO.setProjectId(projectId);
        projectConfigDO.setName(name);
        if (configMapper.selectOne(projectConfigDO) != null) {
            throw new CommonException("error.devops.project.config.name.with.projectId.already.exist");
        }
    }

    @Override
    public Boolean checkIsUsed(Long checkIsUsed) {
        return configMapper.checkIsUsed(checkIsUsed).isEmpty();
    }
}
