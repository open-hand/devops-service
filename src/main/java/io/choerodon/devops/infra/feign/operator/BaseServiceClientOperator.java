package io.choerodon.devops.infra.feign.operator;

import io.choerodon.devops.infra.dto.iam.ApplicationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.BaseServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Create by Li Jinyan on 2019/8/13
 */

@Component
public class BaseServiceClientOperator {

    @Autowired
    private BaseServiceClient baseServiceClient;

    public ProjectDTO queryProjectByApp(Long id){
        try{
            return baseServiceClient.queryProjectByApp(id).getBody();
        }catch(Exception e){
            return null;
        }
    }

    public ApplicationDTO getAppById(Long id){
        try{
            return baseServiceClient.getAppById(id).getBody();
        }catch (Exception e){
            return null;
        }
    }
}
