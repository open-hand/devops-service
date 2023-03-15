package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hzero.core.util.AssertUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.template.CiTemplateCategoryVO;
import io.choerodon.devops.app.service.CiTemplateCategoryBusService;
import io.choerodon.devops.infra.constant.Constant;
import io.choerodon.devops.infra.dto.CiTemplateCategoryDTO;
import io.choerodon.devops.infra.mapper.CiTemplateCategoryBusMapper;
import io.choerodon.devops.infra.util.UserDTOFillUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/12/2
 */
@Service
public class CiTemplateCategoryBusServiceImpl implements CiTemplateCategoryBusService {

    @Autowired
    private CiTemplateCategoryBusMapper ciTemplateCategoryBusMapper;


    @Override
    public Page<CiTemplateCategoryVO> pageTemplateCategory(PageRequest pageRequest, String searchParam) {
        Page<CiTemplateCategoryDTO> ciTemplateCategoryDTOS = PageHelper.doPageAndSort(pageRequest,
                () -> ciTemplateCategoryBusMapper.pageTemplateCategory(searchParam));
        Page<CiTemplateCategoryVO> ciTemplateCategoryVOS = ConvertUtils.convertPage(ciTemplateCategoryDTOS, CiTemplateCategoryVO.class);
        if (CollectionUtils.isEmpty(ciTemplateCategoryVOS.getContent())) {
            return ciTemplateCategoryVOS;
        }
        UserDTOFillUtil.fillUserInfo(ciTemplateCategoryVOS.getContent(), Constant.CREATED_BY, Constant.CREATOR);
        //平台预置的不返回创建者
        ciTemplateCategoryVOS.getContent().forEach(ciTemplateCategoryVO -> {
            if (ciTemplateCategoryVO.getBuiltIn()) {
                ciTemplateCategoryVO.setCreator(null);
            }
        });
        return ciTemplateCategoryVOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiTemplateCategoryVO updateTemplateCategory(Long sourceId, CiTemplateCategoryVO ciTemplateCategoryVO) {
        AssertUtils.isTrue(checkTemplateCategoryName(sourceId, ciTemplateCategoryVO.getCategory(), ciTemplateCategoryVO.getId()),
                "error.pipeline.category.exist");
        CiTemplateCategoryDTO ciTemplateCategoryDTO = ciTemplateCategoryBusMapper.selectByPrimaryKey(ciTemplateCategoryVO.getId());
        AssertUtils.notNull(ciTemplateCategoryDTO, "error.ci.template.category.not.exist");
        AssertUtils.isTrue(!ciTemplateCategoryDTO.getBuiltIn(), "error.update.builtin.ci.template.category");
        BeanUtils.copyProperties(ciTemplateCategoryVO, ciTemplateCategoryDTO);
        ciTemplateCategoryBusMapper.updateByPrimaryKeySelective(ciTemplateCategoryDTO);
        return ConvertUtils.convertObject(ciTemplateCategoryBusMapper.selectByPrimaryKey(ciTemplateCategoryVO.getId()), CiTemplateCategoryVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplateCategory(Long ciTemplateCategoryId) {
        CiTemplateCategoryDTO ciTemplateCategoryDTO = ciTemplateCategoryBusMapper.selectByPrimaryKey(ciTemplateCategoryId);
        if (ciTemplateCategoryDTO == null) return;
        AssertUtils.isTrue(!ciTemplateCategoryDTO.getBuiltIn(), "error.delete.builtin.ci.template.category");
        ciTemplateCategoryBusMapper.deleteByPrimaryKey(ciTemplateCategoryDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiTemplateCategoryVO createTemplateCategory(CiTemplateCategoryVO ciTemplateCategoryVO) {
        AssertUtils.notNull(ciTemplateCategoryVO, "error.ci.template.category.null");
        AssertUtils.isTrue(checkCategoryName(ciTemplateCategoryVO.getCategory()), "error.pipeline.category.exist");
        CiTemplateCategoryDTO ciTemplateCategoryDTO = new CiTemplateCategoryDTO();
        BeanUtils.copyProperties(ciTemplateCategoryVO, ciTemplateCategoryDTO);
        ciTemplateCategoryDTO.setBuiltIn(Boolean.FALSE);
        // 图标使用其他的
        CiTemplateCategoryDTO templateCategoryDTO = ciTemplateCategoryBusMapper.selectOne(new CiTemplateCategoryDTO().setBuiltIn(true).setCategory("其他"));
        ciTemplateCategoryDTO.setImage(templateCategoryDTO == null ? null : templateCategoryDTO.getImage());
        if (ciTemplateCategoryBusMapper.insertSelective(ciTemplateCategoryDTO) != 1) {
            throw new CommonException("error.create.template.category");
        }
        return ConvertUtils.convertObject(ciTemplateCategoryDTO, CiTemplateCategoryVO.class);
    }

    @Override
    public Boolean checkTemplateCategoryName(Long sourceId, String name, Long ciTemplateCategoryId) {
        return ciTemplateCategoryBusMapper.checkTemplateCategoryName(sourceId, name, ciTemplateCategoryId) != null ?
                Boolean.FALSE : Boolean.TRUE;
    }

    @Override
    public List<CiTemplateCategoryVO> queryTemplateCategorys(Long sourceId) {
        List<CiTemplateCategoryDTO> ciTemplateCategoryDTOS = ciTemplateCategoryBusMapper.selectAll();
        if (CollectionUtils.isEmpty(ciTemplateCategoryDTOS)) {
            return Collections.EMPTY_LIST;
        }
        List<CiTemplateCategoryVO> ciTemplateCategoryVOS = ConvertUtils.convertList(ciTemplateCategoryDTOS, CiTemplateCategoryVO.class);
        //排序
        List<CiTemplateCategoryVO> templateCategoryVOS = sortedTemplateCategorys(ciTemplateCategoryVOS);
        return templateCategoryVOS;
    }

    private List<CiTemplateCategoryVO> sortedTemplateCategorys(List<CiTemplateCategoryVO> ciTemplateCategoryVOS) {
        List<CiTemplateCategoryVO> templateCategoryVOS = new ArrayList<>();
        //自定义的放在最后
        List<CiTemplateCategoryVO> customTemplate = ciTemplateCategoryVOS.stream().filter(ciTemplateCategoryVO -> !ciTemplateCategoryVO.getBuiltIn()).collect(Collectors.toList());
        List<CiTemplateCategoryVO> otherVos = ciTemplateCategoryVOS.stream()
                .filter(CiTemplateCategoryVO::getBuiltIn)
                .filter(CiTemplateCategoryVO -> StringUtils.equalsIgnoreCase(CiTemplateCategoryVO.getCategory(), "其他"))
                .collect(Collectors.toList());
        List<CiTemplateCategoryVO> templateCategoryVOS1 = ciTemplateCategoryVOS.stream().filter(CiTemplateCategoryVO::getBuiltIn)
                .filter(CiTemplateCategoryVO -> !StringUtils.equalsIgnoreCase(CiTemplateCategoryVO.getCategory(), "其他"))
                .collect(Collectors.toList());

        templateCategoryVOS.addAll(templateCategoryVOS1);
        templateCategoryVOS.addAll(otherVos);
        templateCategoryVOS.addAll(customTemplate);
        return templateCategoryVOS;
    }

    private Boolean checkCategoryName(String name) {
        CiTemplateCategoryDTO record = new CiTemplateCategoryDTO();
        record.setCategory(name);
        return CollectionUtils.isEmpty(ciTemplateCategoryBusMapper.select(record)) ? Boolean.TRUE : Boolean.FALSE;
    }
}
