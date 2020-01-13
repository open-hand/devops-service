package io.choerodon.devops.app.service.impl;

import java.util.Map;

import org.springframework.stereotype.Component;

import io.choerodon.devops.infra.enums.GitOpsObjectError;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;

/**
 * Converter for resource that's not supported yet.
 *
 * @author zmf
 * @since 11/1/19
 */
@Component
public class ConvertNotSupportedResourceServiceImpl extends ConvertK8sObjectService<Object> {
    public ConvertNotSupportedResourceServiceImpl() {
        super(Object.class);
    }

    @Override
    public Object serializableObject(String jsonString, String filePath, Map<String, String> objectPath, Long envId) {
        throw new GitOpsExplainException(GitOpsObjectError.RESOURCE_TYPE_NOT_SUPPORTED.getError(), filePath);
    }

    @Override
    public ResourceType getType() {
        return ResourceType.MISSTYPE;
    }
}
