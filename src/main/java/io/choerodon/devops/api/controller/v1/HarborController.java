package io.choerodon.devops.api.controller.v1;

import org.codehaus.groovy.runtime.dgmimpl.arrays.IntegerArrayGetAtMetaMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import io.choerodon.devops.app.eventhandler.payload.HarborPayload;
import io.choerodon.devops.app.service.DevopsConfigService;
import io.choerodon.devops.app.service.DevopsHarborUserService;
import io.choerodon.devops.app.service.HarborService;
import io.choerodon.devops.app.service.impl.HarborServiceImpl;
import io.choerodon.devops.infra.dto.HarborUserDTO;
import io.choerodon.devops.infra.dto.harbor.User;

/**
 * @author: 25499
 * @date: 2019/10/23 14:15
 * @description:
 */
@Controller
@RequestMapping("/harbor")
public class HarborController {
    @Autowired
    private DevopsHarborUserService devopsHarborUserService;
    @Autowired
    private HarborServiceImpl harborService;

    @Autowired
    private DevopsConfigService devopsConfigService;
    @RequestMapping("/test")
    @ResponseBody
    public long test(){
       return devopsHarborUserService.create(new HarborUserDTO("a","a", "a",true));
    }

    @RequestMapping("/create/{a}")
    @ResponseBody
    public void create(@PathVariable("a") Boolean b ){


//        devopsConfigService.test(1L,b);
    }
    @RequestMapping("/cassi")
    @ResponseBody
    public String cassi(){
        HarborPayload harborPayload = new HarborPayload(1L,
                "cmcc" + "-" +"fssc-taxp"
        );
        harborService.createHarborForProject(harborPayload);
      return "ok";
    }

//    @RequestMapping("/haas")
//    @ResponseBody
//    public String haas(){
//        HarborUserDTO harborUserDTO = new HarborUserDTO();
//        harborUserDTO.setHarborProjectUserPassword("111");
//        harborUserDTO.setHarborProjectUserName("111");
//        harborUserDTO.setHarborProjectUserEmail("111");
//        devopsHarborUserService.queryHarborUserById(harborUserDTO);
//        return "ok"+harborUserDTO.getId();
//    }

}
