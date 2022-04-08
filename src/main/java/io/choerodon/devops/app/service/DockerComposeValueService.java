package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DockerComposeValueDTO;

/**
 * docker compose部署时保存的yaml文件内容(DockerComposeValue)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2022-04-07 10:25:56
 */
public interface DockerComposeValueService {

    void baseCreate(DockerComposeValueDTO dockerComposeValueDTO);

    DockerComposeValueDTO baseQuery(Long id);

    void deleteByAppId(Long appId);

    void baseUpdate(DockerComposeValueDTO dockerComposeValueDTO);
}

