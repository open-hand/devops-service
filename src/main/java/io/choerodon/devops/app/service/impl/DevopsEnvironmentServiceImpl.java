package io.choerodon.devops.app.service.impl;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.DevopsEnviromentDTO;
import io.choerodon.devops.api.dto.DevopsEnviromentRepDTO;
import io.choerodon.devops.api.dto.DevopsEnvironmentUpdateDTO;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.factory.DevopsEnvironmentFactory;
import io.choerodon.devops.domain.application.repository.ApplicationInstanceRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.domain.application.repository.DevopsServiceRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.infra.common.util.FileUtil;
import io.choerodon.devops.infra.common.util.GenerateUUID;
import io.choerodon.websocket.session.EnvListener;

/**
 * Created by younger on 2018/4/9.
 */
@Service
public class DevopsEnvironmentServiceImpl implements DevopsEnvironmentService {

    @Value("${agent.version}")
    private String agentVersion;

    @Value("${agent.serviceUrl}")
    private String agentServiceUrl;

    private IamRepository iamRepository;
    private DevopsEnvironmentRepository devopsEnviromentRepository;
    private EnvListener envListener;
    private DevopsServiceRepository devopsServiceRepository;
    private ApplicationInstanceRepository applicationInstanceRepository;

    /**
     * 构造方法
     */
    public DevopsEnvironmentServiceImpl(IamRepository iamRepository,
                                        DevopsEnvironmentRepository devopsEnviromentRepository,
                                        EnvListener envListener,
                                        DevopsServiceRepository devopsServiceRepository,
                                        ApplicationInstanceRepository applicationInstanceRepository) {
        this.iamRepository = iamRepository;
        this.devopsEnviromentRepository = devopsEnviromentRepository;
        this.envListener = envListener;
        this.devopsServiceRepository = devopsServiceRepository;
        this.applicationInstanceRepository = applicationInstanceRepository;
    }

    @Override
    public String create(Long projectId, DevopsEnviromentDTO devopsEnviromentDTO) {
        DevopsEnvironmentE devopsEnvironmentE = ConvertHelper.convert(devopsEnviromentDTO, DevopsEnvironmentE.class);
        devopsEnviromentRepository.checkCode(devopsEnvironmentE);
        devopsEnviromentRepository.checkName(devopsEnvironmentE);
        devopsEnvironmentE.initActive(true);
        devopsEnvironmentE.initConnect(false);
        devopsEnvironmentE.initToken(GenerateUUID.generateUUID());
        devopsEnvironmentE.initProjectE(projectId);
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        devopsEnvironmentE.initNamespace(organization.getCode(), projectE.getCode());
        List<DevopsEnvironmentE> devopsEnvironmentES = devopsEnviromentRepository
                .queryByprojectAndActive(projectId, true);
        devopsEnvironmentE.initSequence(devopsEnvironmentES);
        devopsEnviromentRepository.create(devopsEnvironmentE);
        InputStream inputStream = this.getClass().getResourceAsStream("/shell/environment.sh");
        Map<String, String> params = new HashMap<>();
        params.put("{NAMESPACE}", devopsEnvironmentE.getNamespace());
        params.put("{VERSION}", agentVersion);
        params.put("{SERVICEURL}", agentServiceUrl);
        params.put("{TOKEN}", devopsEnvironmentE.getToken());
        return FileUtil.replaceReturnString(inputStream, params);
    }

    @Override
    public List<DevopsEnviromentRepDTO> listByProjectIdAndActive(Long projectId, Boolean active) {
        Set<String> namespaces = envListener.connectedEnv();
        List<DevopsEnvironmentE> devopsEnvironmentES = devopsEnviromentRepository
                .queryByprojectAndActive(projectId, active).parallelStream()
                .sorted(Comparator.comparing(DevopsEnvironmentE::getSequence))
                .collect(Collectors.toList());
        for (String str : namespaces) {
            for (DevopsEnvironmentE devopsEnvironmentE : devopsEnvironmentES) {
                if (str.equals(devopsEnvironmentE.getNamespace())) {
                    devopsEnvironmentE.initConnect(true);
                }
            }
        }

        return ConvertHelper.convertList(devopsEnvironmentES, DevopsEnviromentRepDTO.class);
    }

    @Override
    public List<DevopsEnviromentRepDTO> listDeployed(Long projectId) {
        List<Long> envList = devopsServiceRepository.selectDeployedEnv();
        return listByProjectIdAndActive(projectId, true).stream().filter(t ->
                envList.contains(t.getId())).collect(Collectors.toList());
    }

    @Override
    public Boolean activeEnvironment(Long projectId, Long environmentId, Boolean active) {
        if (!active && applicationInstanceRepository.selectByEnvId(environmentId) > 0) {
            throw new CommonException("error.env.stop");
        }

        DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository.queryById(environmentId);
        devopsEnvironmentE.setActive(active);
        if (active) {
            devopsEnvironmentE.initSequence(devopsEnviromentRepository
                    .queryByprojectAndActive(projectId, active));
        } else {
            List<DevopsEnvironmentE> devopsEnvironmentES = devopsEnviromentRepository
                    .queryByprojectAndActive(projectId, true).parallelStream()
                    .sorted(Comparator.comparing(DevopsEnvironmentE::getSequence))
                    .collect(Collectors.toList());
            List<Long> environmentIds = devopsEnvironmentES
                    .stream()
                    .map(devopsEnvironmentE1
                            -> devopsEnvironmentE1.getId().longValue())
                    .collect(Collectors.toList());
            environmentIds.remove(environmentId);
            Long[] ids = new Long[environmentIds.size()];
            sort(environmentIds.toArray(ids));
        }
        devopsEnviromentRepository.update(devopsEnvironmentE);
        return true;
    }

    @Override
    public DevopsEnvironmentUpdateDTO query(Long environmentId) {
        return ConvertHelper.convert(devopsEnviromentRepository
                .queryById(environmentId), DevopsEnvironmentUpdateDTO.class);
    }

    @Override
    public DevopsEnvironmentUpdateDTO update(DevopsEnvironmentUpdateDTO devopsEnvironmentUpdateDTO, Long projectId) {
        DevopsEnvironmentE devopsEnvironmentE = ConvertHelper.convert(
                devopsEnvironmentUpdateDTO, DevopsEnvironmentE.class);
        devopsEnvironmentE.initProjectE(projectId);
        if (checkNameChange(devopsEnvironmentUpdateDTO)) {
            devopsEnviromentRepository.checkName(devopsEnvironmentE);
        }
        return ConvertHelper.convert(devopsEnviromentRepository.update(
                devopsEnvironmentE), DevopsEnvironmentUpdateDTO.class);
    }

    @Override
    public List<DevopsEnviromentRepDTO> sort(Long[] environmentIds) {
        List<Long> ids = new ArrayList<>();
        Collections.addAll(ids, environmentIds);
        List<DevopsEnvironmentE> devopsEnvironmentES = ids.stream()
                .map(id -> devopsEnviromentRepository.queryById(id))
                .collect(Collectors.toList());
        Long sequence = 1L;
        for (DevopsEnvironmentE devopsEnvironmentE : devopsEnvironmentES) {
            devopsEnvironmentE.setSequence(sequence);
            devopsEnviromentRepository.update(devopsEnvironmentE);
            sequence = sequence + 1;
        }

        Set<String> namespaces = envListener.connectedEnv();
        for (String str : namespaces) {
            for (DevopsEnvironmentE devopsEnvironmentE : devopsEnvironmentES) {
                if (str.equals(devopsEnvironmentE.getNamespace())) {
                    devopsEnvironmentE.initConnect(true);
                }
            }
        }
        return ConvertHelper.convertList(devopsEnvironmentES, DevopsEnviromentRepDTO.class);
    }

    @Override
    public String queryShell(Long environmentId) {
        DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository.queryById(environmentId);
        InputStream inputStream = this.getClass().getResourceAsStream("/shell/environment.sh");
        Map<String, String> params = new HashMap<>();
        params.put("{NAMESPACE}", devopsEnvironmentE.getNamespace());
        params.put("{VERSION}", agentVersion);
        params.put("{SERVICEURL}", agentServiceUrl);
        params.put("{TOKEN}", devopsEnvironmentE.getToken());
        return FileUtil.replaceReturnString(inputStream, params);
    }

    @Override
    public void checkName(Long projectId, String name) {
        DevopsEnvironmentE devopsEnvironmentE = DevopsEnvironmentFactory.createDevopsEnvironmentE();
        devopsEnvironmentE.initProjectE(projectId);
        devopsEnvironmentE.setName(name);
        devopsEnviromentRepository.checkName(devopsEnvironmentE);
    }

    @Override
    public void checkCode(Long projectId, String code) {
        DevopsEnvironmentE devopsEnvironmentE = DevopsEnvironmentFactory.createDevopsEnvironmentE();
        devopsEnvironmentE.initProjectE(projectId);
        devopsEnvironmentE.setCode(code);
        devopsEnviromentRepository.checkCode(devopsEnvironmentE);
    }

    /**
     * 校验name是否改变
     */
    public Boolean checkNameChange(DevopsEnvironmentUpdateDTO devopsEnvironmentUpdateDTO) {
        DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository
                .queryById(devopsEnvironmentUpdateDTO.getId());
        return devopsEnvironmentE.getName().equals(devopsEnvironmentUpdateDTO.getName()) ? false : true;
    }

    @Override
    public List<DevopsEnviromentRepDTO> listByProjectId(Long projectId) {
        List<DevopsEnviromentRepDTO> devopsEnviromentRepDTOList = listByProjectIdAndActive(projectId, true);
        return devopsEnviromentRepDTOList.stream().filter(t ->
                applicationInstanceRepository.selectByEnvId(t.getId()) > 0)
                .collect(Collectors.toList());
    }
}
