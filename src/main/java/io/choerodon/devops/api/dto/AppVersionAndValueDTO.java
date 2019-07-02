package io.choerodon.devops.api.dto;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:55 2019/7/2
 * Description:
 */
public class AppVersionAndValueDTO {
    private String values;
    private String image;
    private String readMeValue;
    private String repository;
    private String version;
    private ProjectConfigDTO harbor;
    private ProjectConfigDTO chart;

    public ProjectConfigDTO getHarbor() {
        return harbor;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getReadMeValue() {
        return readMeValue;
    }

    public void setReadMeValue(String readMeValue) {
        this.readMeValue = readMeValue;
    }

    public void setHarbor(ProjectConfigDTO harbor) {
        this.harbor = harbor;
    }

    public ProjectConfigDTO getChart() {
        return chart;
    }

    public void setChart(ProjectConfigDTO chart) {
        this.chart = chart;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
