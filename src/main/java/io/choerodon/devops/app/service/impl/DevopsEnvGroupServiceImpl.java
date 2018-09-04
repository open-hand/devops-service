package io.choerodon.devops.app.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.dto.DevopsEnvGroupDTO;
import io.choerodon.devops.api.validator.DevopsEnvGroupValidator;
import io.choerodon.devops.app.service.DevopsEnvGroupService;
import io.choerodon.devops.domain.application.entity.DevopsEnvGroupE;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.repository.DevopsEnvGroupRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;

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

    @Override
    public DevopsEnvGroupDTO create(String name, Long projectId) {
        devopsEnvGroupValidator.checkNameUnique(null, name, projectId);
        DevopsEnvGroupE devopsEnvGroupE = new DevopsEnvGroupE();
        devopsEnvGroupE.setName(name);
        devopsEnvGroupE.initProject(projectId);
        devopsEnvGroupE = devopsEnvGroupRepository.create(devopsEnvGroupE);
        return ConvertHelper.convert(devopsEnvGroupE, DevopsEnvGroupDTO.class);
    }

    @Override
    public DevopsEnvGroupDTO update(DevopsEnvGroupDTO devopsEnvGroupDTO, Long projectId) {
        devopsEnvGroupValidator.checkNameUnique(devopsEnvGroupDTO.getId(), devopsEnvGroupDTO.getName(), projectId);
        DevopsEnvGroupE devopsEnvGroupE = ConvertHelper.convert(devopsEnvGroupDTO, DevopsEnvGroupE.class);
        devopsEnvGroupE.initProject(projectId);
        devopsEnvGroupRepository.update(devopsEnvGroupE);
        return ConvertHelper.convert(devopsEnvGroupE, DevopsEnvGroupDTO.class);
    }

    @Override
    public List<DevopsEnvGroupDTO> sort(Long projectId, List<Long> envGroupIds) {
        devopsEnvGroupRepository.sort(projectId, envGroupIds);
        return ConvertHelper.convertList(
                devopsEnvGroupRepository.listByProjectId(projectId).stream()
                        .sorted(Comparator.comparing(DevopsEnvGroupE::getSequence)).collect(Collectors.toList()),
                DevopsEnvGroupDTO.class);
    }

    @Override
    public List<DevopsEnvGroupDTO> listByProject(Long projectId) {
        return ConvertHelper.convertList(
                devopsEnvGroupRepository.listByProjectId(projectId).stream()
                        .sorted(Comparator.comparing(DevopsEnvGroupE::getSequence)).collect(Collectors.toList()),
                DevopsEnvGroupDTO.class);
    }

    @Override
    public Boolean checkUniqueInProject(String name, Long projectId) {
        return devopsEnvGroupRepository.checkUniqueInProject(name, projectId);
    }

    @Override
    public void delete(Long id) {
        DevopsEnvGroupE devopsEnvGroupE = devopsEnvGroupRepository.query(id);
        devopsEnvGroupRepository.delete(id);
        List<DevopsEnvironmentE> devopsEnvironmentES = devopsEnvironmentRepository.queryByprojectAndActive(devopsEnvGroupE.getProjectE().getId(), true);

        LongSummaryStatistics stats = devopsEnvironmentES
                .parallelStream()
                .filter(devopsEnvironmentE -> devopsEnvironmentE.getDevopsEnvGroupId() == null)
                .mapToLong(DevopsEnvironmentE::getSequence)
                .summaryStatistics();
        Long sequence = stats.getMax() + 1;
        List<DevopsEnvironmentE> deletes = devopsEnvironmentES.stream().filter(devopsEnvironmentE -> id.equals(devopsEnvironmentE.getDevopsEnvGroupId())).collect(Collectors.toList());
        for (DevopsEnvironmentE devopsEnvironmentE : deletes) {
            devopsEnvironmentE.setDevopsEnvGroupId(null);
            devopsEnvironmentE.setSequence(sequence);
            devopsEnvironmentRepository.update(devopsEnvironmentE);
            sequence++;
        }
    }
}
