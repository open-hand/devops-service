package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.app.service.ConfigFileDetailService;
import io.choerodon.devops.infra.dto.ConfigFileDetailDTO;
import io.choerodon.devops.infra.mapper.ConfigFileDetailMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 配置文件详情表(ConfigFileDetail)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-02-15 09:25:17
 */
@Service
public class ConfigFileDetailServiceImpl implements ConfigFileDetailService {

    private static final String DEVOPS_SAVE_CONFIG_FILE_DETAIL_FAILED = "devops.save.config.file.detail.failed";
    @Autowired
    private ConfigFileDetailMapper configFileDetailMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigFileDetailDTO baseCreate(ConfigFileDetailDTO configFileDetailDTO) {
        return MapperUtil.resultJudgedInsertSelective(configFileDetailMapper,
                configFileDetailDTO,
                DEVOPS_SAVE_CONFIG_FILE_DETAIL_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseUpdate(ConfigFileDetailDTO configFileDetailDTO) {
        configFileDetailMapper.updateByPrimaryKeySelective(configFileDetailDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseDelete(Long id) {
        configFileDetailMapper.deleteByPrimaryKey(id);
    }

    @Override
    public ConfigFileDetailDTO baseQueryById(Long id) {
        return configFileDetailMapper.selectByPrimaryKey(id);
    }
}

