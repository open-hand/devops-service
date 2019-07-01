package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.entity.AppShareResourceE;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:04 2019/6/28
 * Description:
 */
public interface AppShareRecouceRepository {
    void create(AppShareResourceE appShareResourceE);

    void delete(Long shareId, Long projectId);
}
