package io.choerodon.devops.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.validator.DevopsEnvGroupValidator;
import io.choerodon.devops.api.vo.DevopsEnvGroupVO;
import io.choerodon.devops.app.service.DevopsEnvGroupService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.DevopsEnvGroupDTO;
import io.choerodon.devops.infra.mapper.DevopsEnvGroupMapper;
import io.choerodon.devops.infra.util.CommonExAssertUtil;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.MapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Creator: Runge
 * Date: 2018/9/4
 * Time: 14:56
 * Description:
 */
@Service
public class DevopsEnvGroupServiceImpl implements DevopsEnvGroupService {

    @Autowired
    private DevopsEnvGroupValidator devopsEnvGroupValidator;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsEnvGroupMapper devopsEnvGroupMapper;

    @Override
    public DevopsEnvGroupVO create(String name, Long projectId) {
        devopsEnvGroupValidator.checkNameUnique(null, name, projectId);
        DevopsEnvGroupDTO devopsEnvGroupDTO = new DevopsEnvGroupDTO();
        devopsEnvGroupDTO.setName(name);
        devopsEnvGroupDTO.setProjectId(projectId);
        devopsEnvGroupDTO = baseCreate(devopsEnvGroupDTO);
        return ConvertUtils.convertObject(devopsEnvGroupDTO, DevopsEnvGroupVO.class);
    }

    @Override
    public DevopsEnvGroupVO update(DevopsEnvGroupVO devopsEnvGroupVO, Long projectId) {
        DevopsEnvGroupDTO devopsEnvGroupDTOToCheck = devopsEnvGroupMapper.selectByPrimaryKey(devopsEnvGroupVO.getId());
        CommonExAssertUtil.assertTrue(projectId.equals(devopsEnvGroupDTOToCheck.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        devopsEnvGroupValidator.checkNameUnique(devopsEnvGroupVO.getId(), devopsEnvGroupVO.getName(), projectId);
        DevopsEnvGroupDTO devopsEnvGroupDTO = ConvertUtils.convertObject(devopsEnvGroupVO, DevopsEnvGroupDTO.class);
        devopsEnvGroupDTO.setProjectId(projectId);
        devopsEnvGroupDTO = baseUpdate(devopsEnvGroupDTO);
        return ConvertUtils.convertObject(devopsEnvGroupDTO, DevopsEnvGroupVO.class);
    }


    @Override
    public List<DevopsEnvGroupVO> listByProject(Long projectId) {
        return ConvertUtils.convertList(baseListByProjectId(projectId),
                DevopsEnvGroupVO.class);
    }

    @Override
    public Boolean checkName(String name, Long projectId, Long groupId) {
        return baseCheckUniqueInProject(groupId, name, projectId);
    }

    @Override
    public void checkGroupIdInProject(@Nullable Long groupId, @NotNull Long projectId) {
        if (groupId != null) {
            DevopsEnvGroupDTO devopsEnvGroupDTO = new DevopsEnvGroupDTO();
            devopsEnvGroupDTO.setId(groupId);
            devopsEnvGroupDTO.setProjectId(projectId);
            if (devopsEnvGroupMapper.selectOne(devopsEnvGroupDTO) == null) {
                throw new CommonException("error.group.id.project.id.not.match", projectId, groupId);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void delete(Long projectId, Long id) {
        DevopsEnvGroupDTO devopsEnvGroupDTO = baseQuery(id);

        if (devopsEnvGroupDTO == null) {
            return;
        }

        CommonExAssertUtil.assertTrue(projectId.equals(devopsEnvGroupDTO.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        baseDelete(id);
        //删除环境组，将原环境组内所有环境的env_group_id置为null
        devopsEnvironmentService.updateDevopsEnvGroupIdNullByProjectIdAndGroupId(devopsEnvGroupDTO.getProjectId(), id);
    }

    @Override
    public DevopsEnvGroupDTO baseCreate(DevopsEnvGroupDTO devopsEnvGroupDTO) {
        return MapperUtil.resultJudgedInsert(devopsEnvGroupMapper, devopsEnvGroupDTO, "error.insert.env.group");
    }

    @Override
    public DevopsEnvGroupDTO baseUpdate(DevopsEnvGroupDTO devopsEnvGroupDTO) {
        devopsEnvGroupDTO.setObjectVersionNumber(devopsEnvGroupMapper.selectByPrimaryKey(devopsEnvGroupDTO.getId()).getObjectVersionNumber());
        devopsEnvGroupMapper.updateByPrimaryKeySelective(devopsEnvGroupDTO);
        return devopsEnvGroupDTO;
    }

    @Override
    public List<DevopsEnvGroupDTO> baseListByProjectId(Long projectId) {
        DevopsEnvGroupDTO devopsEnvGroupDTO = new DevopsEnvGroupDTO();
        devopsEnvGroupDTO.setProjectId(projectId);
        return devopsEnvGroupMapper.select(devopsEnvGroupDTO);
    }

    @Override
    public DevopsEnvGroupDTO baseQuery(Long id) {
        return devopsEnvGroupMapper.selectByPrimaryKey(id);
    }


    @Override
    public Boolean baseCheckUniqueInProject(Long id, String name, Long projectId) {
        DevopsEnvGroupDTO devopsEnvGroupDTO = new DevopsEnvGroupDTO();
        devopsEnvGroupDTO.setName(name);
        devopsEnvGroupDTO.setProjectId(projectId);
        List<DevopsEnvGroupDTO> devopsEnvGroupDOS = devopsEnvGroupMapper.select(devopsEnvGroupDTO);
        boolean updateCheck = false;
        if (id != null) {
            updateCheck = devopsEnvGroupDOS.size() == 1 && id.equals(devopsEnvGroupDOS.get(0).getId());
        }
        return devopsEnvGroupDOS.isEmpty() || updateCheck;
    }

    @Override
    public Boolean baseCheckUniqueInProject(String name, Long projectId) {
        return baseCheckUniqueInProject(null, name, projectId);
    }

    @Override
    public void baseDelete(Long id) {
        devopsEnvGroupMapper.deleteByPrimaryKey(id);
    }

    @Override
    public Boolean checkExist(Long id) {
        return devopsEnvGroupMapper.selectByPrimaryKey(id) != null;
    }
}
