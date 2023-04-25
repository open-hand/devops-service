package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.IngressNginxAnnotationVO;
import io.choerodon.devops.app.service.IngressNginxAnnotationService;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.IngressNginxAnnotationDTO;
import io.choerodon.devops.infra.mapper.IngressNginxAnnotationMapper;

/**
 * Nginx-Ingress注解配置(IngressNginxAnnotation)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-04-25 15:29:54
 */
@Service
public class IngressNginxAnnotationServiceImpl implements IngressNginxAnnotationService {

    @Autowired
    private IngressNginxAnnotationMapper ingressNginxAnnotationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(Long ingressId, List<IngressNginxAnnotationVO> nginxIngressAnnotations) {
        if (!CollectionUtils.isEmpty(nginxIngressAnnotations)) {
            List<IngressNginxAnnotationDTO> ingressNginxAnnotationDTOS = nginxIngressAnnotations
                    .stream()
                    .map(nginxIngressAnnotationVO -> new IngressNginxAnnotationDTO(ingressId, nginxIngressAnnotationVO.getAnnotationKey(), nginxIngressAnnotationVO.getAnnotationValue()))
                    .collect(Collectors.toList());
            ingressNginxAnnotationMapper.insertListSelective(ingressNginxAnnotationDTOS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIngressId(Long ingressId) {
        Assert.notNull(ingressId, ResourceCheckConstant.DEVOPS_INGRESS_ID_IS_NULL);

        IngressNginxAnnotationDTO ingressNginxAnnotationDTO = new IngressNginxAnnotationDTO();
        ingressNginxAnnotationDTO.setIngressId(ingressId);
        ingressNginxAnnotationMapper.delete(ingressNginxAnnotationDTO);
    }

    @Override
    public List<IngressNginxAnnotationDTO> listByIngressId(Long ingressId) {
        Assert.notNull(ingressId, ResourceCheckConstant.DEVOPS_INGRESS_ID_IS_NULL);

        IngressNginxAnnotationDTO ingressNginxAnnotationDTO = new IngressNginxAnnotationDTO();
        ingressNginxAnnotationDTO.setIngressId(ingressId);
        return ingressNginxAnnotationMapper.select(ingressNginxAnnotationDTO);
    }
}

