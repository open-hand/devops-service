package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.HelmConfigVO;
import io.choerodon.devops.app.service.DevopsHelmConfigService;

@Service
public class DevopsHelmConfigServiceImpl implements DevopsHelmConfigService {
    @Override
    public List<HelmConfigVO> listHelmConfig(Long projectId) {

        List<HelmConfigVO> helmConfigVOList = new ArrayList<>();

        HelmConfigVO helmConfigVO1 = new HelmConfigVO();
        helmConfigVO1.setUrl("http://www.example.com/org/projects/");
        helmConfigVO1.setUsername("username");
        helmConfigVO1.setPassword("password");
        helmConfigVO1.setName("测试仓库1");
        helmConfigVO1.setRepoPrivate(true);
        helmConfigVO1.setRepoDefault(true);
        helmConfigVO1.setResourceType("organization");
        helmConfigVO1.setCreationDate(new Date());
        helmConfigVO1.setCreatorImageUrl("http://minio.c7n.devops.hand-china.com/iam-service/0/CHOERODON-MINIO/54d21810ba514c87966d28579e65a9ec@src=http___5b0988e595225.cdn.sohucs.com_images_20200424_7c24b1d510b14d0599d69f6c4052867d.jpeg&refer=http___5b0988e595225.cdn.sohucs.jfif");
        helmConfigVO1.setCreatorLoginName("25147");
        helmConfigVO1.setCreatorRealName("周扒皮");

        HelmConfigVO helmConfigVO2 = new HelmConfigVO();
        helmConfigVO2.setUrl("http://www.example.com/org/projects/");
        helmConfigVO2.setUsername("username");
        helmConfigVO2.setPassword("password");
        helmConfigVO2.setName("测试仓库1");
        helmConfigVO2.setRepoPrivate(false);
        helmConfigVO2.setRepoDefault(false);
        helmConfigVO2.setResourceType("project");
        helmConfigVO2.setCreationDate(new Date());
        helmConfigVO2.setCreatorImageUrl("http://minio.c7n.devops.hand-china.com/iam-service/0/CHOERODON-MINIO/54d21810ba514c87966d28579e65a9ec@src=http___5b0988e595225.cdn.sohucs.com_images_20200424_7c24b1d510b14d0599d69f6c4052867d.jpeg&refer=http___5b0988e595225.cdn.sohucs.jfif");
        helmConfigVO2.setCreatorLoginName("25147");
        helmConfigVO2.setCreatorRealName("周扒皮");

        helmConfigVOList.add(helmConfigVO1);
        helmConfigVOList.add(helmConfigVO2);
        return helmConfigVOList;
    }
}
