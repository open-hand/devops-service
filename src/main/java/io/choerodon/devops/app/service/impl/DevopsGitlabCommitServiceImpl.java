package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.CommitFormRecordDTO;
import io.choerodon.devops.api.vo.CommitFormUserDTO;
import io.choerodon.devops.api.vo.DevopsGitlabCommitVO;
import io.choerodon.devops.api.vo.PushWebHookDTO;
import io.choerodon.devops.app.service.DevopsGitlabCommitService;
import io.choerodon.devops.domain.application.repository.DevopsGitlabCommitRepository;
import io.choerodon.devops.infra.dto.DevopsGitlabCommitDTO;
import io.choerodon.devops.infra.mapper.DevopsGitlabCommitMapper;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DevopsGitlabCommitServiceImpl implements DevopsGitlabCommitService {

    private static final Gson gson = new Gson();
    private static final Integer ADMIN = 1;

    @Autowired
    IamRepository iamRepository;
    @Autowired
    DevopsGitlabCommitMapper devopsGitlabCommitMapper;
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
                DevopsGitlabCommitE devopsGitlabCommitE = devopsGitlabCommitRepository.baseQueryByShaAndRef(commitDTO.getId(), ref);

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
                    devopsGitlabCommitRepository.baseCreate(devopsGitlabCommitE);
                }
            });
        } else {
            //直接从一个分支切出来另外一个分支，没有commits记录
            DevopsGitlabCommitE devopsGitlabCommitE = devopsGitlabCommitRepository.baseQueryByShaAndRef(pushWebHookDTO.getCheckoutSha(), ref);
            if (devopsGitlabCommitE == null) {
                CommitDTO commitDTO = devopsGitRepository.getCommit(TypeUtil.objToInteger(applicationE.getGitlabProjectE().getId()), pushWebHookDTO.getCheckoutSha(), ADMIN);
                devopsGitlabCommitE = new DevopsGitlabCommitE();
                devopsGitlabCommitE.setAppId(applicationE.getId());
                devopsGitlabCommitE.setCommitContent(commitDTO.getMessage());
                devopsGitlabCommitE.setCommitSha(commitDTO.getId());
                devopsGitlabCommitE.setRef(ref);
                devopsGitlabCommitE.setUrl(commitDTO.getUrl());
                if ("root".equals(commitDTO.getAuthorName())) {
                    devopsGitlabCommitE.setUserId(1L);
                } else {
                    UserE userE = iamRepository.queryByEmail(applicationE.getProjectE().getId(),
                            commitDTO.getAuthorEmail());
                    if (userE != null) {
                        devopsGitlabCommitE.setUserId(userE.getId());
                    }
                }
                devopsGitlabCommitE.setCommitDate(commitDTO.getCommittedDate());
                devopsGitlabCommitRepository.baseCreate(devopsGitlabCommitE);
            }
        }

    }

    @Override
    public DevopsGitlabCommitVO getCommits(Long projectId, String appIds, Date startDate, Date endDate) {

        List<Long> appIdsMap = gson.fromJson(appIds, new TypeToken<List<Long>>() {
        }.getType());
        if (appIdsMap.isEmpty()) {
            return new DevopsGitlabCommitVO();
        }

        // 查询应用列表下所有commit记录
        List<DevopsGitlabCommitE> devopsGitlabCommitES = devopsGitlabCommitRepository
                .baseListByOptions(projectId, appIdsMap, startDate, endDate);
        if (devopsGitlabCommitES.isEmpty()) {
            return new DevopsGitlabCommitVO();
        }

        // 获得去重后的所有用户信息
        Map<Long, UserE> userMap = getUserDOMap(devopsGitlabCommitES);

        // 获取用户分别的commit
        List<CommitFormUserDTO> commitFormUserDTOS = getCommitFormUserDTOList(devopsGitlabCommitES, userMap);

        // 获取总的commit(将所有用户的commit_date放入一个数组)，按照时间先后排序
        List<Date> totalCommitsDate = getTotalDates(commitFormUserDTOS);
        Collections.sort(totalCommitsDate);

        return new DevopsGitlabCommitVO(commitFormUserDTOS, totalCommitsDate);
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
                .baseListByOptions(projectId, appIdsMap, startDate, endDate);
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
        return devopsGitlabCommitRepository.basePageByOptions(projectId, appId, pageRequest, userMap, startDate, endDate);
    }

    private List<Date> getTotalDates(List<CommitFormUserDTO> commitFormUserDTOS) {
        List<Date> totalCommitsDate = new ArrayList<>();
        commitFormUserDTOS.forEach(e -> totalCommitsDate.addAll(e.getCommitDates()));
        return totalCommitsDate;
    }

    @Override
    public DevopsGitlabCommitDTO baseCreate(DevopsGitlabCommitDTO devopsGitlabCommitDTO) {
        if (!checkExist(devopsGitlabCommitDTO)) {
            if (devopsGitlabCommitMapper.insert(devopsGitlabCommitDTO) != 1) {
                throw new CommonException("error.gitlab.commit.create");
            }
        }
        return devopsGitlabCommitDTO;
    }

    @Override
    public DevopsGitlabCommitDTO baseQueryByShaAndRef(String sha, String ref) {
        DevopsGitlabCommitDTO devopsGitlabCommitDTO = new DevopsGitlabCommitDTO();
        devopsGitlabCommitDTO.setCommitSha(sha);
        devopsGitlabCommitDTO.setRef(ref);
        return devopsGitlabCommitMapper.selectOne(devopsGitlabCommitDTO);
    }

    @Override
    public List<DevopsGitlabCommitDTO> baseListByOptions(Long projectId, List<Long> appIds, Date startDate, Date endDate) {
        List<DevopsGitlabCommitDTO> devopsGitlabCommitDOList = devopsGitlabCommitMapper
                .listCommits(projectId, appIds, new java.sql.Date(startDate.getTime()), new java.sql.Date(endDate.getTime()));
        if (devopsGitlabCommitDOList == null || devopsGitlabCommitDOList.isEmpty()) {
            return new ArrayList<>();
        }
        return devopsGitlabCommitDOList;
    }

    @Override
    public PageInfo<CommitFormRecordDTO> basePageByOptions(Long projectId, List<Long> appId,
                                                           PageRequest pageRequest, Map<Long, UserE> userMap,
                                                           Date startDate, Date endDate) {
        List<CommitFormRecordDTO> commitFormRecordDTOList = new ArrayList<>();

        PageInfo<DevopsGitlabCommitDTO> devopsGitlabCommitDOPage = PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(),
                PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(
                () -> devopsGitlabCommitMapper.listCommits(projectId, appId, new java.sql.Date(startDate.getTime()), new java.sql.Date(endDate.getTime())));

        devopsGitlabCommitDOPage.getList().forEach(e -> {
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
        PageInfo<CommitFormRecordDTO> commitFormRecordDTOPagee = new PageInfo<>();
        BeanUtils.copyProperties(devopsGitlabCommitDOPage, commitFormRecordDTOPagee);
        commitFormRecordDTOPagee.setList(commitFormRecordDTOList);

        return commitFormRecordDTOPagee;
    }

    @Override
    public void baseUpdate(DevopsGitlabCommitDTO devopsGitlabCommitDTO) {
        DevopsGitlabCommitDTO oldDevopsGitlabCommitDO = devopsGitlabCommitMapper.selectByPrimaryKey(devopsGitlabCommitDTO.getId());
        DevopsGitlabCommitDTO newDevopsGitlabCommitDO = ConvertHelper.convert(devopsGitlabCommitDTO, DevopsGitlabCommitDTO.class);
        newDevopsGitlabCommitDO.setObjectVersionNumber(oldDevopsGitlabCommitDO.getObjectVersionNumber());
        if (devopsGitlabCommitMapper.updateByPrimaryKeySelective(newDevopsGitlabCommitDO) != 1) {
            throw new CommonException("error.gitlab.commit.update");
        }
    }

    @Override
    public List<DevopsGitlabCommitDTO> baseListByAppIdAndBranch(Long appId, String branch, Date startDate) {
        return devopsGitlabCommitMapper.queryByAppIdAndBranch(appId, branch, startDate == null ? null : new java.sql.Date(startDate.getTime()));
    }

    private boolean checkExist(DevopsGitlabCommitDTO devopsGitlabCommitDTO) {
        devopsGitlabCommitDTO.setCommitSha(devopsGitlabCommitDTO.getCommitSha());
        devopsGitlabCommitDTO.setRef(devopsGitlabCommitDTO.getRef());
        if (devopsGitlabCommitMapper.selectOne(devopsGitlabCommitDTO) != null) {
            return true;
        }
        return false;
    }
}
