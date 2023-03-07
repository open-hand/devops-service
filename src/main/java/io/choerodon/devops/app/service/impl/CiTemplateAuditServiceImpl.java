package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.pipeline.CiAuditConfigVO;
import io.choerodon.devops.api.vo.pipeline.CiTemplateAuditConfigVO;
import io.choerodon.devops.app.service.CiTemplateAuditService;
import io.choerodon.devops.app.service.CiTemplateAuditUserService;
import io.choerodon.devops.infra.dto.CiTemplateAuditDTO;
import io.choerodon.devops.infra.dto.CiTemplateAuditUserDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.CiTemplateAuditMapper;
import io.choerodon.devops.infra.util.ConvertUtils;

/**
 * ci 人工卡点模板配置表(CiTemplateAudit)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-02 14:39:47
 */
@Service
public class CiTemplateAuditServiceImpl implements CiTemplateAuditService {
    @Autowired
    private CiTemplateAuditMapper ciTemplateAuditMapper;
    @Autowired
    private CiTemplateAuditUserService ciTemplateAuditUserService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Override
    public CiTemplateAuditConfigVO queryConfigWithUsersById(Long id) {
        CiTemplateAuditDTO ciTemplateAuditDTO = ciTemplateAuditMapper.selectByPrimaryKey(id);
        CiTemplateAuditConfigVO ciTemplateAuditConfigVO = ConvertUtils.convertObject(ciTemplateAuditDTO, CiTemplateAuditConfigVO.class);

        List<CiTemplateAuditUserDTO> ciTemplateAuditUserDTOS = ciTemplateAuditUserService.listByConfigId(ciTemplateAuditDTO.getId());
        List<Long> userIds = ciTemplateAuditUserDTOS.stream().map(CiTemplateAuditUserDTO::getUserId).collect(Collectors.toList());
        ciTemplateAuditConfigVO.setCdAuditUserIds(userIds);
        ciTemplateAuditConfigVO.setIamUserDTOS(baseServiceClientOperator.listUsersByIds(userIds));

        return ciTemplateAuditConfigVO;
    }

    @Override
    public CiAuditConfigVO queryConfigWithUserDetailsById(Long id) {
        CiTemplateAuditConfigVO ciTemplateAuditConfigVO = queryConfigWithUsersById(id);
        List<Long> cdAuditUserIds = ciTemplateAuditConfigVO.getCdAuditUserIds();
        if (!CollectionUtils.isEmpty(cdAuditUserIds)) {
            ciTemplateAuditConfigVO.setIamUserDTOS(baseServiceClientOperator.listUsersByIds(cdAuditUserIds));
        }
        return ConvertUtils.convertObject(ciTemplateAuditConfigVO, CiAuditConfigVO.class);
    }
}

