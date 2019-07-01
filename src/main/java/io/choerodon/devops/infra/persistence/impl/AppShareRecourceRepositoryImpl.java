package io.choerodon.devops.infra.persistence.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.AppShareResourceE;
import io.choerodon.devops.domain.application.repository.AppShareRecouceRepository;
import io.choerodon.devops.infra.dataobject.AppShareResourceDO;
import io.choerodon.devops.infra.mapper.AppShareResourceMapper;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:12 2019/6/28
 * Description:
 */
@Component
public class AppShareRecourceRepositoryImpl implements AppShareRecouceRepository {
    @Autowired
    private AppShareResourceMapper appShareResourceMapper;

    @Override
    public void create(AppShareResourceE appShareResourceE) {
        AppShareResourceDO shareResourceDO = ConvertHelper.convert(appShareResourceE, AppShareResourceDO.class);
        if (appShareResourceMapper.insert(shareResourceDO) != 1) {
            throw new CommonException("error.insert.app.share.resource");
        }
    }

    @Override
    public void delete(Long shareId, Long projectId) {
        AppShareResourceDO shareResourceDO = new AppShareResourceDO();
        shareResourceDO.setShareId(shareId);
        shareResourceDO.setProjectId(projectId);
        appShareResourceMapper.deleteByPrimaryKey(shareResourceDO);
    }
}
