package io.choerodon.devops.api.dto;

        import java.util.Date;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:27 2019/4/19
 * Description:
 */
public class PipelineRecordListDTO {
    private Long id;
    private Date creationTime;

    public PipelineRecordListDTO(Long id, Date creationTime) {
        this.id = id;
        this.creationTime = creationTime;
    }
}
