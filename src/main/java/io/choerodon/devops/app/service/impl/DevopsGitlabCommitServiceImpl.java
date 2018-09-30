package io.choerodon.devops.app.service.impl;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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

    private static final Gson gson = new Gson();

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
        });
    }

    @Override
    public DevopsGitlabCommitDTO getCommits(Long projectId, String appIds, String startDate, String endDate) {

        List<Long> appIdsMap = gson.fromJson(appIds, new TypeToken<List<Long>>() {
        }.getType());
        if (appIdsMap.isEmpty()) {
            return new DevopsGitlabCommitDTO();
        }
        // 如果传入的时间为null，表示查询至今所有的commit记录
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String sd = "null".equals(startDate) ? "1970-01-01" : sdf.format(new Date(Long.valueOf(startDate)));
        String ed = "null".equals(endDate) ?
                sdf.format(new Date(System.currentTimeMillis())) :
                sdf.format(new Date(Long.valueOf(endDate)));

        // 查询应用列表下所有commit记录
        List<DevopsGitlabCommitE> devopsGitlabCommitES = devopsGitlabCommitRepository
                .listCommits(projectId, appIdsMap, sd, ed);
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
    public Page<CommitFormRecordDTO> getRecordCommits(Long projectId, String appIds, PageRequest pageRequest,
                                                      String startDate, String endDate) {

        List<Long> appIdsMap = gson.fromJson(appIds, new TypeToken<List<Long>>() {
        }.getType());
        if (appIdsMap.isEmpty()) {
            return new Page<>();
        }
        // 如果传入的时间为null，表示查询至今所有的commit记录
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String sd = "null".equals(startDate) ? "1970-01-01" : sdf.format(new Date(Long.valueOf(startDate)));
        String ed = "null".equals(endDate) ?
                sdf.format(new Date(System.currentTimeMillis())) :
                sdf.format(new Date(Long.valueOf(endDate)));
        // 查询应用列表下所有commit记录
        List<DevopsGitlabCommitE> devopsGitlabCommitES = devopsGitlabCommitRepository
                .listCommits(projectId, appIdsMap, sd, ed);
        Map<Long, UserE> userMap = getUserDOMap(devopsGitlabCommitES);
        // 获取最近的commit(返回所有的commit记录，按时间先后排序，分页查询)
        return getCommitFormRecordDTOS(projectId, appIdsMap, pageRequest, userMap, sd, ed);
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

    private Page<CommitFormRecordDTO> getCommitFormRecordDTOS(Long projectId, List<Long> appId, PageRequest pageRequest,
                                                              Map<Long, UserE> userMap, String startDate, String endDate) {
        return devopsGitlabCommitRepository.pageCommitRecord(projectId, appId, pageRequest, userMap, startDate, endDate);
    }

    private List<Date> getTotalDates(List<CommitFormUserDTO> commitFormUserDTOS) {
        List<Date> totalCommitsDate = new ArrayList<>();
        commitFormUserDTOS.forEach(e -> totalCommitsDate.addAll(e.getCommitDates()));
        return totalCommitsDate;
    }
}
