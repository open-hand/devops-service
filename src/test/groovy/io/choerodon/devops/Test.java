package io.choerodon.devops;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.util.FileUtil;

/**
 * @author: 25499
 * @date: 2019/9/5 9:15
 * @description:
 */
public class Test {
    public static void main(String[] args) {
//        File file = new File("D:\\ideaWork\\devops-service");
        File file = new File("C:\\Users\\lzz\\Desktop\\choerodon-springboot-template-master");
        Map<String, String> params = new HashMap<>();
        String group = "choerodon-trainning";
        String service = "hello-service";

            params.put("{{group.name}}", group);
            params.put("{{service.code}}", service);
            params.put("the-oldService-name", "devops-service");
            params.put("devops-service", service);
//            params.put("devops", service);
            FileUtil.replaceReturnFile(file, params);
    }
//    private void replaceParams(AppServiceDTO applicationDTO,
//                               ProjectDTO projectDTO,
//                               OrganizationDTO organizationDTO,
//                               String applicationDir) {
//        try {
//            File file = new File(gitUtil.getWorkingDirectory(applicationDir));
//            Map<String, String> params = new HashMap<>();
//            params.put("{{group.name}}", organizationDTO.getCode() + "-" + projectDTO.getCode());
//            params.put("{{service.code}}", applicationDTO.getCode());
//            FileUtil.replaceReturnFile(file, params);
//        } catch (Exception e) {
//            //删除模板
//            gitUtil.deleteWorkingDirectory(applicationDir);
//            throw new CommonException(e.getMessage(), e);
//        }
//    }
}
