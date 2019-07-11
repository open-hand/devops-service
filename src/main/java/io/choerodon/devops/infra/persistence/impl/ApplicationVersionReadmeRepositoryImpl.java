package io.choerodon.devops.infra.persistence.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.repository.ApplicationVersionReadmeRepository;
import io.choerodon.devops.domain.application.valueobject.ApplicationVersionReadmeV;
import io.choerodon.devops.infra.dto.ApplicationVersionReadmeDO;
import io.choerodon.devops.infra.mapper.ApplicationVersionReadmeMapper;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  11:52 2019/7/3
 * Description:
 */
@Component
public class ApplicationVersionReadmeRepositoryImpl implements ApplicationVersionReadmeRepository {
    @Autowired
    private ApplicationVersionReadmeMapper versionReadmeMapper;

    @Override
    public ApplicationVersionReadmeV create(ApplicationVersionReadmeV versionReadmeV) {
        ApplicationVersionReadmeDO versionReadmeDO = new ApplicationVersionReadmeDO();
        versionReadmeDO.setReadme(versionReadmeV.getReadme());
        if (versionReadmeMapper.insert(versionReadmeDO) != 1) {
            throw new CommonException("error.insert.version.readme");
        }
        BeanUtils.copyProperties(versionReadmeDO, versionReadmeV);
        return versionReadmeV;
    }
}
