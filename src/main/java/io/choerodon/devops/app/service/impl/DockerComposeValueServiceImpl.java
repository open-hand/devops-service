package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.DockerComposeValueService;
import io.choerodon.devops.infra.dto.DockerComposeValueDTO;
import io.choerodon.devops.infra.mapper.DockerComposeValueMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * docker compose部署时保存的yaml文件内容(DockerComposeValue)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2022-04-07 10:25:56
 */
@Service
public class DockerComposeValueServiceImpl implements DockerComposeValueService {
    @Autowired
    private DockerComposeValueMapper dockerComposeValueMapper;


    @Override
    @Transactional
    public void baseCreate(DockerComposeValueDTO dockerComposeValueDTO) {
        MapperUtil.resultJudgedInsertSelective(dockerComposeValueMapper, dockerComposeValueDTO, "error.save.compose.value");
    }

    @Override
    public DockerComposeValueDTO baseQuery(Long id) {
        return dockerComposeValueMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional
    public void deleteByAppId(Long appId) {
        Assert.notNull(appId, "error.appId.is.null");

        DockerComposeValueDTO dockerComposeValueDTO = new DockerComposeValueDTO();
        dockerComposeValueDTO.setAppId(appId);
        dockerComposeValueMapper.delete(dockerComposeValueDTO);
    }
}

