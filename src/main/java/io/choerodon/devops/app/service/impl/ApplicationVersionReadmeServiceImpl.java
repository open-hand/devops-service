package io.choerodon.devops.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.ApplicationVersionReadmeService;
import io.choerodon.devops.infra.dto.ApplicationVersionReadmeDTO;
import io.choerodon.devops.infra.mapper.ApplicationVersionReadmeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Sheep on 2019/7/12.
 */
@Service
public class ApplicationVersionReadmeServiceImpl implements ApplicationVersionReadmeService {

    @Autowired
    private ApplicationVersionReadmeMapper applicationVersionReadmeMapper;

    public ApplicationVersionReadmeDTO baseCreate(ApplicationVersionReadmeDTO applicationVersionReadmeDTO) {
        if (applicationVersionReadmeMapper.insert(applicationVersionReadmeDTO) != 1) {
            throw new CommonException("error.insert.version.readme");
        }
        return applicationVersionReadmeDTO;
    }



}
