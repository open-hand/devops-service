package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.cd.PipelineAuditCfgVO;
import io.choerodon.devops.app.service.PipelineAuditCfgService;
import io.choerodon.devops.app.service.PipelineAuditUserService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.PipelineAuditCfgDTO;
import io.choerodon.devops.infra.dto.PipelineAuditUserDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.PipelineAuditConfigMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 人工卡点配置表(PipelineAuditConfig)应用服务
 *
 * @author
 * @since 2022-11-24 15:56:37
 */
@Service
public class PipelineAuditConfigServiceImpl implements PipelineAuditCfgService {

    private static final String DEVOPS_SAVE_AUDIT_CONFIG_FAILED = "devops.save.audit.config.failed";

    @Autowired
    private PipelineAuditConfigMapper pipelineAuditConfigMapper;
    @Autowired
    private PipelineAuditUserService pipelineAuditUserService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfigByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);

        List<PipelineAuditCfgDTO> pipelineAuditCfgDTOS = listByPipelineId(pipelineId);
        if (!CollectionUtils.isEmpty(pipelineAuditCfgDTOS)) {
            List<Long> configIds = pipelineAuditCfgDTOS.stream().map(PipelineAuditCfgDTO::getId).collect(Collectors.toList());

            pipelineAuditUserService.batchDeleteByConfigIds(configIds);

            PipelineAuditCfgDTO pipelineAuditCfgDTO = new PipelineAuditCfgDTO();
            pipelineAuditCfgDTO.setPipelineId(pipelineId);
            pipelineAuditConfigMapper.delete(pipelineAuditCfgDTO);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(PipelineAuditCfgDTO pipelineAuditCfgDTO) {
        MapperUtil.resultJudgedInsertSelective(pipelineAuditConfigMapper, pipelineAuditCfgDTO, DEVOPS_SAVE_AUDIT_CONFIG_FAILED);
    }

    @Override
    public List<PipelineAuditCfgDTO> listByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);

        PipelineAuditCfgDTO pipelineAuditCfgDTO = new PipelineAuditCfgDTO();
        pipelineAuditCfgDTO.setPipelineId(pipelineId);
        return pipelineAuditConfigMapper.select(pipelineAuditCfgDTO);
    }

    @Override
    public PipelineAuditCfgVO queryConfigWithUsersById(Long id) {
        PipelineAuditCfgDTO pipelineAuditCfgDTO = pipelineAuditConfigMapper.selectByPrimaryKey(id);
        PipelineAuditCfgVO pipelineAuditCfgVO = ConvertUtils.convertObject(pipelineAuditCfgDTO, PipelineAuditCfgVO.class);

        List<PipelineAuditUserDTO> pipelineAuditUserDTOS = pipelineAuditUserService.listByAuditConfigId(pipelineAuditCfgDTO.getId());
        if (!CollectionUtils.isEmpty(pipelineAuditUserDTOS)) {
            List<Long> userIds = pipelineAuditUserDTOS.stream().map(PipelineAuditUserDTO::getUserId).collect(Collectors.toList());
            pipelineAuditCfgVO.setAuditUserIds(userIds);
        }
        return pipelineAuditCfgVO;
    }

    @Override
    public PipelineAuditCfgVO queryConfigWithUserDetailsById(Long id) {
        PipelineAuditCfgVO pipelineAuditCfgVO = queryConfigWithUsersById(id);
        List<Long> auditUserIds = pipelineAuditCfgVO.getAuditUserIds();
        if (!CollectionUtils.isEmpty(auditUserIds)) {
            pipelineAuditCfgVO.setIamUserDTOS(baseServiceClientOperator.listUsersByIds(auditUserIds));
        }
        return pipelineAuditCfgVO;
    }
}

