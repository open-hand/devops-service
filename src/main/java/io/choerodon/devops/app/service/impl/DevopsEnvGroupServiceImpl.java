package io.choerodon.devops.app.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import io.choerodon.devops.api.validator.DevopsEnvGroupValidator;
import io.choerodon.devops.api.vo.DevopsEnvGroupVO;
import io.choerodon.devops.app.service.DevopsEnvGroupService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.infra.dto.DevopsEnvGroupDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.mapper.DevopsEnvGroupMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        devopsEnvGroupValidator.checkNameUnique(devopsEnvGroupVO.getId(), devopsEnvGroupVO.getName(), projectId);
        DevopsEnvGroupDTO devopsEnvGroupDTO = ConvertUtils.convertObject(devopsEnvGroupVO, DevopsEnvGroupDTO.class);
        devopsEnvGroupDTO.setProjectId(projectId);
        devopsEnvGroupDTO = baseUpdate(devopsEnvGroupDTO);
        return ConvertUtils.convertObject(devopsEnvGroupDTO, DevopsEnvGroupVO.class);
    }


    @Override
    public List<DevopsEnvGroupVO> listByProject(Long projectId) {
        return ConvertUtils.convertList(baseListByProjectId(projectId).stream()
                        .sorted(Comparator.comparing(DevopsEnvGroupDTO::getSequence)).collect(Collectors.toList()),
                DevopsEnvGroupVO.class);
    }

    @Override
    public Boolean checkName(String name, Long projectId) {
        return baseCheckUniqueInProject(name, projectId);
    }

    @Override
    public void delete(Long id) {
        DevopsEnvGroupDTO devopsEnvGroupDTO = baseQuery(id);
        baseDelete(id);
        //删除环境组，将原环境组内所有环境放到默认组内
        List<DevopsEnvironmentDTO> devopsEnvironmentDTOS = devopsEnvironmentService.baseListByProjectIdAndActive(devopsEnvGroupDTO.getProjectId(), true);

        List<DevopsEnvironmentDTO> deletes = devopsEnvironmentDTOS.stream().filter(devopsEnvironmentDTO -> id.equals(devopsEnvironmentDTO.getDevopsEnvGroupId())).collect(Collectors.toList());
        for (DevopsEnvironmentDTO devopsEnvironmentDTO : deletes) {
            devopsEnvironmentDTO.setDevopsEnvGroupId(null);
            devopsEnvironmentService.baseUpdate(devopsEnvironmentDTO);
        }
    }

    @Override
    public DevopsEnvGroupDTO baseCreate(DevopsEnvGroupDTO devopsEnvGroupDTO) {
        devopsEnvGroupDTO.setSequence(getMaxSequenceInProject(devopsEnvGroupDTO.getProjectId()) + 1);
        devopsEnvGroupMapper.insert(devopsEnvGroupDTO);
        return devopsEnvGroupDTO;
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

    private Long getMaxSequenceInProject(Long projectId) {
        DevopsEnvGroupDTO devopsEnvGroupDO = new DevopsEnvGroupDTO();
        devopsEnvGroupDO.setProjectId(projectId);
        List<DevopsEnvGroupDTO> devopsEnvGroupDOS = devopsEnvGroupMapper.select(devopsEnvGroupDO);
        return devopsEnvGroupDOS.stream().map(DevopsEnvGroupDTO::getSequence).max(Long::compareTo).orElse(0L);
    }
}
