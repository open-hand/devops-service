package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.SonarAnalyseIssueAuthorRankVO;
import io.choerodon.devops.api.vo.SonarAnalyseIssueAuthorVO;
import io.choerodon.devops.app.service.SonarAnalyseUserIssueAuthorService;
import io.choerodon.devops.infra.dto.SonarAnalyseIssueAuthorDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.SonarAnalyseUserRecordMapper;
import io.choerodon.devops.infra.util.MapperUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 代码扫描记录表(SonarAnalyseUserRecord)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-06 15:44:31
 */
@Service
public class SonarAnalyseUserIssueAuthorServiceImpl implements SonarAnalyseUserIssueAuthorService {

    private static final String DEVOPS_SAVE_SONAR_ANALYSE_USER_RECORD_FAILED = "devops.save.sonar.analyse.user.record.failed";
    @Autowired
    private SonarAnalyseUserRecordMapper sonarAnalyseUserRecordMapper;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(SonarAnalyseIssueAuthorDTO sonarAnalyseIssueAuthorDTO) {
        MapperUtil.resultJudgedInsertSelective(sonarAnalyseUserRecordMapper, sonarAnalyseIssueAuthorDTO, DEVOPS_SAVE_SONAR_ANALYSE_USER_RECORD_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(Long recordId, Collection<SonarAnalyseIssueAuthorDTO> sonarAnalyseIssueAuthorDTOList) {
        if (!CollectionUtils.isEmpty(sonarAnalyseIssueAuthorDTOList)) {
            sonarAnalyseUserRecordMapper.batchSave(recordId, sonarAnalyseIssueAuthorDTOList);
        }
    }

    @Override
    public List<SonarAnalyseIssueAuthorVO> listMemberIssue(Long appServiceId) {
        return sonarAnalyseUserRecordMapper.listMemberIssue(appServiceId);
    }

    @Override
    public List<SonarAnalyseIssueAuthorRankVO> listMemberBugRank(Long projectId, Long appServiceId) {
        List<SonarAnalyseIssueAuthorRankVO> sonarAnalyseIssueAuthorRankVOS = sonarAnalyseUserRecordMapper.listMemberBugRank(appServiceId);
        if (CollectionUtils.isEmpty(sonarAnalyseIssueAuthorRankVOS)) {
            return new ArrayList<>();
        }
        addUsername(sonarAnalyseIssueAuthorRankVOS);
        return sonarAnalyseIssueAuthorRankVOS;
    }

    private void addUsername(List<SonarAnalyseIssueAuthorRankVO> sonarAnalyseIssueAuthorRankVOS) {
        List<String> userEmails = sonarAnalyseIssueAuthorRankVOS.stream().map(SonarAnalyseIssueAuthorRankVO::getAuthor).collect(Collectors.toList());
        List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.listUsersByEmails(userEmails);
        if (!CollectionUtils.isEmpty(iamUserDTOS)) {
            Map<String, IamUserDTO> userMap = iamUserDTOS.stream().collect(Collectors.toMap(IamUserDTO::getEmail, Function.identity()));

            for (SonarAnalyseIssueAuthorRankVO sonarAnalyseIssueAuthorRankVO : sonarAnalyseIssueAuthorRankVOS) {
                IamUserDTO iamUserDTO = userMap.get(sonarAnalyseIssueAuthorRankVO.getAuthor());
                if (iamUserDTO != null) {
                    sonarAnalyseIssueAuthorRankVO.setUsername(iamUserDTO.getRealName());
                } else {
                    sonarAnalyseIssueAuthorRankVO.setUsername(sonarAnalyseIssueAuthorRankVO.getAuthor());
                }
            }
        }
    }

    @Override
    public Page<SonarAnalyseIssueAuthorVO> listMemberIssue(Long projectId, Long appServiceId, PageRequest pageRequest) {
        Page<SonarAnalyseIssueAuthorVO> page = PageHelper.doPageAndSort(pageRequest, () -> sonarAnalyseUserRecordMapper.listMemberIssue(appServiceId));
        List<SonarAnalyseIssueAuthorVO> content = page.getContent();
        if (CollectionUtils.isEmpty(content)) {
            return page;
        }
        List<String> userEmails = content.stream().map(SonarAnalyseIssueAuthorVO::getAuthor).collect(Collectors.toList());
        List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.listUsersByEmails(userEmails);
        if (!CollectionUtils.isEmpty(iamUserDTOS)) {
            Map<String, IamUserDTO> userMap = iamUserDTOS.stream().collect(Collectors.toMap(IamUserDTO::getEmail, Function.identity()));

            for (SonarAnalyseIssueAuthorVO sonarAnalyseIssueAuthorVO : content) {
                IamUserDTO iamUserDTO = userMap.get(sonarAnalyseIssueAuthorVO.getAuthor());
                if (iamUserDTO != null) {
                    sonarAnalyseIssueAuthorVO.setEmail(iamUserDTO.getEmail());
                    sonarAnalyseIssueAuthorVO.setRealName(iamUserDTO.getRealName());
                    sonarAnalyseIssueAuthorVO.setImageUrl(iamUserDTO.getImageUrl());
                }
            }
        }
        return page;
    }

    @Override
    public List<SonarAnalyseIssueAuthorRankVO> listMemberVulnRank(Long projectId, Long appServiceId) {
        List<SonarAnalyseIssueAuthorRankVO> sonarAnalyseIssueAuthorRankVOS = sonarAnalyseUserRecordMapper.listMemberVulnRank(appServiceId);
        if (CollectionUtils.isEmpty(sonarAnalyseIssueAuthorRankVOS)) {
            return new ArrayList<>();
        }
        addUsername(sonarAnalyseIssueAuthorRankVOS);
        return sonarAnalyseIssueAuthorRankVOS;
    }

    @Override
    public List<SonarAnalyseIssueAuthorRankVO> listMemberCodeSmellRank(Long projectId, Long appServiceId) {
        List<SonarAnalyseIssueAuthorRankVO> sonarAnalyseIssueAuthorRankVOS = sonarAnalyseUserRecordMapper.listMemberCodeSmellRank(appServiceId);
        if (CollectionUtils.isEmpty(sonarAnalyseIssueAuthorRankVOS)) {
            return new ArrayList<>();
        }
        addUsername(sonarAnalyseIssueAuthorRankVOS);
        return sonarAnalyseIssueAuthorRankVOS;
    }
}

