package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.CiAuditResultVO;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.CiAuditRecordService;
import io.choerodon.devops.app.service.CiAuditUserRecordService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.CiAuditRecordDTO;
import io.choerodon.devops.infra.dto.CiAuditUserRecordDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.enums.AuditStatusEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.CiAuditRecordMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * ci 人工卡点审核记录表(CiAuditRecord)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-03 10:16:51
 */
@Service
public class CiAuditRecordServiceImpl implements CiAuditRecordService {

    private static final String DEVOPS_AUDIT_RECORD_SAVE = "devops.audit.record.save";

    @Autowired
    private CiAuditRecordMapper ciAuditRecordMapper;
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private CiAuditUserRecordService ciAuditUserRecordService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Override
    public CiAuditRecordDTO queryByUniqueOption(Long appServiceId, Long gitlabPipelineId, String name) {
        CiAuditRecordDTO ciAuditRecordDTO = new CiAuditRecordDTO(appServiceId,
                gitlabPipelineId,
                name);
        return ciAuditRecordMapper.selectOne(ciAuditRecordDTO);
    }

    @Override
    public CiAuditRecordDTO baseCreate(CiAuditRecordDTO ciAuditRecordDTO) {
        return MapperUtil.resultJudgedInsertSelective(ciAuditRecordMapper, ciAuditRecordDTO, DEVOPS_AUDIT_RECORD_SAVE);
    }

    @Override
    public CiAuditResultVO queryAuditStatus(String token, Long gitlabPipelineId, String jobName) {
        AppServiceDTO appServiceDTO = appServiceService.queryByTokenOrThrowE(token);
        CiAuditRecordDTO ciAuditRecordDTO = queryByUniqueOption(appServiceDTO.getId(), gitlabPipelineId, jobName);
        List<CiAuditUserRecordDTO> auditUserRecordDTOList = ciAuditUserRecordService.listByAuditRecordId(ciAuditRecordDTO.getId());
        CiAuditResultVO ciAuditResultVO = new CiAuditResultVO();
        ciAuditResultVO.setCountersigned(ciAuditRecordDTO.getCountersigned());


        if (ciAuditRecordDTO.getCountersigned()) {
            ciAuditResultVO.setSuccess(auditUserRecordDTOList.stream().allMatch(v -> AuditStatusEnum.PASSED.value().equals(v.getStatus())));
        } else {
            ciAuditResultVO.setSuccess(auditUserRecordDTOList.stream().anyMatch(v -> AuditStatusEnum.PASSED.value().equals(v.getStatus())));
        }
        List<Long> userIds = auditUserRecordDTOList.stream().map(CiAuditUserRecordDTO::getUserId).collect(Collectors.toList());
        List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.listUsersByIds(userIds);
        if (!CollectionUtils.isEmpty(iamUserDTOS)) {
            Map<Long, IamUserDTO> userMap = iamUserDTOS.stream().collect(Collectors.toMap(IamUserDTO::getId, Function.identity()));

            auditUserRecordDTOList.forEach(r -> {
                IamUserDTO iamUserDTO = userMap.get(r.getUserId());
                if (iamUserDTO != null) {
                    if (AuditStatusEnum.PASSED.value().equals(r.getStatus())) {
                        ciAuditResultVO.getPassedUserNameList().add(iamUserDTO.getRealName());
                    }
                    if (AuditStatusEnum.NOT_AUDIT.value().equals(r.getStatus())) {
                        ciAuditResultVO.getNotAuditUserNameList().add(iamUserDTO.getRealName());
                    }
                    if (AuditStatusEnum.REFUSED.value().equals(r.getStatus())) {
                        ciAuditResultVO.getRefusedUserNameList().add(iamUserDTO.getRealName());
                    }
                }
            });
        }


        return ciAuditResultVO;
    }
}

