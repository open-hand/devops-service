package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.app.service.VulnTargetRelService;
import io.choerodon.devops.infra.dto.VulnTargetRelDTO;
import io.choerodon.devops.infra.mapper.VulnTargetRelMapper;
import io.choerodon.devops.infra.util.C7nCollectionUtils;

/**
 * 漏洞扫描对象关系表(VulnTargetRel)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 15:27:39
 */
@Service
public class VulnTargetRelServiceImpl implements VulnTargetRelService {
    @Autowired
    private VulnTargetRelMapper vulnTargetRelMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<VulnTargetRelDTO> vulnTargetRelDTOList) {
        int pageSize = 500;
        if (!CollectionUtils.isEmpty(vulnTargetRelDTOList)) {
            if (vulnTargetRelDTOList.size() > pageSize) {
                List<List<VulnTargetRelDTO>> fragmentList = C7nCollectionUtils.fragmentList(vulnTargetRelDTOList, pageSize);
                for (List<VulnTargetRelDTO> vulnTargetRelDTOS : fragmentList) {
                    vulnTargetRelMapper.insertListSelective(vulnTargetRelDTOS);
                }
            } else {
                vulnTargetRelMapper.insertListSelective(vulnTargetRelDTOList);
            }
        }
    }
}

