package io.choerodon.devops.infra.persistence.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsIngressDTO;
import io.choerodon.devops.api.vo.DevopsIngressPathDTO;
import io.choerodon.devops.api.vo.iam.entity.*;
import io.choerodon.devops.domain.application.repository.CertificationRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.domain.application.repository.DevopsIngressRepository;
import io.choerodon.devops.domain.application.repository.DevopsServiceRepository;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.enums.IngressStatus;
import io.choerodon.devops.infra.enums.ServiceStatus;
import io.choerodon.devops.infra.dataobject.DevopsIngressDO;
import io.choerodon.devops.infra.dataobject.DevopsIngressPathDO;
import io.choerodon.devops.infra.mapper.DevopsIngressMapper;
import io.choerodon.devops.infra.mapper.DevopsIngressPathMapper;



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
    @Autowired
    private DevopsIngressMapper devopsIngressMapper;
    @Autowired
    private DevopsIngressPathMapper devopsIngressPathMapper;
    @Autowired
    private DevopsEnvironmentRepository environmentRepository;
    @Autowired
    private DevopsServiceRepository devopsServiceRepository;
    @Autowired
    private CertificationRepository certificationRepository;


    @Override
    public DevopsIngressDO createIngress(DevopsIngressDO devopsIngressDO) {
        if (!checkIngressName(devopsIngressDO.getEnvId(), devopsIngressDO.getName())) {
            throw new CommonException(DOMAIN_NAME_EXIST_ERROR);
        }
        devopsIngressMapper.insert(devopsIngressDO);
        devopsIngressDO.getDevopsIngressPathDOS().forEach(t -> {
            t.setIngressId(devopsIngressDO.getId());
            devopsIngressPathMapper.insert(t);
        });
        return devopsIngressDO;
    }

    @Override
    public void updateIngressAndIngressPath(DevopsIngressDO devopsIngressDO) {
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
        if (!devopsIngressDO.getDevopsIngressPathDOS().equals(ingressPathList)) {
            devopsIngressPathMapper.delete(new DevopsIngressPathDO(id));
            devopsIngressDO.getDevopsIngressPathDOS().forEach(t -> {
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
        devopsIngressDO.setObjectVersionNumber(ingressDO.getObjectVersionNumber());
        devopsIngressMapper.updateByPrimaryKeySelective(devopsIngressDO);
    }

    @Override
    public PageInfo<DevopsIngressDTO> getIngress(Long projectId, Long envId, Long serviceId, PageRequest pageRequest, String params) {
        List<DevopsIngressDTO> devopsIngressDTOS = new ArrayList<>();

        Map<String, Object> maps = gson.fromJson(params, new TypeToken<Map<String, Object>>() {
        }.getType());

        Sort sort = pageRequest.getSort();
        String sortResult = "";
        if (sort != null) {
            sortResult = Lists.newArrayList(pageRequest.getSort().iterator()).stream()
                    .map(t -> {
                        String property = t.getProperty();
                        if (property.equals("envName")) {
                            property = "de.name";
                        } else if (property.equals("path")) {
                            property = "dip.path";
                        }
                        return property + " " + t.getDirection();
                    })
                    .collect(Collectors.joining(","));
        }


        PageInfo<DevopsIngressDO> devopsIngressDOS =
                PageHelper.startPage(pageRequest.getPage(),pageRequest.getSize(), sortResult).doSelectPageInfo(
                        () -> devopsIngressMapper.selectIngress(projectId, envId, serviceId, maps == null ? null : TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)), maps == null ? null : TypeUtil.cast(maps.get(TypeUtil.PARAM))));
        devopsIngressDOS.getList().forEach(t -> {
            DevopsIngressDTO devopsIngressDTO =
                    new DevopsIngressDTO(t.getId(), t.getDomain(), t.getName(),
                            t.getEnvId(), t.getUsable(), t.getEnvName());
            devopsIngressDTO.setStatus(t.getStatus());
            devopsIngressDTO.setCommandStatus(t.getCommandStatus());
            devopsIngressDTO.setCommandType(t.getCommandType());
            devopsIngressDTO.setError(t.getError());
            setIngressDTOCert(t.getCertId(), devopsIngressDTO);
            DevopsIngressPathDO devopsIngressPathDO = new DevopsIngressPathDO(t.getId());
            devopsIngressPathMapper.select(devopsIngressPathDO).forEach(e -> getDevopsIngressDTO(devopsIngressDTO, e));
            devopsIngressDTOS.add(devopsIngressDTO);
        });
        PageInfo<DevopsIngressDTO> ingressDTOPage = new PageInfo<>();
        BeanUtils.copyProperties(devopsIngressDOS, ingressDTOPage);
        ingressDTOPage.setList(devopsIngressDTOS);
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
            setIngressDTOCert(devopsIngressDO.getCertId(), devopsIngressDTO);
            return devopsIngressDTO;
        }

        return null;
    }

    @Override
    public DevopsIngressDO getIngress(Long ingressId) {
        return devopsIngressMapper.selectByPrimaryKey(ingressId);
    }

    private void setIngressDTOCert(Long certId, DevopsIngressDTO devopsIngressDTO) {
        if (certId != null) {
            devopsIngressDTO.setCertId(certId);
            CertificationE certificationE = certificationRepository.queryById(certId);
            if (certificationE != null) {
                devopsIngressDTO.setCertName(certificationE.getName());
                devopsIngressDTO.setCertStatus(certificationE.getStatus());
            }
        }
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
    public Boolean checkIngressAndPath(Long envId, String domain, String path, Long id) {
        return !devopsIngressPathMapper.checkDomainAndPath(envId, domain, path, id);
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

    @Override
    public void deleteIngressAndIngressPathByEnvId(Long envId) {
        DevopsIngressDO devopsIngressDO = new DevopsIngressDO();
        devopsIngressDO.setEnvId(envId);
        // 获取环境下的所有域名ids
        List<Long> allIngressIds = devopsIngressMapper.select(devopsIngressDO).stream().map(DevopsIngressDO::getId)
                .collect(Collectors.toList());
        devopsIngressMapper.delete(devopsIngressDO);
        if (!allIngressIds.isEmpty()) {
            devopsIngressPathMapper.deleteByIngressIds(allIngressIds);
        }
    }
}
