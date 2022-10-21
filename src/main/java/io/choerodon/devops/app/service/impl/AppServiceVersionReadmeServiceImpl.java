package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.AppServiceVersionReadmeService;
import io.choerodon.devops.infra.dto.AppServiceVersionReadmeDTO;
import io.choerodon.devops.infra.mapper.AppServiceVersionReadmeMapper;

/**
 * Created by Sheep on 2019/7/12.
 */
@Service
public class AppServiceVersionReadmeServiceImpl implements AppServiceVersionReadmeService {

    private static final String ERROR_INSERT_VERSION_README = "error.insert.version.readme";

    @Autowired
    private AppServiceVersionReadmeMapper appServiceVersionReadmeMapper;

    @Override
    @Transactional
    public AppServiceVersionReadmeDTO baseCreate(AppServiceVersionReadmeDTO appServiceVersionReadmeDTO) {
        if (appServiceVersionReadmeMapper.insert(appServiceVersionReadmeDTO) != 1) {
            throw new CommonException(ERROR_INSERT_VERSION_README);
        }
        return appServiceVersionReadmeDTO;
    }



}
