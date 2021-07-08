package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.hzero.mybatis.BatchInsertHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsBranchService;
import io.choerodon.devops.app.service.DevopsIssueRelService;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsBranchDTO;
import io.choerodon.devops.infra.dto.DevopsIssueRelDTO;
import io.choerodon.devops.infra.enums.DevopsIssueRelObjectTypeEnum;
import io.choerodon.devops.infra.mapper.DevopsBranchMapper;
import io.choerodon.devops.infra.mapper.DevopsGitlabCommitMapper;
import io.choerodon.devops.infra.mapper.DevopsIssueRelMapper;
import io.choerodon.devops.infra.util.LogUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;


/**
 * Created by Sheep on 2019/7/11.
 */

@Service
public class DevopsBranchServiceImpl implements DevopsBranchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsBranchServiceImpl.class);

    @Autowired
    private DevopsBranchMapper devopsBranchMapper;

    @Autowired
    private DevopsGitlabCommitMapper devopsGitlabCommitMapper;

    @Autowired
    @Qualifier("devopsIssueRelBatchInsertHelper")
    private BatchInsertHelper<DevopsIssueRelDTO> batchInsertHelper;

    @Autowired
    private DevopsIssueRelService devopsIssueRelService;

    @Autowired
    private DevopsIssueRelMapper devopsIssueRelMapper;

    @Override
    public List<DevopsBranchDTO> baseListDevopsBranchesByIssueId(Long issueId) {
        return devopsBranchMapper.listByIssueIdAndOrderByProjectId(issueId);
    }

    @Override
    public DevopsBranchDTO baseQueryByAppAndBranchName(Long appServiceId, String branchName) {
        return devopsBranchMapper
                .queryByAppAndBranchName(appServiceId, branchName);
    }

    @Override
    public DevopsBranchDTO baseQueryByAppAndBranchNameWithIssueIds(Long appServiceId, String branchName) {
        return devopsBranchMapper.queryByAppAndBranchNameWithIssueIds(appServiceId, branchName);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBranchIssue(Long projectId, AppServiceDTO appServiceDTO, DevopsBranchDTO devopsBranchDTO, boolean onlyInsert) {
        DevopsBranchDTO oldDevopsBranchDTO = devopsBranchMapper
                .queryByAppAndBranchNameWithIssueIds(devopsBranchDTO.getAppServiceId(), devopsBranchDTO.getBranchName());

        if (oldDevopsBranchDTO == null) {
            throw new CommonException("error.query.branch.by.name");
        }

        Long branchId = oldDevopsBranchDTO.getId();

        List<Long> oldIssueIds = oldDevopsBranchDTO.getIssueIds() == null ? new ArrayList<>() : oldDevopsBranchDTO.getIssueIds();
        List<Long> newIssueIds = devopsBranchDTO.getIssueIds() == null ? new ArrayList<>() : devopsBranchDTO.getIssueIds();

        // 对比获得新增的issue关联关系
        List<Long> issueIdsToAdd = newIssueIds
                .stream()
                .filter(i -> !oldIssueIds.contains(i))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(issueIdsToAdd)) {
            devopsIssueRelService.addRelation(DevopsIssueRelObjectTypeEnum.BRANCH.getValue(), branchId, branchId, projectId, appServiceDTO.getCode(), issueIdsToAdd);
        }

        // 如果不仅是插入操作，那么还需要更新被删除的关联关系
        if (!onlyInsert) {
            // 对比获得需要删除的issueId
            List<Long> issueIdsToDelete = oldIssueIds
                    .stream()
                    .filter(i -> !newIssueIds.contains(i))
                    .collect(Collectors.toList());

            if (!CollectionUtils.isEmpty(issueIdsToDelete)) {
                devopsIssueRelMapper.batchDeleteByBranchIdAndIssueIds(branchId, issueIdsToDelete);
            }
        }
    }

    @Override
    public void baseUpdateBranchLastCommit(DevopsBranchDTO devopsBranchDTO) {
        DevopsBranchDTO oldDevopsBranchDTO = devopsBranchMapper
                .queryByAppAndBranchName(devopsBranchDTO.getAppServiceId(), devopsBranchDTO.getBranchName());
        oldDevopsBranchDTO.setLastCommit(devopsBranchDTO.getLastCommit());
        oldDevopsBranchDTO.setLastCommitDate(devopsBranchDTO.getLastCommitDate());
        oldDevopsBranchDTO.setLastCommitMsg(LogUtil.cutOutString(devopsBranchDTO.getLastCommitMsg(), MiscConstants.DEVOPS_BRANCH_LAST_COMMIT_MESSAGE_MAX_LENGTH));
        oldDevopsBranchDTO.setLastCommitUser(devopsBranchDTO.getLastCommitUser());
        devopsBranchMapper.updateByPrimaryKey(oldDevopsBranchDTO);
    }

    @Override
    public DevopsBranchDTO baseCreate(DevopsBranchDTO devopsBranchDTO) {
        DevopsBranchDTO exist = new DevopsBranchDTO();
        exist.setAppServiceId(devopsBranchDTO.getAppServiceId());
        exist.setBranchName(devopsBranchDTO.getBranchName());
        if (devopsBranchMapper.selectOne(exist) != null) {
            throw new CommonException("error.branch.exist");
        }
        devopsBranchMapper.insert(devopsBranchDTO);
        return devopsBranchDTO;
    }

    @Override
    public DevopsBranchDTO baseQuery(Long devopsBranchId) {
        return devopsBranchMapper.selectByPrimaryKey(devopsBranchId);
    }


    @Override
    public void baseUpdateBranch(DevopsBranchDTO devopsBranchDTO) {
        devopsBranchDTO.setObjectVersionNumber(devopsBranchMapper.selectByPrimaryKey(devopsBranchDTO.getId()).getObjectVersionNumber());
        if (devopsBranchMapper.updateByPrimaryKey(devopsBranchDTO) != 1) {
            throw new CommonException("error.branch.update");
        }
    }


    @Override
    public Page<DevopsBranchDTO> basePageBranch(Long appServiceId, PageRequest pageable, String params, Long issueId) {
        Map<String, Object> maps = TypeUtil.castMapParams(params);
        Sort sort = pageable.getSort();
        String sortResult = "";
        if (sort != null) {
            sortResult = Lists.newArrayList(pageable.getSort().iterator()).stream()
                    .map(t -> {
                        String property = t.getProperty();
                        if ("branchName".equals(property)) {
                            property = "db.branch_name";
                        } else if ("creation_date".equals(property)) {
                            property = "db.creation_date";
                        } else {
                            throw new CommonException("error.field.not.supported.for.sort", t.getProperty());
                        }
                        return property + " " + t.getDirection();
                    })
                    .collect(Collectors.joining(","));
        }
        String sortString = sortResult;
        return PageHelper.doPage(pageable,
                () -> devopsBranchMapper.list(appServiceId,
                        sortString,
                        TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)),
                        TypeUtil.cast(maps.get(TypeUtil.PARAMS)),
                        issueId));
    }


    @Override
    public void baseDelete(Long appServiceId, String branchName) {
        Objects.requireNonNull(appServiceId);
        Objects.requireNonNull(branchName);
        DevopsBranchDTO devopsBranchDTO = devopsBranchMapper.queryByAppAndBranchName(appServiceId, branchName);
        if (devopsBranchDTO != null) {
            // 构建删除条件
            DevopsBranchDTO deleteCondition = new DevopsBranchDTO();
            deleteCondition.setAppServiceId(appServiceId);
            deleteCondition.setBranchName(branchName);
            int resultCount;
            if ((resultCount = devopsBranchMapper.delete(deleteCondition)) != 1) {
                throw new CommonException("Failed to delete branch due to result count " + resultCount);
            }
            // 删除敏捷问题关联关系
            devopsIssueRelService.deleteRelationByObjectAndObjectId(DevopsIssueRelObjectTypeEnum.BRANCH.getValue(), devopsBranchDTO.getId());
        } else {
            LOGGER.info("Branch {} is not found in app service with id {}", branchName, appServiceId);
        }
    }

    @Override
    public void deleteAllBranch(Long appServiceId) {
        devopsBranchMapper.deleteByAppServiceId(appServiceId);
    }

    @Override
    public void fixIssueId() {
        int totalCount = devopsBranchMapper.countBranchBoundWithIssue();
        int pageNumber = 0;
        int pageSize = 100;
        int totalPage = (totalCount + pageSize - 1) / pageSize;
        do {
            PageRequest pageRequest = new PageRequest();
            pageRequest.setPage(pageNumber);
            pageRequest.setSize(pageSize);
            Page<DevopsBranchDTO> result = PageHelper.doPage(pageRequest, () -> devopsBranchMapper.listBranchBoundWithIssue());
            if (!CollectionUtils.isEmpty(result.getContent())) {
                List<DevopsIssueRelDTO> devopsIssueRelDTOList = result.getContent().stream().map(b -> {
                    DevopsIssueRelDTO devopsIssueRelDTO = new DevopsIssueRelDTO();
                    devopsIssueRelDTO.setIssueId(b.getIssueId());
                    devopsIssueRelDTO.setObject(DevopsIssueRelObjectTypeEnum.BRANCH.getValue());
                    devopsIssueRelDTO.setObjectId(b.getId());
                    return devopsIssueRelDTO;
                }).collect(Collectors.toList());
                batchInsertHelper.batchInsert(devopsIssueRelDTOList);
            }
            pageNumber++;
        } while (pageNumber < totalPage);
    }

    @Override
    public List<DevopsBranchDTO> listByCommitIs(List<Long> commitIds) {
        if (!CollectionUtils.isEmpty(commitIds)) {
            return devopsBranchMapper.listByCommitIds(commitIds);
        }
        return new ArrayList<>();
    }

    @Override
    public List<DevopsBranchDTO> listByIds(List<Long> branchIds) {
        if (!CollectionUtils.isEmpty(branchIds)) {
            return devopsBranchMapper.listByIds(branchIds);
        }
        return new ArrayList<>();
    }
}
