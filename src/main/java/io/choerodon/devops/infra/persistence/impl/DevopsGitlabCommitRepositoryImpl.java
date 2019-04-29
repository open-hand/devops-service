package io.choerodon.devops.infra.persistence.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DevopsGitlabCommitRepositoryImpl implements DevopsGitlabCommitRepository {

    @Autowired
    DevopsGitlabCommitMapper devopsGitlabCommitMapper;

    @Override
    public DevopsGitlabCommitE create(DevopsGitlabCommitE devopsGitlabCommitE) {
        DevopsGitlabCommitDO devopsGitlabCommitDO =
                ConvertHelper.convert(devopsGitlabCommitE, DevopsGitlabCommitDO.class);
        if (!checkExist(devopsGitlabCommitE)) {
            if (devopsGitlabCommitMapper.insert(devopsGitlabCommitDO) != 1) {
                throw new CommonException("error.gitlab.commit.create");
            }
        }
        return ConvertHelper.convert(devopsGitlabCommitDO, DevopsGitlabCommitE.class);
    }

    @Override
    public DevopsGitlabCommitE queryByShaAndRef(String sha, String ref) {
        DevopsGitlabCommitDO devopsGitlabCommitDO = new DevopsGitlabCommitDO();
        devopsGitlabCommitDO.setCommitSha(sha);
        devopsGitlabCommitDO.setRef(ref);
        return ConvertHelper.convert(devopsGitlabCommitMapper.selectOne(devopsGitlabCommitDO),
                DevopsGitlabCommitE.class);
    }

    @Override
    public List<DevopsGitlabCommitE> listCommits(Long projectId, List<Long> appIds, Date startDate, Date endDate) {
        List<DevopsGitlabCommitDO> devopsGitlabCommitDOList = devopsGitlabCommitMapper
                .listCommits(projectId, appIds, new java.sql.Date(startDate.getTime()), new java.sql.Date(endDate.getTime()));
        if (devopsGitlabCommitDOList == null || devopsGitlabCommitDOList.isEmpty()) {
            return new ArrayList<>();
        }
        return ConvertHelper.convertList(devopsGitlabCommitDOList, DevopsGitlabCommitE.class);
    }

    @Override
    public Page<CommitFormRecordDTO> pageCommitRecord(Long projectId, List<Long> appId,
                                                      PageRequest pageRequest, Map<Long, UserE> userMap,
                                                      Date startDate, Date endDate) {
        List<CommitFormRecordDTO> commitFormRecordDTOList = new ArrayList<>();

        Page<DevopsGitlabCommitDO> devopsGitlabCommitDOPage = PageHelper.doPageAndSort(pageRequest,
                () -> devopsGitlabCommitMapper.listCommits(projectId, appId, new java.sql.Date(startDate.getTime()), new java.sql.Date(endDate.getTime())));

        devopsGitlabCommitDOPage.getContent().forEach(e -> {
            Long userId = e.getUserId();
            UserE user = userMap.get(userId);
            if (user != null) {
                CommitFormRecordDTO commitFormRecordDTO = new CommitFormRecordDTO(userId, user.getImageUrl(),
                        user.getRealName() + " " + user.getLoginName()
                        , e);
                commitFormRecordDTOList.add(commitFormRecordDTO);
            } else {
                CommitFormRecordDTO commitFormRecordDTO = new CommitFormRecordDTO(null, null,
                        null, e);
                commitFormRecordDTOList.add(commitFormRecordDTO);
            }
        });
        Page<CommitFormRecordDTO> commitFormRecordDTOPagee = new Page<>();
        BeanUtils.copyProperties(devopsGitlabCommitDOPage, commitFormRecordDTOPagee);
        commitFormRecordDTOPagee.setContent(commitFormRecordDTOList);

        return commitFormRecordDTOPagee;
    }

    @Override
    public void update(DevopsGitlabCommitE devopsGitlabCommitE) {
        DevopsGitlabCommitDO oldDevopsGitlabCommitDO = devopsGitlabCommitMapper.selectByPrimaryKey(devopsGitlabCommitE.getId());
        DevopsGitlabCommitDO newDevopsGitlabCommitDO = ConvertHelper.convert(devopsGitlabCommitE, DevopsGitlabCommitDO.class);
        newDevopsGitlabCommitDO.setObjectVersionNumber(oldDevopsGitlabCommitDO.getObjectVersionNumber());
        if (devopsGitlabCommitMapper.updateByPrimaryKeySelective(newDevopsGitlabCommitDO) != 1) {
            throw new CommonException("error.gitlab.commit.update");
        }
    }


    public boolean checkExist(DevopsGitlabCommitE devopsGitlabCommitE) {
        DevopsGitlabCommitDO devopsGitlabCommitDO = new DevopsGitlabCommitDO();
        devopsGitlabCommitDO.setCommitSha(devopsGitlabCommitE.getCommitSha());
        devopsGitlabCommitDO.setRef(devopsGitlabCommitE.getRef());
        if (devopsGitlabCommitMapper.selectOne(devopsGitlabCommitDO) != null) {
            return true;
        }
        return false;
    }
}
