package io.choerodon.devops.infra.persistence.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.DevopsIngressDTO;
import io.choerodon.devops.api.dto.DevopsIngressPathDTO;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.entity.DevopsIngressE;
import io.choerodon.devops.domain.application.entity.DevopsIngressPathE;
import io.choerodon.devops.domain.application.entity.DevopsServiceE;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.domain.application.repository.DevopsIngressRepository;
import io.choerodon.devops.domain.application.repository.DevopsServiceRepository;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.IngressStatus;
import io.choerodon.devops.infra.common.util.enums.ServiceStatus;
import io.choerodon.devops.infra.dataobject.DevopsIngressDO;
import io.choerodon.devops.infra.dataobject.DevopsIngressPathDO;
import io.choerodon.devops.infra.mapper.DevopsIngressMapper;
import io.choerodon.devops.infra.mapper.DevopsIngressPathMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.websocket.helper.EnvListener;
import io.choerodon.websocket.helper.EnvSession;


/**
 * Creator: Runge
 * Date: 2018/4/20
 * Time: 16:07
 * Description:
 */
@Component
public class DevopsIngressRepositoryImpl implements DevopsIngressRepository {
    private static final String DOMAIN_NAME_EXIST_ERROR = "error.domain.name.exist";
    private static final Gson gson = new Gson();
    @Value("${agent.version}")
    private String agentExpectVersion;
    private DevopsIngressMapper devopsIngressMapper;
    private DevopsIngressPathMapper devopsIngressPathMapper;
    private DevopsEnvironmentRepository environmentRepository;
    private DevopsServiceRepository devopsServiceRepository;
    private EnvListener envListener;

    /**
     * 构造函数
     */
    public DevopsIngressRepositoryImpl(DevopsIngressMapper devopsIngressMapper,
                                       DevopsIngressPathMapper devopsIngressPathMapper,
                                       DevopsEnvironmentRepository environmentRepository,
                                       DevopsServiceRepository devopsServiceRepository,
                                       EnvListener envListener) {
        this.devopsIngressMapper = devopsIngressMapper;
        this.devopsIngressPathMapper = devopsIngressPathMapper;
        this.environmentRepository = environmentRepository;
        this.devopsServiceRepository = devopsServiceRepository;
        this.envListener = envListener;
    }

    @Override
    public void createIngress(DevopsIngressDO devopsIngressDO, List<DevopsIngressPathDO> devopsIngressPathDOList) {
        if (!checkIngressName(devopsIngressDO.getEnvId(), devopsIngressDO.getName())) {
            throw new CommonException(DOMAIN_NAME_EXIST_ERROR);
        }
        devopsIngressMapper.insert(devopsIngressDO);
        devopsIngressPathDOList.forEach(t -> {
            t.setIngressId(devopsIngressDO.getId());
            devopsIngressPathMapper.insert(t);
        });
    }

    @Override
    public void updateIngress(DevopsIngressDO devopsIngressDO, List<DevopsIngressPathDO> devopsIngressPathDOList) {
        Long id = devopsIngressDO.getId();
        DevopsIngressDO ingressDO = devopsIngressMapper.selectByPrimaryKey(id);
        if (ingressDO == null) {
            throw new CommonException("domain.not.exist");
        }
        if (!devopsIngressDO.getName().equals(ingressDO.getName())
                && !checkIngressName(devopsIngressDO.getEnvId(), devopsIngressDO.getName())) {
            throw new CommonException(DOMAIN_NAME_EXIST_ERROR);
        }
        if (!ingressDO.equals(devopsIngressDO)) {
            devopsIngressDO.setObjectVersionNumber(ingressDO.getObjectVersionNumber());
            devopsIngressMapper.updateByPrimaryKey(devopsIngressDO);
        }
        List<DevopsIngressPathDO> ingressPathList = devopsIngressPathMapper.select(new DevopsIngressPathDO(id));
        if (!devopsIngressPathDOList.equals(ingressPathList)) {
            devopsIngressPathMapper.delete(new DevopsIngressPathDO(id));
            devopsIngressPathDOList.forEach(t -> {
                t.setIngressId(id);
                devopsIngressPathMapper.insert(t);
            });
        }
    }

    @Override
    public void updateIngress(DevopsIngressDO devopsIngressDO) {
        Long id = devopsIngressDO.getId();
        DevopsIngressDO ingressDO = devopsIngressMapper.selectByPrimaryKey(id);
        if (ingressDO == null) {
            throw new CommonException("domain.not.exist");
        }
        if (!devopsIngressDO.getName().equals(ingressDO.getName())
                && !checkIngressName(devopsIngressDO.getEnvId(), devopsIngressDO.getName())) {
            throw new CommonException(DOMAIN_NAME_EXIST_ERROR);
        }
        if (!ingressDO.equals(devopsIngressDO)) {
            devopsIngressDO.setObjectVersionNumber(ingressDO.getObjectVersionNumber());
            devopsIngressMapper.updateByPrimaryKey(devopsIngressDO);
        }
    }

    @Override
    public Page<DevopsIngressDTO> getIngress(Long projectId, Long envId, PageRequest pageRequest, String params) {
        List<DevopsIngressDTO> devopsIngressDTOS = new ArrayList<>();
        Map<String, Object> maps = gson.fromJson(params, new TypeToken<Map<String, Object>>() {}.getType());
        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        String paramMap = TypeUtil.cast(maps.get(TypeUtil.PARAM));

        if (pageRequest.getSort() != null) {
            Map<String, String> map = new HashMap<>();
            map.put("envName", "de.name");
            map.put("path", "dip.path");
            pageRequest.resetOrder("di", map);
        }

        Page<DevopsIngressDO> devopsIngressDOS =
                PageHelper.doPageAndSort(pageRequest,
                        () -> devopsIngressMapper.selectIngress(projectId, envId, searchParamMap, paramMap));
        Map<String, EnvSession> envs = envListener.connectedEnv();
        devopsIngressDOS.getContent().forEach(t -> {
            DevopsIngressDTO devopsIngressDTO =
                    new DevopsIngressDTO(t.getId(), t.getDomain(), t.getName(),
                            t.getEnvId(), t.getUsable(), t.getEnvName());
            devopsIngressDTO.setStatus(t.getStatus());
            for (Map.Entry<String, EnvSession> entry : envs.entrySet()) {
                EnvSession envSession = entry.getValue();
                if (envSession.getEnvId().equals(t.getEnvId())
                        && agentExpectVersion.compareTo(
                        envSession.getVersion() == null ? "0" : envSession.getVersion()) < 1) {
                    devopsIngressDTO.setEnvStatus(true);
                }
            }
            DevopsIngressPathDO devopsIngressPathDO = new DevopsIngressPathDO(t.getId());
            devopsIngressPathMapper.select(devopsIngressPathDO).forEach(e -> getDevopsIngressDTO(devopsIngressDTO, e));
            devopsIngressDTOS.add(devopsIngressDTO);
        });
        Page<DevopsIngressDTO> ingressDTOPage = new Page<>();
        BeanUtils.copyProperties(devopsIngressDOS, ingressDTOPage);
        ingressDTOPage.setContent(devopsIngressDTOS);
        return ingressDTOPage;
    }

    @Override
    public DevopsIngressDTO getIngress(Long projectId, Long ingressId) {
        DevopsIngressDO devopsIngressDO = devopsIngressMapper.selectByPrimaryKey(ingressId);
        if (devopsIngressDO != null) {
            DevopsEnvironmentE devopsEnvironmentE = environmentRepository.queryById(devopsIngressDO.getEnvId());
            DevopsIngressDTO devopsIngressDTO = new DevopsIngressDTO(
                    ingressId, devopsIngressDO.getDomain(), devopsIngressDO.getName(), devopsEnvironmentE.getId(),
                    devopsIngressDO.getUsable(), devopsEnvironmentE.getName());
            DevopsIngressPathDO devopsIngressPathDO = new DevopsIngressPathDO(ingressId);
            devopsIngressPathMapper.select(devopsIngressPathDO).forEach(e -> getDevopsIngressDTO(devopsIngressDTO, e));
            devopsIngressDTO.setStatus(devopsIngressDO.getStatus());
            return devopsIngressDTO;
        }

        return null;
    }

    @Override
    public DevopsIngressDO getIngress(Long ingressId) {
        return devopsIngressMapper.selectByPrimaryKey(ingressId);
    }

    @Override
    public void deleteIngress(Long ingressId) {
        devopsIngressMapper.deleteByPrimaryKey(ingressId);
        devopsIngressPathMapper.delete(new DevopsIngressPathDO(ingressId));
    }

    @Override
    public Long setStatus(Long envId, String name, String status) {
        DevopsIngressDO ingressDO = new DevopsIngressDO(name);
        ingressDO.setEnvId(envId);
        DevopsIngressDO ingress = devopsIngressMapper.selectOne(ingressDO);
        ingress.setStatus(status);
        if (status.equals(IngressStatus.RUNNING.getStatus())) {
            ingress.setUsable(true);
        }
        devopsIngressMapper.updateByPrimaryKey(ingress);
        return ingress.getId();
    }

    @Override
    public List<String> queryIngressNameByServiceId(Long serviceId) {
        return devopsIngressMapper.queryIngressNameByServiceId(serviceId);
    }

    @Override
    public Boolean checkIngressName(Long envId, String name) {
        DevopsIngressDO devopsIngressDO = new DevopsIngressDO(name);
        devopsIngressDO.setEnvId(envId);
        return devopsIngressMapper.select(devopsIngressDO).isEmpty();
    }

    @Override
    public Boolean checkIngressAndPath(Long id, String domain, String path) {
        return !devopsIngressPathMapper.checkDomainAndPath(id, domain, path);
    }

    @Override
    public DevopsIngressE selectByEnvAndName(Long envId, String name) {
        DevopsIngressDO domainDO = new DevopsIngressDO();
        domainDO.setEnvId(envId);
        domainDO.setName(name);
        return ConvertHelper.convert(devopsIngressMapper.selectOne(domainDO), DevopsIngressE.class);
    }

    @Override
    public DevopsIngressE insertIngress(DevopsIngressE devopsIngressE) {
        DevopsIngressDO ingressDO = ConvertHelper.convert(devopsIngressE, DevopsIngressDO.class);
        if (devopsIngressMapper.insert(ingressDO) != 1) {
            throw new CommonException("error.domain.insert");
        }
        return ConvertHelper.convert(ingressDO, DevopsIngressE.class);
    }

    @Override
    public void insertIngressPath(DevopsIngressPathE devopsIngressPathE) {
        if (devopsIngressPathMapper.insert(ConvertHelper.convert(devopsIngressPathE, DevopsIngressPathDO.class)) != 1) {
            throw new CommonException("error.domainAttr.insert");
        }
    }

    @Override
    public List<DevopsIngressPathE> selectByEnvIdAndServiceName(Long envId, String serviceName) {
        return ConvertHelper.convertList(
                devopsIngressPathMapper.selectByEnvIdAndServiceName(envId, serviceName),
                DevopsIngressPathE.class);
    }

    @Override
    public List<DevopsIngressPathE> selectByEnvIdAndServiceId(Long envId, Long serviceId) {
        return ConvertHelper.convertList(
                devopsIngressPathMapper.selectByEnvIdAndServiceId(envId, serviceId),
                DevopsIngressPathE.class);
    }

    @Override
    public List<DevopsIngressPathE> selectByIngressId(Long ingressId) {
        DevopsIngressPathDO devopsIngressPathDO = new DevopsIngressPathDO();
        devopsIngressPathDO.setIngressId(ingressId);
        return ConvertHelper.convertList(
                devopsIngressPathMapper.select(devopsIngressPathDO),
                DevopsIngressPathE.class);
    }

    @Override
    public List<DevopsIngressE> listByEnvId(Long envId) {
        DevopsIngressDO devopsIngressDO = new DevopsIngressDO();
        devopsIngressDO.setEnvId(envId);
        return ConvertHelper.convertList(devopsIngressMapper.select(devopsIngressDO), DevopsIngressE.class);
    }

    @Override
    public void updateIngressPath(DevopsIngressPathE devopsIngressPathE) {
        if (devopsIngressPathMapper.updateByPrimaryKey(
                ConvertHelper.convert(devopsIngressPathE, DevopsIngressPathDO.class)) != 1) {
            throw new CommonException("error.domainAttr.update");
        }
    }

    @Override
    public void deleteIngressPath(Long ingressId) {
        DevopsIngressPathDO devopsIngressPathDO = new DevopsIngressPathDO();
        devopsIngressPathDO.setIngressId(ingressId);
        devopsIngressPathMapper.delete(devopsIngressPathDO);
    }

    @Override
    public Boolean checkEnvHasIngress(Long envId) {
        return devopsIngressMapper.checkEnvHasIngress(envId);
    }

    @Override
    public List<DevopsIngressE> list() {
        return ConvertHelper.convertList(devopsIngressMapper.selectAll(), DevopsIngressE.class);
    }

    private void getDevopsIngressDTO(DevopsIngressDTO devopsIngressDTO, DevopsIngressPathDO e) {
        DevopsServiceE devopsServiceE = devopsServiceRepository.query(e.getServiceId());
        DevopsIngressPathDTO devopsIngressPathDTO = new DevopsIngressPathDTO(
                e.getPath(), e.getServiceId(), e.getServiceName(),
                devopsServiceE == null ? ServiceStatus.DELETED.getStatus() : devopsServiceE.getStatus());
        devopsIngressPathDTO.setServicePort(e.getServicePort());
        devopsIngressDTO.addDevopsIngressPathDTO(devopsIngressPathDTO);
    }
}
