package io.choerodon.devops.domain.application.factory;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.domain.application.entity.ProjectE;

public class ProjectEFactory {
    private ProjectEFactory() {
    }

    public static ProjectE create() {
        return ApplicationContextHelper.getSpringFactory().getBean(ProjectE.class);
    }
}
