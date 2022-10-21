package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.ExceptionConstants.BranchCode.DEVOPS_BRANCH_EXIST;
import static io.choerodon.devops.infra.constant.ExceptionConstants.PublicCode.DEVOPS_FIELD_NOT_SUPPORTED_FOR_SORT;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final String DEVOPS_QUERY_BRANCH_BY_NAME = "devops.query.branch.by.name";
    private static final String DEVOPS_BRANCH_UPDATE = "devops.branch.update";
    private static final String DEVOPS_BRANCH_DELETE = "devops.branch.delete";

    @Autowired
    private DevopsBranchMapper devopsBranchMapper;

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
    public DevopsBranchDTO baseQueryByAppAndBranchIdWithIssueId(Long appServiceId, Long branchId) {
        return devopsBranchMapper.queryByAppAndBranchIdWithIssueId(appServiceId, branchId);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBranchIssue(Long projectId, AppServiceDTO appServiceDTO, DevopsBranchDTO devopsBranchDTO, boolean onlyInsert) {
        DevopsBranchDTO oldDevopsBranchDTO = devopsBranchMapper
                .queryByAppAndBranchNameWithIssueIds(devopsBranchDTO.getAppServiceId(), devopsBranchDTO.getBranchName());

        if (oldDevopsBranchDTO == null) {
            throw new CommonException(DEVOPS_QUERY_BRANCH_BY_NAME);
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
                // 根据分支id以及分支关联的issueIds移除所有分支与敏捷问题关联关系
                devopsIssueRelMapper.batchDeleteByBranchIdAndIssueIds(branchId, issueIdsToDelete);
                // 根据分支id以及分支关联的issueIds移除所有有关的commit和敏捷问题关联关系
                devopsIssueRelMapper.batchDeleteCommitRelationByBranchIdAndIssueIds(branchId, issueIdsToDelete);
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
            throw new CommonException(DEVOPS_BRANCH_EXIST);
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
            throw new CommonException(DEVOPS_BRANCH_UPDATE);
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
                            throw new CommonException(DEVOPS_FIELD_NOT_SUPPORTED_FOR_SORT, t.getProperty());
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
            if (devopsBranchMapper.delete(deleteCondition) != 1) {
                throw new CommonException(DEVOPS_BRANCH_DELETE);
            }
            // 在2021.7.5之前，删除分支会移除敏捷问题关联关系
            // 在此之后会保留该关系，是为了在gitlab或界面上删除分支后，敏捷照样能够获得分支和问题的关联关系
//            devopsIssueRelService.deleteRelationByObjectAndObjectId(DevopsIssueRelObjectTypeEnum.BRANCH.getValue(), devopsBranchDTO.getId());
        } else {
            LOGGER.info("Branch {} is not found in app service with id {}", branchName, appServiceId);
        }
    }

    @Override
    public void deleteAllBranch(Long appServiceId) {
        devopsBranchMapper.deleteByAppServiceId(appServiceId);
    }

    @Override
    public List<DevopsBranchDTO> listByCommitIs(List<Long> commitIds) {
        if (!CollectionUtils.isEmpty(commitIds)) {
            return devopsBranchMapper.listByCommitIds(commitIds);
        }
        return new ArrayList<>();
    }

    @Override
    public List<Long> listDeletedBranchIds(Set<Long> branchIds) {
        if (CollectionUtils.isEmpty(branchIds)) {
            return new ArrayList<>();
        }
        List<Long> existBranchIds = devopsBranchMapper.listExistBranchIds(branchIds);
        return branchIds.stream().filter(id -> !existBranchIds.contains(id)).collect(Collectors.toList());
    }

    @Override
    public List<DevopsBranchDTO> listByIds(List<Long> branchIds) {
        if (!CollectionUtils.isEmpty(branchIds)) {
            return devopsBranchMapper.listByIds(branchIds);
        }
        return new ArrayList<>();
    }

    @Override
    public Boolean checkIssueBranchRelExist(Long projectId, Long issueId) {
        return devopsIssueRelMapper.checkIssueBranchRelExist(DevopsIssueRelObjectTypeEnum.BRANCH.getValue(), projectId, issueId);
    }

    @Override
    public void copyIssueBranchRel(Long projectId, Long oldIssueId, Long newIssueId) {
        Set<DevopsIssueRelDTO> devopsIssueRelDTOS = devopsIssueRelService.listRelationByIssueIdAndObjectType(DevopsIssueRelObjectTypeEnum.BRANCH.getValue(), oldIssueId);
        devopsIssueRelDTOS.forEach(devopsIssueRelDTO -> devopsIssueRelService
                .addRelation(DevopsIssueRelObjectTypeEnum.BRANCH.getValue(),
                        devopsIssueRelDTO.getBranchId(),
                        devopsIssueRelDTO.getBranchId(),
                        devopsIssueRelDTO.getProjectId(),
                        devopsIssueRelDTO.getAppServiceCode(),
                        newIssueId));
    }
}
