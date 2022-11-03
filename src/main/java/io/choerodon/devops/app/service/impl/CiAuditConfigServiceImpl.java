package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.pipeline.CiAuditConfigVO;
import io.choerodon.devops.app.service.CiAuditConfigService;
import io.choerodon.devops.app.service.CiAuditUserService;
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
    public CiAuditConfigVO queryConfigWithUsersByStepId(Long stepId) {
        CiAuditConfigDTO record = new CiAuditConfigDTO();
        record.setStepId(stepId);

        CiAuditConfigDTO ciAuditConfigDTO = ciAuditConfigMapper.selectOne(record);
        CiAuditConfigVO ciAuditConfigVO = ConvertUtils.convertObject(ciAuditConfigDTO, CiAuditConfigVO.class);

        List<CiAuditUserDTO> ciAuditUserDTOS = ciAuditUserService.listByAuditConfigId(ciAuditConfigDTO.getId());
        List<Long> userIds = ciAuditUserDTOS.stream().map(CiAuditUserDTO::getUserId).collect(Collectors.toList());
        ciAuditConfigVO.setCdAuditUserIds(userIds);
        return ciAuditConfigVO;
    }

    @Override
    public CiAuditConfigVO queryConfigWithUserDetailsByStepId(Long stepId) {
        CiAuditConfigVO ciAuditConfigVO = queryConfigWithUsersByStepId(stepId);
        ciAuditConfigVO.setIamUserDTOS(baseServiceClientOperator.listUsersByIds(ciAuditConfigVO.getCdAuditUserIds()));
        return ciAuditConfigVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiAuditConfigDTO baseCreate(CiAuditConfigDTO ciAuditConfigDTO) {
        return MapperUtil.resultJudgedInsertSelective(ciAuditConfigMapper, ciAuditConfigDTO, DEVOPS_AUDIT_CONFIG_CREATE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteByIds(List<Long> ids) {
        ciAuditConfigMapper.batchDeleteByIds(ids);
    }

    @Override
    public List<CiAuditConfigDTO> listByStepIds(Set<Long> stepIds) {
        if (CollectionUtils.isEmpty(stepIds)) {
            return new ArrayList<>();
        }
        return ciAuditConfigMapper.listByStepIds(stepIds);
    }
}

