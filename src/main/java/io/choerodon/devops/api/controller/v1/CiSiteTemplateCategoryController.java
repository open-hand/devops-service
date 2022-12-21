package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.base.BaseController;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.template.CiTemplateCategoryVO;
import io.choerodon.devops.app.service.CiTemplateCategoryBusService;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * 流水线模板适用语言表(CiTemplateLanguage)表控制层
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:18
 */

@RestController("ciSiteTemplateCategoryController.v1")
@RequestMapping("/v1/site/{source_id}/ci_template_category")
public class CiSiteTemplateCategoryController extends BaseController {

    @Autowired
    private CiTemplateCategoryBusService ciTemplateCategoryBusService;

    @ApiOperation(value = "平台层查询流水线分类")
    @GetMapping
    @Permission(level = ResourceLevel.SITE)
    @CustomPageRequest
    public ResponseEntity<Page<CiTemplateCategoryVO>> pageTemplateCategory(
            @PathVariable(value = "source_id") Long sourceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
            @RequestParam(value = "name", required = false) String name) {
        return ResponseEntity.ok(ciTemplateCategoryBusService.pageTemplateCategory(pageRequest, name));
    }

    @ApiOperation(value = "平台层创建流水线分类")
    @Permission(level = ResourceLevel.SITE)
    @PostMapping
    public ResponseEntity<CiTemplateCategoryVO> createTemplateCategory(
            @PathVariable(value = "source_id") Long sourceId,
            @RequestBody CiTemplateCategoryVO ciTemplateCategoryVO) {
        return ResponseEntity.ok(ciTemplateCategoryBusService.createTemplateCategory(ciTemplateCategoryVO));
    }

    @ApiOperation(value = "平台层查询流水线分类列表")
    @Permission(level = ResourceLevel.SITE)
    @GetMapping("/list")
    public ResponseEntity<List<CiTemplateCategoryVO>> queryTemplateCategorys(
            @PathVariable(value = "source_id") Long sourceId) {
        return ResponseEntity.ok(ciTemplateCategoryBusService.queryTemplateCategorys(sourceId));
    }


    @ApiOperation(value = "平台层修改流水线分类")
    @Permission(level = ResourceLevel.SITE)
    @PutMapping
    public ResponseEntity<CiTemplateCategoryVO> updateTemplateCategory(
            @PathVariable(value = "source_id") Long sourceId,
            @RequestBody CiTemplateCategoryVO ciTemplateCategoryVO) {
        return ResponseEntity.ok(ciTemplateCategoryBusService.updateTemplateCategory(sourceId, ciTemplateCategoryVO));
    }


    @ApiOperation(value = "平台层删除流水线分类")
    @Permission(level = ResourceLevel.SITE)
    @DeleteMapping("/{ci_template_category_id}")
    public ResponseEntity<CiTemplateCategoryVO> deleteTemplateCategory(
            @PathVariable(value = "source_id") Long sourceId,
            @Encrypt @PathVariable("ci_template_category_id") Long ciTemplateCategoryId) {
        ciTemplateCategoryBusService.deleteTemplateCategory(ciTemplateCategoryId);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "平台层校验分类名称是否唯一")
    @Permission(level = ResourceLevel.SITE)
    @GetMapping("/check/name/unique")
    public ResponseEntity<Boolean> checkTemplateCategoryName(
            @PathVariable(value = "source_id") Long sourceId,
            @RequestParam(value = "name", required = false) String name,
            @Encrypt @RequestParam(value = "ci_template_category_id", required = false) Long ciTemplateCategoryId) {
        return ResponseEntity.ok(ciTemplateCategoryBusService.checkTemplateCategoryName(sourceId, name, ciTemplateCategoryId));

    }

}

