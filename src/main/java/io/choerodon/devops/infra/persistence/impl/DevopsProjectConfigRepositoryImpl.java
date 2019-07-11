package io.choerodon.devops.infra.persistence.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.DevopsProjectConfigE;
import io.choerodon.devops.domain.application.repository.DevopsProjectConfigRepository;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.dto.DevopsProjectConfigDO;
import io.choerodon.devops.infra.mapper.DevopsProjectConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

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
        return ConvertHelper.convert(paramDO, DevopsProjectConfigE.class);
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
    public DevopsProjectConfigE queryByNameWithNullProject(String name) {
        return ConvertHelper.convert(configMapper.queryByNameWithNoProject(name), DevopsProjectConfigE.class);
    }

    @Override
    public PageInfo<DevopsProjectConfigE> listByOptions(Long projectId, PageRequest pageRequest, String params) {
        Map<String, Object> mapParams = TypeUtil.castMapParams(params);

        PageInfo<DevopsProjectConfigDO> configDOPage = PageHelper
                .startPage(pageRequest.getPage(),pageRequest.getSize(),PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo( () -> configMapper.list(projectId,
                        (Map<String, Object>) mapParams.get(TypeUtil.SEARCH_PARAM),
                        (String) mapParams.get(TypeUtil.PARAM), PageRequestUtil.checkSortIsEmpty(pageRequest)));
        return ConvertPageHelper.convertPageInfo(configDOPage, DevopsProjectConfigE.class);
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
