package io.choerodon.devops.infra.dto.gitlab;



/**
 * This class contains the sizing information from the project. To get this information,
 * ProjectApi.getProject() has to be called with parameter statistics=true
 * which is only allowed for GitLab admins.
 */
public class ProjectStatistics {

    long commitCount;
    long storageSize;
    long repositorySize;
    long wikiSize;
    long lfsObjectsSize;
    long jobArtifactsSize;

    public long getCommitCount() {
        return commitCount;
    }

    public void setCommitCount(long commitCount) {
        this.commitCount = commitCount;
    }

    public long getStorageSize() {
        return storageSize;
    }

    public void setStorageSize(long storageSize) {
        this.storageSize = storageSize;
    }

    public long getRepositorySize() {
        return repositorySize;
    }

    public void setRepositorySize(long repositorySize) {
        this.repositorySize = repositorySize;
    }

    public long getWikiSize() {
        return wikiSize;
    }

    public void setWikiSize(long wikiSize) {
        this.wikiSize = wikiSize;
    }

    public long getLfsObjectsSize() {
        return lfsObjectsSize;
    }

    public void setLfsObjectsSize(long lfsObjectsSize) {
        this.lfsObjectsSize = lfsObjectsSize;
    }

    public long getJobArtifactsSize() {
        return jobArtifactsSize;
    }

    public void setJobArtifactsSize(long jobArtifactsSize) {
        this.jobArtifactsSize = jobArtifactsSize;
    }

}
