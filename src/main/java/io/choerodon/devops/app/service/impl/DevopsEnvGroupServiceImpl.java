package io.choerodon.devops.app.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.validator.DevopsEnvGroupValidator;
import io.choerodon.devops.api.vo.DevopsEnvGroupVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvGroupE;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvironmentE;
import io.choerodon.devops.app.service.DevopsEnvGroupService;
import io.choerodon.devops.domain.application.repository.DevopsEnvGroupRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.infra.dto.DevopsEnvGroupDTO;
import io.choerodon.devops.infra.mapper.DevopsEnvGroupMapper;

/**
 * Creator: Runge
 * Date: 2018/9/4
 * Time: 14:56
 * Description:
 */
@Service
public class DevopsEnvGroupServiceImpl implements DevopsEnvGroupService {

    @Autowired
    private DevopsEnvGroupRepository devopsEnvGroupRepository;
    @Autowired
    private DevopsEnvGroupValidator devopsEnvGroupValidator;
    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository;
    @Autowired
    private DevopsEnvGroupMapper devopsEnvGroupMapper;

    @Override
    public DevopsEnvGroupVO create(String name, Long projectId) {
        devopsEnvGroupValidator.checkNameUnique(null, name, projectId);
        DevopsEnvGroupE devopsEnvGroupE = new DevopsEnvGroupE();
        devopsEnvGroupE.setName(name);
        devopsEnvGroupE.initProject(projectId);
        devopsEnvGroupE = devopsEnvGroupRepository.baseCreate(devopsEnvGroupE);
        return ConvertHelper.convert(devopsEnvGroupE, DevopsEnvGroupVO.class);
    }

    @Override
    public DevopsEnvGroupVO update(DevopsEnvGroupVO devopsEnvGroupDTO, Long projectId) {
        devopsEnvGroupValidator.checkNameUnique(devopsEnvGroupDTO.getId(), devopsEnvGroupDTO.getName(), projectId);
        DevopsEnvGroupE devopsEnvGroupE = ConvertHelper.convert(devopsEnvGroupDTO, DevopsEnvGroupE.class);
        devopsEnvGroupE.initProject(projectId);
        devopsEnvGroupE = devopsEnvGroupRepository.baseUpdate(devopsEnvGroupE);
        return ConvertHelper.convert(devopsEnvGroupE, DevopsEnvGroupVO.class);
    }


    @Override
    public List<DevopsEnvGroupVO> listByProject(Long projectId) {
        return ConvertHelper.convertList(
                devopsEnvGroupRepository.baseListByProjectId(projectId).stream()
                        .sorted(Comparator.comparing(DevopsEnvGroupE::getSequence)).collect(Collectors.toList()),
                DevopsEnvGroupVO.class);
    }

    @Override
    public Boolean checkUniqueInProject(String name, Long projectId) {
        return devopsEnvGroupRepository.baseCheckUniqueInProject(name, projectId);
    }

    @Override
    public void delete(Long id) {
        DevopsEnvGroupE devopsEnvGroupE = devopsEnvGroupRepository.baseQuery(id);
        devopsEnvGroupRepository.baseDelete(id);
        //删除环境组，将原环境组内所有环境放到默认组内，环境sequence在默认组环境递增
        List<DevopsEnvironmentE> devopsEnvironmentES = devopsEnvironmentRepository.baseListByProjectIdAndActive(devopsEnvGroupE.getProjectE().getId(), true);

        List<DevopsEnvironmentE> defaultDevopsEnvironmentES = devopsEnvironmentES.stream().filter(devopsEnvironmentE -> devopsEnvironmentE.getDevopsEnvGroupId() == null).collect(Collectors.toList());
        Long sequence = 1L;
        if (!defaultDevopsEnvironmentES.isEmpty()) {
            LongSummaryStatistics stats = devopsEnvironmentES
                    .stream()
                    .mapToLong(DevopsEnvironmentE::getSequence)
                    .summaryStatistics();
            sequence = stats.getMax() + 1;
        }
        List<DevopsEnvironmentE> deletes = devopsEnvironmentES.stream().filter(devopsEnvironmentE -> id.equals(devopsEnvironmentE.getDevopsEnvGroupId())).collect(Collectors.toList());
        for (DevopsEnvironmentE devopsEnvironmentE : deletes) {
            devopsEnvironmentE.setDevopsEnvGroupId(null);
            devopsEnvironmentE.setSequence(sequence);
            devopsEnvironmentRepository.baseUpdate(devopsEnvironmentE);
            sequence++;
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
