package io.choerodon.devops.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.AppServiceVersionReadmeService;
import io.choerodon.devops.infra.dto.AppServiceVersionReadmeDTO;
import io.choerodon.devops.infra.mapper.AppServiceVersionReadmeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Sheep on 2019/7/12.
 */
@Service
public class AppServiceVersionReadmeServiceImpl implements AppServiceVersionReadmeService {

    @Autowired
    private AppServiceVersionReadmeMapper appServiceVersionReadmeMapper;

    public AppServiceVersionReadmeDTO baseCreate(AppServiceVersionReadmeDTO appServiceVersionReadmeDTO) {
        if (appServiceVersionReadmeMapper.insert(appServiceVersionReadmeDTO) != 1) {
            throw new CommonException("error.insert.version.readme");
        }
        return appServiceVersionReadmeDTO;
    }



}
