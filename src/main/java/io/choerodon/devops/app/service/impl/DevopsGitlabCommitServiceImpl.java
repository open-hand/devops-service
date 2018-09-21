package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.CommitFormRecordDTO;
import io.choerodon.devops.api.dto.CommitFormUserDTO;
import io.choerodon.devops.api.dto.DevopsGitlabCommitDTO;
import io.choerodon.devops.api.dto.PushWebHookDTO;
import io.choerodon.devops.app.service.DevopsGitlabCommitService;
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.domain.application.entity.DevopsGitlabCommitE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.repository.ApplicationRepository;
import io.choerodon.devops.domain.application.repository.DevopsGitlabCommitRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Service
public class DevopsGitlabCommitServiceImpl implements DevopsGitlabCommitService {

    @Autowired
    IamRepository iamRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private DevopsGitlabCommitRepository devopsGitlabCommitRepository;

    @Override
    public void create(PushWebHookDTO pushWebHookDTO, String token) {
        ApplicationE applicationE = applicationRepository.queryByToken(token);
        pushWebHookDTO.getCommits().parallelStream().forEach(commitDTO -> {
            DevopsGitlabCommitE devopsGitlabCommitE = new DevopsGitlabCommitE();
            devopsGitlabCommitE.setAppId(applicationE.getId());
            devopsGitlabCommitE.setCommitContent(commitDTO.getMessage());
            devopsGitlabCommitE.setCommitSha(commitDTO.getId());
            devopsGitlabCommitE.setRef(pushWebHookDTO.getRef().split("/")[2]);
            if ("root".equals(commitDTO.getAuthor().getName())) {
                devopsGitlabCommitE.setUserId(1L);
            } else {
                UserE userE = iamRepository.queryByEmail(applicationE.getProjectE().getId(),
                        commitDTO.getAuthor().getEmail());
                if (userE != null) {
                    devopsGitlabCommitE.setUserId(userE.getId());
                }
            }
            devopsGitlabCommitE.setCommitDate(commitDTO.getTimestamp());
            devopsGitlabCommitRepository.create(devopsGitlabCommitE);
        });
    }

    @Override
    public DevopsGitlabCommitDTO getCommits(String[] appId) {
        List<Long> listStrings;
        if ("null".equals(appId[0])) {
            listStrings = null;
        } else {
            listStrings = Arrays.stream(appId).map(e -> Long.valueOf(e)).collect(Collectors.toList());
        }
        // 查询应用列表下所有commit记录
        List<DevopsGitlabCommitE> devopsGitlabCommitES = devopsGitlabCommitRepository.listCommitsByAppId(listStrings);
        if (devopsGitlabCommitES.isEmpty()) {
            return new DevopsGitlabCommitDTO();
        }

        // 获得去重后的所有用户信息
        Map<Long, UserE> userMap = getUserDOMap(devopsGitlabCommitES);

        // 获取用户分别的commit
        List<CommitFormUserDTO> commitFormUserDTOS = getCommitFormUserDTOList(devopsGitlabCommitES, userMap);

        // 获取总的commit(将所有用户的commit_date放入一个数组)，按照时间先后排序
        List<Date> totalCommitsDate = getTotalDates(commitFormUserDTOS);
        Collections.sort(totalCommitsDate);

        return new DevopsGitlabCommitDTO(commitFormUserDTOS, totalCommitsDate);
    }

    @Override
    public Page<CommitFormRecordDTO> getRecordCommits(String[] appIds, PageRequest pageRequest) {
        List<Long> listStrings;
        if ("null".equals(appIds[0])) {
            listStrings = null;
        } else {
            listStrings = Arrays.stream(appIds).map(e -> Long.valueOf(e)).collect(Collectors.toList());
        }
        // 查询应用列表下所有commit记录
        List<DevopsGitlabCommitE> devopsGitlabCommitES = devopsGitlabCommitRepository.listCommitsByAppId(listStrings);
        Map<Long, UserE> userMap = getUserDOMap(devopsGitlabCommitES);
        // 获取最近的commit(返回所有的commit记录，按时间先后排序，分页查询)
        return getCommitFormRecordDTOS(listStrings, pageRequest, userMap);
    }

    private Map<Long, UserE> getUserDOMap(List<DevopsGitlabCommitE> devopsGitlabCommitES) {
        // 获取users
        List<UserE> userEList = iamRepository.listUsersByIds(devopsGitlabCommitES.stream().map(
                DevopsGitlabCommitE::getUserId).distinct().collect(Collectors.toList()));

        return userEList.stream().collect(Collectors.toMap(UserE::getId, u -> u, (u1, u2) -> u1));
    }

    private List<CommitFormUserDTO> getCommitFormUserDTOList(List<DevopsGitlabCommitE> devopsGitlabCommitES,
                                                             Map<Long, UserE> userMap) {
        List<CommitFormUserDTO> commitFormUserDTOS = new ArrayList<>();
        // 遍历map，key为userid，value为list
        Map<Long, List<DevopsGitlabCommitE>> groupByUserIdCommitsMap = devopsGitlabCommitES.stream()
                .collect(Collectors.groupingBy(DevopsGitlabCommitE::getUserId));
        groupByUserIdCommitsMap.forEach((userId, list) -> {
            UserE userE = userMap.get(userId);
            String name = userE == null ? "" : userE.getLoginName() + userE.getRealName();
            String imgUrl = userE == null ? "" : userE.getImageUrl();
            // 遍历list，将每个用户的所有commitdate取出放入List<Date>，然后保存为DTO
            List<Date> date = new ArrayList<>();
            list.forEach(e -> date.add(e.getCommitDate()));
            commitFormUserDTOS.add(new CommitFormUserDTO(userId, name, imgUrl, date));
        });
        return commitFormUserDTOS;
    }

    private Page<CommitFormRecordDTO> getCommitFormRecordDTOS(List<Long> appId, PageRequest pageRequest,
                                                              Map<Long, UserE> userMap) {
        return devopsGitlabCommitRepository.pageCommitRecord(appId, pageRequest, userMap);
    }

    private List<Date> getTotalDates(List<CommitFormUserDTO> commitFormUserDTOS) {
        List<Date> totalCommitsDate = new ArrayList<>();
        commitFormUserDTOS.forEach(e -> totalCommitsDate.addAll(e.getCommitDates()));
        return totalCommitsDate;
    }
}
