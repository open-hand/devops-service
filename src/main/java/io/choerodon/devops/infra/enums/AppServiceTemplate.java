package io.choerodon.devops.infra.enums;

import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;

public enum AppServiceTemplate {
    MICROSERVICE_TEMPLATE("MicroService"),
    MICROSERVICE_FRONT_TEMPLATE("MicroServiceFront"),
    JAVALIB_TEMPLATE("JavaLib"),
    SPRINGBOOT_TEMPLATE("SpringBoot"),
    GO_TEMPLATE("goTemplate"),
    CHOERODON_MOCHA_TEMPLATE("choerodonMochaTemplate");

    private static final String MICROSERVICE_PATH = "choerodon-microservice-template";
    private static final String MICROSERVICE_FRONT_PATH = "choerodon-front-template";
    private static final String JAVALIB_PATH = "choerodon-javalib-template";
    private static final String SPRINGBOOT_PATH = "choerodon-springboot-template";
    private static final String GO_PATH = "choerodon-golang-template";
    private static final String CHOERODON_MOCHA_PATH = "choerodon-mocha-template";

    private String templateName;

    AppServiceTemplate(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public static HashMap<String, String> templatePath = new HashMap<>(6);

    static {
        templatePath.put(AppServiceTemplate.MICROSERVICE_TEMPLATE.templateName, MICROSERVICE_PATH);
        templatePath.put(AppServiceTemplate.MICROSERVICE_FRONT_TEMPLATE.templateName, MICROSERVICE_FRONT_PATH);
        templatePath.put(AppServiceTemplate.JAVALIB_TEMPLATE.templateName, JAVALIB_PATH);
        templatePath.put(AppServiceTemplate.SPRINGBOOT_TEMPLATE.templateName, SPRINGBOOT_PATH);
        templatePath.put(AppServiceTemplate.GO_TEMPLATE.templateName, GO_PATH);
        templatePath.put(AppServiceTemplate.CHOERODON_MOCHA_TEMPLATE.templateName, CHOERODON_MOCHA_PATH);
    }
}
