package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.valueobject.ApplicationVersionReadmeV;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  11:50 2019/7/3
 * Description:
 */
public interface ApplicationVersionReadmeRepository {
    ApplicationVersionReadmeV create(ApplicationVersionReadmeV versionReadmeV);
}
