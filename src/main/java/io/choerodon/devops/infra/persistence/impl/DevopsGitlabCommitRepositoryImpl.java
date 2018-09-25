package io.choerodon.devops.infra.persistence.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.CommitFormRecordDTO;
import io.choerodon.devops.domain.application.entity.DevopsGitlabCommitE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.repository.DevopsGitlabCommitRepository;
import io.choerodon.devops.infra.dataobject.DevopsGitlabCommitDO;
import io.choerodon.devops.infra.mapper.DevopsGitlabCommitMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Service
public class DevopsGitlabCommitRepositoryImpl implements DevopsGitlabCommitRepository {

    @Autowired
    DevopsGitlabCommitMapper devopsGitlabCommitMapper;

    @Override
    public DevopsGitlabCommitE create(DevopsGitlabCommitE devopsGitlabCommitE) {
        DevopsGitlabCommitDO devopsGitlabCommitDO =
                ConvertHelper.convert(devopsGitlabCommitE, DevopsGitlabCommitDO.class);
        if (devopsGitlabCommitMapper.insert(devopsGitlabCommitDO) != 1) {
            throw new CommonException("error.gitlab.commit.create");
        }
        return ConvertHelper.convert(devopsGitlabCommitDO, DevopsGitlabCommitE.class);
    }

    @Override
    public DevopsGitlabCommitE queryBySha(String sha) {
        DevopsGitlabCommitDO devopsGitlabCommitDO = new DevopsGitlabCommitDO();
        devopsGitlabCommitDO.setCommitSha(sha);
        return ConvertHelper.convert(devopsGitlabCommitMapper.selectOne(devopsGitlabCommitDO),
                DevopsGitlabCommitE.class);
    }

    @Override
    public List<DevopsGitlabCommitE> listCommits(Long projectId, List<Long> appIds, String startDate, String endDate) {
        List<DevopsGitlabCommitDO> devopsGitlabCommitDOList = devopsGitlabCommitMapper
                .listCommits(projectId, appIds, startDate, endDate);
        if (devopsGitlabCommitDOList == null || devopsGitlabCommitDOList.isEmpty()) {
            return new ArrayList<>();
        }
        return ConvertHelper.convertList(devopsGitlabCommitDOList, DevopsGitlabCommitE.class);
    }

    @Override
    public Page<CommitFormRecordDTO> pageCommitRecord(Long projectId, List<Long> appId,
                                                      PageRequest pageRequest, Map<Long, UserE> userMap,
                                                      String startDate, String endDate) {
        List<CommitFormRecordDTO> commitFormRecordDTOList = new ArrayList<>();

        Page<DevopsGitlabCommitDO> devopsGitlabCommitDOPage = PageHelper.doPageAndSort(pageRequest,
                () -> devopsGitlabCommitMapper.listCommits(projectId, appId, startDate, endDate));

        devopsGitlabCommitDOPage.getContent().forEach(e -> {
            Long userId = e.getUserId();
            UserE user = userMap.get(userId);
            if (user != null) {
                CommitFormRecordDTO commitFormRecordDTO = new CommitFormRecordDTO(userId, e.getAppId(), user.getImageUrl(),
                        e.getCommitContent(), user.getRealName() + " " + user.getLoginName(), e.getCommitDate(),
                        e.getCommitSha(), e.getAppName());
                commitFormRecordDTOList.add(commitFormRecordDTO);
            } else {
                CommitFormRecordDTO commitFormRecordDTO = new CommitFormRecordDTO(null, e.getAppId(), null, e.getCommitContent(),
                        null, e.getCommitDate(), e.getCommitSha(), e.getAppName());
                commitFormRecordDTOList.add(commitFormRecordDTO);
            }
        });
        Page<CommitFormRecordDTO> commitFormRecordDTOPagee = new Page<>();
        BeanUtils.copyProperties(devopsGitlabCommitDOPage, commitFormRecordDTOPagee);
        commitFormRecordDTOPagee.setContent(commitFormRecordDTOList);

        return commitFormRecordDTOPagee;
    }
}
