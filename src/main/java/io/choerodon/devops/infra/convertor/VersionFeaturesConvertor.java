package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.VersionFeaturesDTO;
import io.choerodon.devops.domain.application.valueobject.PipelineResultV;

/**
 * Created by Zenger on 2018/4/18.
 */
@Component
public class VersionFeaturesConvertor implements ConvertorI<PipelineResultV, Object, VersionFeaturesDTO> {

    @Override
    public VersionFeaturesDTO entityToDto(PipelineResultV entity) {
        VersionFeaturesDTO versionFeaturesDTO = new VersionFeaturesDTO();
        BeanUtils.copyProperties(entity, versionFeaturesDTO);
        return versionFeaturesDTO;
    }
}
