package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import io.choerodon.devops.api.vo.C7nCertificationCreateOrUpdateVO;
import io.choerodon.devops.app.service.DevopsCertificationNoticeService;
import io.choerodon.devops.infra.dto.CertificationNoticeDTO;
import io.choerodon.devops.infra.mapper.DevopsCertificationNoticeMapper;

@Service
public class DevopsCertificationNoticeServiceImpl implements DevopsCertificationNoticeService {
    @Autowired
    private DevopsCertificationNoticeMapper devopsCertificationNoticeMapper;


    @Transactional
    @Override
    public void batchCreate(Long certificationId, List<C7nCertificationCreateOrUpdateVO.NotifyObject> notifyObjects) {
        if (ObjectUtils.isEmpty(notifyObjects)) {
            return;
        }
        List<CertificationNoticeDTO> certificationNoticeDTOList = new ArrayList<>();
        notifyObjects.forEach(notifyObject -> {
            CertificationNoticeDTO certificationNoticeDTO = new CertificationNoticeDTO(notifyObject.getType(), notifyObject.getId(), certificationId);
            certificationNoticeDTOList.add(certificationNoticeDTO);
        });

        devopsCertificationNoticeMapper.insertList(certificationNoticeDTOList);
    }

    @Transactional
    @Override
    public void batchUpdate(Long certificationId, List<C7nCertificationCreateOrUpdateVO.NotifyObject> notifyObjects) {
        if (ObjectUtils.isEmpty(notifyObjects)) {
            return;
        }
        devopsCertificationNoticeMapper.deleteByCertificationId(certificationId);
        batchCreate(certificationId, notifyObjects);
    }

    @Override
    public List<CertificationNoticeDTO> listByCertificationIds(List<Long> certificationIds) {
        return devopsCertificationNoticeMapper.listByCertificationIds(certificationIds);
    }

    @Override
    public List<CertificationNoticeDTO> listByCertificationId(Long certificationId) {
        return devopsCertificationNoticeMapper.listByCertificationId(certificationId);
    }
}
