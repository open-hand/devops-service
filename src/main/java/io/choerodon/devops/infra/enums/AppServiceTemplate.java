package io.choerodon.devops.infra.enums;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum AppServiceTemplate {
    MICROSERVICE_TEMPLATE("MicroService"),
    MICROSERVICE_FRONT_TEMPLATE("MicroServiceFront"),
    JAVALIB_TEMPLATE("JavaLib"),
    SPRINGBOOT_TEMPLATE("SpringBoot"),
    GO_TEMPLATE("goTemplate"),
    CHOERODON_MOCHA_TEMPLATE("choerodonMochaTemplate"),
    CHOERODON_TESTNG_TEMPLATE("choerodonTestngTemplate"),
    CHOERODON_TESTNG_SELENIUM_TEMPLATE("choerodonTestngSeleniumTemplate");
    private static final String TEMPLATE_URL = "https://github.com/choerodon/";
    private static final String FORMAT_MODAL = "%s%s";
    private static final String MICROSERVICE_PATH = "choerodon-microservice-template.git?version=0.14.1";
    private static final String MICROSERVICE_FRONT_PATH = "choerodon-front-template.git?version=0.21.0";
    private static final String JAVALIB_PATH = "choerodon-javalib-template.git?version=0.21.0";
    private static final String SPRINGBOOT_PATH = "choerodon-springboot-template.git?version=0.14.0";
    private static final String GO_PATH = "choerodon-golang-template.git?version=0.22.0";
    private static final String CHOERODON_MOCHA_PATH = "choerodon-mocha-template.git?version=0.14.0";
    private static final String CHOERODON_TESTNG_TEMPLATE_PATH = "choerodon-testng-template.git?version=0.20.0";
    private static final String CHOERODON_TESTNG_SELENIUM_TEMPLATE_PATH = "choerodon-testng-selenium-template.git?version=0.20.0";

    private String templateName;

    AppServiceTemplate(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateName() {
        return templateName;
    }

    private static final Map<String, String> namePathMap;

    static {
        Map<String, String> templatePath = new HashMap<>(8);
        templatePath.put(AppServiceTemplate.MICROSERVICE_TEMPLATE.templateName, String.format(FORMAT_MODAL, TEMPLATE_URL, MICROSERVICE_PATH));
        templatePath.put(AppServiceTemplate.MICROSERVICE_FRONT_TEMPLATE.templateName, String.format(FORMAT_MODAL, TEMPLATE_URL, MICROSERVICE_FRONT_PATH));
        templatePath.put(AppServiceTemplate.JAVALIB_TEMPLATE.templateName, String.format(FORMAT_MODAL, TEMPLATE_URL, JAVALIB_PATH));
        templatePath.put(AppServiceTemplate.SPRINGBOOT_TEMPLATE.templateName, String.format(FORMAT_MODAL, TEMPLATE_URL, SPRINGBOOT_PATH));
        templatePath.put(AppServiceTemplate.GO_TEMPLATE.templateName, String.format(FORMAT_MODAL, TEMPLATE_URL, GO_PATH));
        templatePath.put(AppServiceTemplate.CHOERODON_MOCHA_TEMPLATE.templateName, String.format(FORMAT_MODAL, TEMPLATE_URL, CHOERODON_MOCHA_PATH));
        templatePath.put(AppServiceTemplate.CHOERODON_TESTNG_TEMPLATE.templateName, String.format(FORMAT_MODAL, TEMPLATE_URL, CHOERODON_TESTNG_TEMPLATE_PATH));
        templatePath.put(AppServiceTemplate.CHOERODON_TESTNG_SELENIUM_TEMPLATE.templateName, String.format(FORMAT_MODAL, TEMPLATE_URL, CHOERODON_TESTNG_SELENIUM_TEMPLATE_PATH));
        namePathMap = Collections.unmodifiableMap(templatePath);
    }

    public static Map<String, String> getTemplatePath() {
        return namePathMap;
    }
}
