package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.CommitFormRecordDTO;
import io.choerodon.devops.api.vo.CommitFormUserDTO;
import io.choerodon.devops.api.vo.DevopsGitlabCommitDTO;
import io.choerodon.devops.api.vo.PushWebHookDTO;
import io.choerodon.devops.app.service.DevopsGitlabCommitService;
import io.choerodon.devops.api.vo.iam.entity.ApplicationE;
import io.choerodon.devops.api.vo.iam.entity.DevopsGitlabCommitE;
import io.choerodon.devops.api.vo.iam.entity.gitlab.CommitE;
import io.choerodon.devops.api.vo.iam.entity.iam.UserE;
import io.choerodon.devops.domain.application.repository.ApplicationRepository;
import io.choerodon.devops.domain.application.repository.DevopsGitlabCommitRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.infra.util.TypeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DevopsGitlabCommitServiceImpl implements DevopsGitlabCommitService {

    private static final Gson gson = new Gson();
    private static final Integer ADMIN = 1;

    @Autowired
    IamRepository iamRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private DevopsGitlabCommitRepository devopsGitlabCommitRepository;
    @Autowired
    private DevopsGitRepository devopsGitRepository;

    @Override
    public void create(PushWebHookDTO pushWebHookDTO, String token) {
        ApplicationE applicationE = applicationRepository.queryByToken(token);
        String ref = pushWebHookDTO.getRef().split("/")[2];
        if (!pushWebHookDTO.getCommits().isEmpty()) {
            pushWebHookDTO.getCommits().forEach(commitDTO -> {
                DevopsGitlabCommitE devopsGitlabCommitE = devopsGitlabCommitRepository.queryByShaAndRef(commitDTO.getId(), ref);

                if (devopsGitlabCommitE == null) {
                    devopsGitlabCommitE = new DevopsGitlabCommitE();
                    devopsGitlabCommitE.setAppId(applicationE.getId());
                    devopsGitlabCommitE.setCommitContent(commitDTO.getMessage());
                    devopsGitlabCommitE.setCommitSha(commitDTO.getId());
                    devopsGitlabCommitE.setRef(ref);
                    devopsGitlabCommitE.setUrl(commitDTO.getUrl());
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
                }
            });
        } else {
            //直接从一个分支切出来另外一个分支，没有commits记录
            DevopsGitlabCommitE devopsGitlabCommitE = devopsGitlabCommitRepository.queryByShaAndRef(pushWebHookDTO.getCheckoutSha(), ref);
            if (devopsGitlabCommitE == null) {
                CommitE commitE = devopsGitRepository.getCommit(TypeUtil.objToInteger(applicationE.getGitlabProjectE().getId()), pushWebHookDTO.getCheckoutSha(), ADMIN);
                devopsGitlabCommitE = new DevopsGitlabCommitE();
                devopsGitlabCommitE.setAppId(applicationE.getId());
                devopsGitlabCommitE.setCommitContent(commitE.getMessage());
                devopsGitlabCommitE.setCommitSha(commitE.getId());
                devopsGitlabCommitE.setRef(ref);
                devopsGitlabCommitE.setUrl(commitE.getUrl());
                if ("root".equals(commitE.getAuthorName())) {
                    devopsGitlabCommitE.setUserId(1L);
                } else {
                    UserE userE = iamRepository.queryByEmail(applicationE.getProjectE().getId(),
                            commitE.getAuthorEmail());
                    if (userE != null) {
                        devopsGitlabCommitE.setUserId(userE.getId());
                    }
                }
                devopsGitlabCommitE.setCommitDate(commitE.getCommittedDate());
                devopsGitlabCommitRepository.create(devopsGitlabCommitE);
            }
        }

    }

    @Override
    public DevopsGitlabCommitDTO getCommits(Long projectId, String appIds, Date startDate, Date endDate) {

        List<Long> appIdsMap = gson.fromJson(appIds, new TypeToken<List<Long>>() {
        }.getType());
        if (appIdsMap.isEmpty()) {
            return new DevopsGitlabCommitDTO();
        }

        // 查询应用列表下所有commit记录
        List<DevopsGitlabCommitE> devopsGitlabCommitES = devopsGitlabCommitRepository
                .listCommits(projectId, appIdsMap, startDate, endDate);
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
    public PageInfo<CommitFormRecordDTO> getRecordCommits(Long projectId, String appIds, PageRequest pageRequest,
                                                          Date startDate, Date endDate) {

        List<Long> appIdsMap = gson.fromJson(appIds, new TypeToken<List<Long>>() {
        }.getType());
        if (appIdsMap.isEmpty()) {
            return new PageInfo<>();
        }

        // 查询应用列表下所有commit记录
        List<DevopsGitlabCommitE> devopsGitlabCommitES = devopsGitlabCommitRepository
                .listCommits(projectId, appIdsMap, startDate, endDate);
        Map<Long, UserE> userMap = getUserDOMap(devopsGitlabCommitES);
        // 获取最近的commit(返回所有的commit记录，按时间先后排序，分页查询)
        return getCommitFormRecordDTOS(projectId, appIdsMap, pageRequest, userMap, startDate, endDate);
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
        // 遍历list，key为userid，value为list
        Map<Long, List<DevopsGitlabCommitE>> map = new HashMap<>();
        for (DevopsGitlabCommitE commitE : devopsGitlabCommitES) {
            Long userId = commitE.getUserId();
            if (userId == null && !map.containsKey(0L)) {
                List<DevopsGitlabCommitE> commitES = new ArrayList<>();
                commitES.add(commitE);
                map.put(0L, commitES);
            } else if (userId == null && map.containsKey(0L)) {
                map.get(0L).add(commitE);
            } else if (userId != null && !map.containsKey(userId)) {
                List<DevopsGitlabCommitE> commitES = new ArrayList<>();
                commitES.add(commitE);
                map.put(userId, commitES);
            } else {
                map.get(userId).add(commitE);
            }
        }
        map.forEach((userId, list) -> {
            UserE userE = userMap.get(userId);
            String name = userE == null ? null : userE.getRealName() + userE.getLoginName();
            String imgUrl = userE == null ? null : userE.getImageUrl();
            // 遍历list，将每个用户的所有commitdate取出放入List<Date>，然后保存为DTO
            List<Date> date = new ArrayList<>();
            list.forEach(e -> date.add(e.getCommitDate()));
            commitFormUserDTOS.add(new CommitFormUserDTO(userId, name, imgUrl, date));
        });
        return commitFormUserDTOS;
    }

    private PageInfo<CommitFormRecordDTO> getCommitFormRecordDTOS(Long projectId, List<Long> appId, PageRequest pageRequest,
                                                              Map<Long, UserE> userMap, Date startDate, Date endDate) {
        return devopsGitlabCommitRepository.pageCommitRecord(projectId, appId, pageRequest, userMap, startDate, endDate);
    }

    private List<Date> getTotalDates(List<CommitFormUserDTO> commitFormUserDTOS) {
        List<Date> totalCommitsDate = new ArrayList<>();
        commitFormUserDTOS.forEach(e -> totalCommitsDate.addAll(e.getCommitDates()));
        return totalCommitsDate;
    }
}
