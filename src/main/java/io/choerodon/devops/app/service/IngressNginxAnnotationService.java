package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.IngressNginxAnnotationVO;
import io.choerodon.devops.infra.dto.IngressNginxAnnotationDTO;

/**
 * Nginx-Ingress注解配置(IngressNginxAnnotation)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-04-25 15:29:54
 */
public interface IngressNginxAnnotationService {

    void batchSave(Long ingressId, List<IngressNginxAnnotationVO> nginxIngressAnnotations);

    void deleteByIngressId(Long ingressId);

    List<IngressNginxAnnotationDTO> listByIngressId(Long ingressId);

    List<IngressNginxAnnotationVO> listVOByIngressId(Long ingressId);

    List<IngressNginxAnnotationVO> listNginxIngressAnnotation();

}

