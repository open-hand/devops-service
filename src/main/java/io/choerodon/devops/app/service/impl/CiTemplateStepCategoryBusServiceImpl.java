package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.hzero.core.util.AssertUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.template.CiTemplateStepCategoryVO;
import io.choerodon.devops.app.service.CiTemplateStepCategoryBusService;
import io.choerodon.devops.infra.constant.Constant;
import io.choerodon.devops.infra.dto.CiTemplateStepCategoryDTO;
import io.choerodon.devops.infra.dto.CiTemplateStepDTO;
import io.choerodon.devops.infra.mapper.CiTemplateStepBusMapper;
import io.choerodon.devops.infra.mapper.CiTemplateStepCategoryBusMapper;
import io.choerodon.devops.infra.util.UserDTOFillUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/12/2
 */
@Service
public class CiTemplateStepCategoryBusServiceImpl implements CiTemplateStepCategoryBusService {
    private static final Integer STEP_CATEGORY_NAME_LENGTH = 30;

    @Autowired
    private CiTemplateStepCategoryBusMapper ciTemplateStepCategoryBusMapper;

    @Autowired
    private CiTemplateStepBusMapper ciTemplateStepBusMapper;

    @Override
    public Page<CiTemplateStepCategoryVO> pageTemplateStepCategory(Long sourceId,
                                                                   PageRequest pageRequest, String searchParam) {
        Page<CiTemplateStepCategoryVO> ciTemplateStepCategoryVOPage = PageHelper.doPageAndSort(pageRequest,
                () -> ciTemplateStepCategoryBusMapper.queryTemplateStepCategoryByParams(sourceId, searchParam));
        if (CollectionUtils.isEmpty(ciTemplateStepCategoryVOPage.getContent())) {
            return ciTemplateStepCategoryVOPage;
        }
        UserDTOFillUtil.fillUserInfo(ciTemplateStepCategoryVOPage.getContent(), Constant.CREATED_BY, Constant.CREATOR);
        ciTemplateStepCategoryVOPage.getContent().forEach(ciTemplateStepCategoryVO -> {
            if (ciTemplateStepCategoryVO.getBuiltIn()) {
                ciTemplateStepCategoryVO.setCreator(null);
            }
        });
        return ciTemplateStepCategoryVOPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiTemplateStepCategoryVO updateTemplateStepCategory(Long sourceId,
                                                               CiTemplateStepCategoryVO ciTemplateStepCategoryVO) {
        CiTemplateStepCategoryDTO ciTemplateStepCategoryDTO
                = ciTemplateStepCategoryBusMapper.selectByPrimaryKey(ciTemplateStepCategoryVO.getId());
        AssertUtils.notNull(ciTemplateStepCategoryDTO, "error.ci.step.template.category.not.exist");
        AssertUtils.isTrue(!ciTemplateStepCategoryDTO.getBuiltIn(), "error.update.builtin.step.template.category");
        AssertUtils.isTrue(ciTemplateStepCategoryDTO.getName().length() <= STEP_CATEGORY_NAME_LENGTH,
                "error.step.template.limit.exceeded");
        AssertUtils.isTrue(checkTemplateStepCategory(sourceId, ciTemplateStepCategoryVO.getName(),
                ciTemplateStepCategoryVO.getId()),
                "error.pipeline.category.exist");
        BeanUtils.copyProperties(ciTemplateStepCategoryVO, ciTemplateStepCategoryDTO);
        ciTemplateStepCategoryBusMapper.updateByPrimaryKeySelective(ciTemplateStepCategoryDTO);
        return ConvertUtils
                .convertObject(ciTemplateStepCategoryBusMapper.selectByPrimaryKey(ciTemplateStepCategoryDTO.getId()),
                        CiTemplateStepCategoryVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplateStepCategory(Long sourceId, Long ciTemplateCategoryId) {
        CiTemplateStepCategoryDTO ciTemplateStepCategoryDTO
                = ciTemplateStepCategoryBusMapper.selectByPrimaryKey(ciTemplateCategoryId);
        if (ciTemplateStepCategoryDTO == null) {
            return;
        }
        checkRelated(ciTemplateStepCategoryDTO);
        AssertUtils.isTrue(!ciTemplateStepCategoryDTO.getBuiltIn(), "error.delete.builtin.step.template.category");
        ciTemplateStepCategoryBusMapper.deleteByPrimaryKey(ciTemplateStepCategoryDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiTemplateStepCategoryVO createTemplateStepCategory(Long sourceId,
                                                               CiTemplateStepCategoryVO ciTemplateStepCategoryVO) {
        AssertUtils.notNull(ciTemplateStepCategoryVO, "error.ci.template.step.category.null");
        AssertUtils.isTrue(checkTemplateStepCategory(sourceId, ciTemplateStepCategoryVO.getName(), null),
                "error.pipeline.category.exist");
        //校验类型必须是自定义的
        AssertUtils.isTrue(!ciTemplateStepCategoryVO.getBuiltIn(), "error.ci.template.step.category.built.in");
        CiTemplateStepCategoryDTO ciTemplateStepCategoryDTO = new CiTemplateStepCategoryDTO();
        BeanUtils.copyProperties(ciTemplateStepCategoryVO, ciTemplateStepCategoryDTO);
        if (ciTemplateStepCategoryBusMapper.insertSelective(ciTemplateStepCategoryDTO) != 1) {
            throw new CommonException("error.create.step.template.category");
        }
        return ConvertUtils.convertObject(ciTemplateStepCategoryDTO, CiTemplateStepCategoryVO.class);

    }

    @Override
    public Boolean checkTemplateStepCategory(Long sourceId, String name, Long ciTemplateCategoryId) {
        return ciTemplateStepCategoryBusMapper.checkTemplateStepCategoryName(sourceId, name, ciTemplateCategoryId)
                != null ? Boolean.FALSE : Boolean.TRUE;
    }

    /**
     * 校验组织层或者平台层是否有关联该分类
     *
     * @param ciTemplateStepCategoryDTO
     */
    private void checkRelated(CiTemplateStepCategoryDTO ciTemplateStepCategoryDTO) {
        CiTemplateStepDTO ciTemplateStepDTO = new CiTemplateStepDTO();
        ciTemplateStepDTO.setCategoryId(ciTemplateStepCategoryDTO.getId());
        List<CiTemplateStepDTO> ciTemplateStepDTOS = ciTemplateStepBusMapper.select(ciTemplateStepDTO);
        AssertUtils.isTrue(CollectionUtils.isEmpty(ciTemplateStepDTOS), "error.delete.step.category.related");
    }

}
