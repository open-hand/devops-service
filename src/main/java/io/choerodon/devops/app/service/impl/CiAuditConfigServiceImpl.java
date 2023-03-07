package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.pipeline.CiAuditConfigVO;
import io.choerodon.devops.app.service.CiAuditConfigService;
import io.choerodon.devops.app.service.CiAuditUserService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.CiAuditConfigDTO;
import io.choerodon.devops.infra.dto.CiAuditUserDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.CiAuditConfigMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * ci 人工卡点配置表(CiAuditConfig)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-02 11:40:28
 */
@Service
public class CiAuditConfigServiceImpl implements CiAuditConfigService {

    private static final String DEVOPS_AUDIT_CONFIG_CREATE = "devops.audit.config.create";

    @Autowired
    private CiAuditConfigMapper ciAuditConfigMapper;
    @Autowired
    private CiAuditUserService ciAuditUserService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Override
    public CiAuditConfigVO queryConfigWithUsersById(Long id) {

        CiAuditConfigDTO ciAuditConfigDTO = ciAuditConfigMapper.selectByPrimaryKey(id);
        CiAuditConfigVO ciAuditConfigVO = ConvertUtils.convertObject(ciAuditConfigDTO, CiAuditConfigVO.class);

        List<CiAuditUserDTO> ciAuditUserDTOS = ciAuditUserService.listByAuditConfigId(ciAuditConfigDTO.getId());
        if (!CollectionUtils.isEmpty(ciAuditUserDTOS)) {
            List<Long> userIds = ciAuditUserDTOS.stream().map(CiAuditUserDTO::getUserId).collect(Collectors.toList());
            ciAuditConfigVO.setCdAuditUserIds(userIds);
        }
        return ciAuditConfigVO;
    }

    @Override
    public CiAuditConfigVO queryConfigWithUserDetailsById(Long id) {
        CiAuditConfigVO ciAuditConfigVO = queryConfigWithUsersById(id);
        List<Long> cdAuditUserIds = ciAuditConfigVO.getCdAuditUserIds();
        if (!CollectionUtils.isEmpty(cdAuditUserIds)) {
            ciAuditConfigVO.setIamUserDTOS(baseServiceClientOperator.listUsersByIds(cdAuditUserIds));

        }
        return ciAuditConfigVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiAuditConfigDTO baseCreate(CiAuditConfigDTO ciAuditConfigDTO) {
        return MapperUtil.resultJudgedInsertSelective(ciAuditConfigMapper, ciAuditConfigDTO, DEVOPS_AUDIT_CONFIG_CREATE);
    }

    @Override
    public List<CiAuditConfigDTO> listByCiPipelineId(Long ciPipelineId) {
        Assert.notNull(ciPipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);

        CiAuditConfigDTO ciAuditConfigDTO = new CiAuditConfigDTO();
        ciAuditConfigDTO.setCiPipelineId(ciPipelineId);
        return ciAuditConfigMapper.select(ciAuditConfigDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteByIds(List<Long> ids) {
        ciAuditConfigMapper.batchDeleteByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfigByPipelineId(Long ciPipelineId) {
        Assert.notNull(ciPipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);

        List<CiAuditConfigDTO> ciAuditConfigDTOS = listByCiPipelineId(ciPipelineId);

        List<Long> configIds = ciAuditConfigDTOS.stream().map(CiAuditConfigDTO::getId).collect(Collectors.toList());

        ciAuditUserService.batchDeleteByConfigIds(configIds);

        CiAuditConfigDTO ciAuditConfigDTO = new CiAuditConfigDTO();
        ciAuditConfigDTO.setCiPipelineId(ciPipelineId);
        ciAuditConfigMapper.delete(ciAuditConfigDTO);
    }
}

