package io.choerodon.devops.api.controller.v1;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.runtime.dgmimpl.arrays.IntegerArrayGetAtMetaMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import io.choerodon.devops.api.vo.DevopsConfigRepVO;
import io.choerodon.devops.api.vo.DevopsConfigVO;
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

    @RequestMapping("/update/{a}")
    @ResponseBody
    public void update(@PathVariable("a") Boolean b ){
        List<DevopsConfigVO> devopsConfigVOS = new ArrayList<>();

        List<DevopsConfigVO> list = new ArrayList<>();
        DevopsConfigVO harbor = new DevopsConfigVO();
        harbor.setCustom(false);
        harbor.setHarborPrivate(b);
        harbor.setType("harbor");
        list.add(harbor);
//        devopsConfigService.test(1L,"project",list);
    }
    @RequestMapping("/create")
    @ResponseBody
    public String create(){
        HarborPayload harborPayload = new HarborPayload(1L,
                "cmcc" + "-" +"fssc-taxp"
        );
        //创建
        harborService.createHarborForProject(harborPayload);
      return "ok";
    }
    @RequestMapping("/customer/{a}")
    @ResponseBody
    public void customer(@PathVariable("a") Boolean b , @RequestBody DevopsConfigRepVO devopsConfigRepVO){
        List<DevopsConfigVO> devopsConfigVOS = new ArrayList<>();

        List<DevopsConfigVO> list = new ArrayList<>();
        DevopsConfigVO harbor = new DevopsConfigVO();
        harbor.setType("harbor");
        devopsConfigRepVO.getHarbor().setHarborPrivate(true);
        list.add(devopsConfigRepVO.getHarbor());
//        devopsConfigService.test(1L,"project",list);
    }

}
