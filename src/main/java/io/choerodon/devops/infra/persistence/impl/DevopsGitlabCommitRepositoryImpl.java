package io.choerodon.devops.infra.persistence.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.CommitFormRecordVO;
import io.choerodon.devops.api.vo.iam.entity.iam.UserE;
import io.choerodon.devops.domain.application.repository.DevopsGitlabCommitRepository;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.dto.DevopsGitlabCommitDTO;
import io.choerodon.devops.infra.mapper.DevopsGitlabCommitMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DevopsGitlabCommitRepositoryImpl implements DevopsGitlabCommitRepository {

    @Autowired
    DevopsGitlabCommitMapper devopsGitlabCommitMapper;

    @Override
    public DevopsGitlabCommitE baseCreate(DevopsGitlabCommitE devopsGitlabCommitE) {
        DevopsGitlabCommitDTO devopsGitlabCommitDO =
                ConvertHelper.convert(devopsGitlabCommitE, DevopsGitlabCommitDTO.class);
        if (!checkExist(devopsGitlabCommitE)) {
            if (devopsGitlabCommitMapper.insert(devopsGitlabCommitDO) != 1) {
                throw new CommonException("error.gitlab.commit.create");
            }
        }
        return ConvertHelper.convert(devopsGitlabCommitDO, DevopsGitlabCommitE.class);
    }

    @Override
    public DevopsGitlabCommitE baseQueryByShaAndRef(String sha, String ref) {
        DevopsGitlabCommitDTO devopsGitlabCommitDO = new DevopsGitlabCommitDTO();
        devopsGitlabCommitDO.setCommitSha(sha);
        devopsGitlabCommitDO.setRef(ref);
        return ConvertHelper.convert(devopsGitlabCommitMapper.selectOne(devopsGitlabCommitDO),
                DevopsGitlabCommitE.class);
    }

    @Override
    public List<DevopsGitlabCommitE> baseListByOptions(Long projectId, List<Long> appIds, Date startDate, Date endDate) {
        List<DevopsGitlabCommitDTO> devopsGitlabCommitDOList = devopsGitlabCommitMapper
                .listCommits(projectId, appIds, new java.sql.Date(startDate.getTime()), new java.sql.Date(endDate.getTime()));
        if (devopsGitlabCommitDOList == null || devopsGitlabCommitDOList.isEmpty()) {
            return new ArrayList<>();
        }
        return ConvertHelper.convertList(devopsGitlabCommitDOList, DevopsGitlabCommitE.class);
    }

    @Override
    public PageInfo<CommitFormRecordVO> basePageByOptions(Long projectId, List<Long> appId,
                                                          PageRequest pageRequest, Map<Long, UserE> userMap,
                                                          Date startDate, Date endDate) {
        List<CommitFormRecordVO> commitFormRecordVOList = new ArrayList<>();

        PageInfo<DevopsGitlabCommitDTO> devopsGitlabCommitDOPage = PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(),
                PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(
                () -> devopsGitlabCommitMapper.listCommits(projectId, appId, new java.sql.Date(startDate.getTime()), new java.sql.Date(endDate.getTime())));

        devopsGitlabCommitDOPage.getList().forEach(e -> {
            Long userId = e.getUserId();
            UserE user = userMap.get(userId);
            if (user != null) {
                CommitFormRecordVO commitFormRecordVO = new CommitFormRecordVO(userId, user.getImageUrl(),
                        user.getRealName() + " " + user.getLoginName()
                        , e);
                commitFormRecordVOList.add(commitFormRecordVO);
            } else {
                CommitFormRecordVO commitFormRecordVO = new CommitFormRecordVO(null, null,
                        null, e);
                commitFormRecordVOList.add(commitFormRecordVO);
            }
        });
        PageInfo<CommitFormRecordVO> commitFormRecordDTOPagee = new PageInfo<>();
        BeanUtils.copyProperties(devopsGitlabCommitDOPage, commitFormRecordDTOPagee);
        commitFormRecordDTOPagee.setList(commitFormRecordVOList);

        return commitFormRecordDTOPagee;
    }

    @Override
    public void baseUpdate(DevopsGitlabCommitE devopsGitlabCommitE) {
        DevopsGitlabCommitDTO oldDevopsGitlabCommitDO = devopsGitlabCommitMapper.selectByPrimaryKey(devopsGitlabCommitE.getId());
        DevopsGitlabCommitDTO newDevopsGitlabCommitDO = ConvertHelper.convert(devopsGitlabCommitE, DevopsGitlabCommitDTO.class);
        newDevopsGitlabCommitDO.setObjectVersionNumber(oldDevopsGitlabCommitDO.getObjectVersionNumber());
        if (devopsGitlabCommitMapper.updateByPrimaryKeySelective(newDevopsGitlabCommitDO) != 1) {
            throw new CommonException("error.gitlab.commit.update");
        }
    }

    @Override
    public List<DevopsGitlabCommitE> baseListByAppIdAndBranch(Long appId, String branch, Date startDate) {
        return ConvertHelper.convertList(devopsGitlabCommitMapper.queryByAppIdAndBranch(appId, branch, startDate == null ? null : new java.sql.Date(startDate.getTime())), DevopsGitlabCommitE.class);
    }


    public boolean checkExist(DevopsGitlabCommitE devopsGitlabCommitE) {
        DevopsGitlabCommitDTO devopsGitlabCommitDO = new DevopsGitlabCommitDTO();
        devopsGitlabCommitDO.setCommitSha(devopsGitlabCommitE.getCommitSha());
        devopsGitlabCommitDO.setRef(devopsGitlabCommitE.getRef());
        if (devopsGitlabCommitMapper.selectOne(devopsGitlabCommitDO) != null) {
            return true;
        }
        return false;
    }
}
