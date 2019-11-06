package io.choerodon.devops.app.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsPvPermissionUpateVO;
import io.choerodon.devops.api.vo.DevopsPvVO;
import io.choerodon.devops.api.vo.ProjectReqVO;
import io.choerodon.devops.app.service.DevopsPvProPermissionService;
import io.choerodon.devops.app.service.DevopsPvServcie;
import io.choerodon.devops.infra.dto.DevopsPvDTO;
import io.choerodon.devops.infra.dto.DevopsPvProPermissionDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsPvMapper;
import io.choerodon.devops.infra.util.TypeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DevopsPvServiceImpl implements DevopsPvServcie {

    @Autowired
    DevopsPvMapper devopsPvMapper;

    @Autowired
    BaseServiceClientOperator baseServiceClientOperator;

    @Autowired
    DevopsPvProPermissionService devopsPvProPermissionService;

    @Override
    public PageInfo<DevopsPvVO> basePagePvByOptions(Boolean doPage, Pageable pageable, String params) {
        // search_param 根据确定的键值对查询
        // params 是遍历字段模糊查询
        Map<String, Object> searchParamMap = TypeUtil.castMapParams(params);
        return PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize())
                .doSelectPageInfo(() -> devopsPvMapper.listPvByOptions(
                        TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                        TypeUtil.cast(searchParamMap.get(TypeUtil.PARAMS))
                ));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePvById(Long pvId) {
        if(baseQueryById(pvId) == null){return;}

        devopsPvMapper.deleteByPrimaryKey(pvId);

        //级联删除权限表中的数据
        devopsPvProPermissionService.baseDeleteByPvId(pvId);
    }

    @Override
    public void createPv(DevopsPvDTO devopsPvDTO) {
        if (devopsPvMapper.insertSelective(devopsPvDTO) != 1){
            throw new CommonException("error.pv.create.error");
        }
    }

    @Override
    public void checkName(Long clusterId, String pvName) {
        DevopsPvDTO devopsPvDTO = new DevopsPvDTO();
        devopsPvDTO.setClusterId(clusterId);
        devopsPvDTO.setName(pvName);
        baseCheckPv(devopsPvDTO);
    }

    @Override
    public void baseCheckPv(DevopsPvDTO devopsPvDTO) {
        if (devopsPvMapper.selectOne(devopsPvDTO) != null){
            throw new CommonException("error.pv.name.exists");
        }
    }

    @Override
    @Transactional
    public void assignPermission(DevopsPvPermissionUpateVO update) {
        DevopsPvDTO devopsPvDTO = devopsPvMapper.selectByPrimaryKey(update.getPvId());

        if (devopsPvDTO.getSkipCheckProjectPermission()){
            // 原来对组织下所有项目公开,更新之后依然公开，则不做任何处理
            // 更新之后对特定项目公开则忽略之前的更新权限表
            if (!update.getSkipCheckProjectPermission()){
                // 更新相关字段
                updateCheckPermission(update);

                //批量插入
                devopsPvProPermissionService.batchInsertIgnore(update.getPvId(), update.getProjectIds());
            }
        }else{
            // 原来不公开,现在设置公开，更新版本号，直接删除原来的权限表中的数据
            if(update.getSkipCheckProjectPermission()){
                // 先更新相关字段
                updateCheckPermission(update);

                //批量删除
                devopsPvProPermissionService.baseListByPvId(update.getPvId());
            }else{
                //原来不公开，现在也不公开,则根据ids批量插入
                devopsPvProPermissionService.batchInsertIgnore(update.getPvId(), update.getProjectIds());
            }

        }
    }

    @Override
    public void updateCheckPermission(DevopsPvPermissionUpateVO update){
        DevopsPvDTO devopsPvDTO = new DevopsPvDTO();
        devopsPvDTO.setId(update.getPvId());
        devopsPvDTO.setSkipCheckProjectPermission(update.getSkipCheckProjectPermission());
        devopsPvDTO.setObjectVersionNumber(update.getObjectVersionNumber());
        devopsPvMapper.updateByPrimaryKeySelective(devopsPvDTO);
    }

    @Override
    public void updatePv(DevopsPvDTO devopsPvDTO) {
        devopsPvMapper.updateByPrimaryKeySelective(devopsPvDTO);
    }

    @Override
    public DevopsPvVO queryById(Long pvId) {
        return devopsPvMapper.queryById(pvId);
    }

    @Override
    public DevopsPvDTO baseQueryById(Long pvId) {
        return devopsPvMapper.selectByPrimaryKey(pvId);
    }

    @Override
    public List<ProjectReqVO> listNonRelatedProjects(Long projectId, Long pvId) {
        DevopsPvDTO devopsPvDTO = baseQueryById(pvId);
        if (devopsPvDTO == null){
            throw new CommonException("error.pv.not.exists");
        }

        ProjectDTO iamProjectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(iamProjectDTO.getOrganizationId());
        List<ProjectDTO> projectDTOList = baseServiceClientOperator.listIamProjectByOrgId(organizationDTO.getId());

        //根据PvId查权限表中关联的projectId
        List<Long> permitted = devopsPvProPermissionService.baseListByPvId(pvId)
                .stream()
                .map(DevopsPvProPermissionDTO::getProjectId)
                .collect(Collectors.toList());

        //把组织下有权限的项目过滤掉再返回
        return projectDTOList.stream()
                .filter(i -> !permitted.contains(i.getId()))
                .map(i -> new ProjectReqVO(i.getId(), i.getName(), i.getCode(), null))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteRelatedProjectById(Long pvId, Long relatedProjectId) {
        DevopsPvProPermissionDTO devopsPvProPermissionDTO = new DevopsPvProPermissionDTO();
        devopsPvProPermissionDTO.setPvId(pvId);
        devopsPvProPermissionDTO.setProjectId(relatedProjectId);
        devopsPvProPermissionService.baseDeletePermission(devopsPvProPermissionDTO);
    }
}
