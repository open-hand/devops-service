package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.infra.dto.CiTemplateJobDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/12/16
 */
public interface CiTemplateJobBusService {
    List<CiTemplateJobVO> queryTemplateJobsByGroupId(Long sourceId, Long ciTemplateJobGroupId);

    CiTemplateJobVO createTemplateJob(Long sourceId, String sourceType, CiTemplateJobVO ciTemplateJobVO);

    CiTemplateJobVO updateTemplateJob(Long sourceId, String sourceType, CiTemplateJobVO ciTemplateJobVO);

    void deleteTemplateJob(Long sourceId, String sourceType, Long jobId);

    Boolean isNameUnique(String name, Long sourceId, Long jobId);


    Page<CiTemplateJobVO> pageTemplateJobs(Long sourceId, String sourceType, PageRequest pageRequest, String name, Long groupId, Boolean builtIn, String params);

    Boolean checkJobTemplateByJobId(Long sourceId, Long templateJobId);

    List<CiTemplateJobVO> listTemplateJobs(Long sourceId, String sourceType);

    CiTemplateJobVO queryTemplateByJobById(Long sourceId, Long templateJobId);

    void fillCdJobConfig(CiTemplateJobVO ciTemplateJobVO);

    void deleteTemplateJobConfig(CiTemplateJobDTO ciTemplateJobDTO);

    void deleteTemplateJobByIds(Set<Long> ciTemplateJobIds);

    void createNonVisibilityJob(Long sourceId, String sourceType, List<CiTemplateJobVO> ciTemplateJobVOList);

    boolean checkName(Long projectId, String newName);

    void initNonVisibilityJob(Long sourceId, CiTemplateJobVO ciTemplateJobVO);

}
