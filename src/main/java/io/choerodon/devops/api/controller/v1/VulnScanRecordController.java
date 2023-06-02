package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.vuln.VulnTargetVO;
import io.choerodon.devops.app.service.VulnScanRecordService;
import io.choerodon.swagger.annotation.Permission;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/6/1 17:01
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/vuln_scan_records")
public class VulnScanRecordController {

    @Autowired
    private VulnScanRecordService vulnScanRecordService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("查询漏洞扫描详情")
    @GetMapping("/{record_id}/details")
    public ResponseEntity<List<VulnTargetVO>> queryDetailsById(
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @PathVariable(value = "record_id") Long recordId,
            @RequestParam(value = "pkg_name", required = false) String pkgName,
            @RequestParam(value = "severity", required = false) String severity,
            @RequestParam(value = "param", required = false) String param) {
        return ResponseEntity.ok(vulnScanRecordService.queryDetailsById(projectId, recordId, pkgName, severity, param));
    }
}
