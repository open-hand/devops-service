package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ConfigFileVO;
import io.choerodon.devops.api.vo.PipelineInstanceReferenceVO;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.app.service.CiTemplateJobService;
import io.choerodon.devops.app.service.ConfigFileDetailService;
import io.choerodon.devops.app.service.ConfigFileService;
import io.choerodon.devops.app.service.DevopsCiPipelineService;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.ConfigFileDTO;
import io.choerodon.devops.infra.dto.ConfigFileDetailDTO;
import io.choerodon.devops.infra.mapper.ConfigFileMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.MapperUtil;
import io.choerodon.devops.infra.util.UserDTOFillUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 配置文件表(ConfigFile)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-02-15 09:25:06
 */
@Service
public class ConfigFileServiceImpl implements ConfigFileService {

    private static final String DEVOPS_SAVE_CONFIG_FILE_FAILED = "devops.save.config.file.failed";
    private static final String DEVOPS_UPDATE_CONFIG_FILE_FAILED = "devops.update.config.file.failed";

    @Autowired
    private ConfigFileMapper configFileMapper;
    @Autowired
    private ConfigFileDetailService configFileDetailService;

    @Autowired
    private DevopsCiPipelineService devopsCiPipelineService;
    @Autowired
    private CiTemplateJobService ciTemplateJobService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigFileDTO create(String sourceType, Long sourceId, ConfigFileVO configFileVO) {
        ConfigFileDetailDTO configFileDetailDTO = configFileDetailService.baseCreate(new ConfigFileDetailDTO(configFileVO.getMessage()));

        ConfigFileDTO configFileDTO = ConvertUtils.convertObject(configFileVO, ConfigFileDTO.class);
        configFileDTO.setSourceType(sourceType);
        configFileDTO.setSourceId(sourceId);
        configFileDTO.setDetailId(configFileDetailDTO.getId());
        return MapperUtil.resultJudgedInsertSelective(configFileMapper, configFileDTO, DEVOPS_SAVE_CONFIG_FILE_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(String sourceType,
                       Long sourceId,
                       Long id,
                       ConfigFileVO configFileVO) {
        ConfigFileDTO configFileDTO = baseQueryById(id);
        if (!configFileDTO.getSourceType().equals(sourceType) || !configFileDTO.getSourceId().equals(sourceId)) {
            throw new CommonException(MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_TEAM);
        }
        configFileDTO.setName(configFileVO.getName());
        configFileDTO.setDescription(configFileVO.getDescription());
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(configFileMapper, configFileDTO, DEVOPS_UPDATE_CONFIG_FILE_FAILED);

        ConfigFileDetailDTO configFileDetailDTO = configFileDetailService.baseQueryById(configFileDTO.getDetailId());
        configFileDetailDTO.setMessage(configFileVO.getMessage());
        configFileDetailService.baseUpdate(configFileDetailDTO);
    }

    @Override
    public ConfigFileDTO baseQueryById(Long id) {
        return configFileMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String sourceType, Long sourceId, Long id) {
        ConfigFileDTO configFileDTO = baseQueryById(id);
        if (!configFileDTO.getSourceType().equals(sourceType) || !configFileDTO.getSourceId().equals(sourceId)) {
            throw new CommonException(MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_TEAM);
        }
        configFileMapper.deleteByPrimaryKey(id);
        configFileDetailService.baseDelete(configFileDTO.getDetailId());
    }

    @Override
    public Page<ConfigFileVO> paging(String sourceType, Long sourceId, PageRequest pageable, String name, String description, String param) {
        Page<ConfigFileVO> page = PageHelper.doPage(pageable, () -> configFileMapper.listByParams(sourceType, sourceId, name, description, param));
        UserDTOFillUtil.fillUserInfo(page.getContent(), "createdBy", "creator");
        return page;
    }

    @Override
    public ConfigFileVO queryByIdWithDetail(Long id) {
        return configFileMapper.queryByIdWithDetail(id);
    }

    @Override
    public Boolean checkIsUsed(Long projectId, Long id) {
        // 查询是否存在流水线引用
        List<PipelineInstanceReferenceVO> pipelineInstanceReferenceVOList = devopsCiPipelineService.listConfigFileReferencePipelineInfo(projectId, id);
        if (!CollectionUtils.isEmpty(pipelineInstanceReferenceVOList)) {
            return true;
        }
        // 查询是否存在流水线模板引用
        List<CiTemplateJobVO> ciTemplateJobVOS = ciTemplateJobService.listConfigFileReferenceInfo(id);
        if (!CollectionUtils.isEmpty(ciTemplateJobVOS)) {
            return true;
        }
        return false;
    }


}

