import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';
import map from 'lodash/map';
import { handlePromptError } from '../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    /**
     * 强制失败流水线
     * @param projectId
     * @param id
     * @returns {*}
     */
    manualStop(projectId, id) {
      return axios.get(`/devops//v1/projects/${projectId}/pipeline/failed?pipeline_record_id=${id}`);
    },

    /**
     ** 流水线重试
     * @param projectId
     * @param id 执行记录id
     */
    retry(projectId, id) {
      return axios.get(`/devops/v1/projects/${projectId}/pipeline/${id}/retry`);
    },

    /**
     ** 人工审核阶段或任务
     * @param projectId
     * @param data
     */
    checkData(projectId, data) {
      return axios.post(`/devops/v1/projects/${projectId}/pipeline/audit`, JSON.stringify(data));
    },

    /**
     ** 人工审核预检，判断是否可以审核
     * @param projectId
     * @param data
     */
    canCheck(projectId, data) {
      return axios.post(`/devops/v1/projects/${projectId}/pipeline/check_audit`, JSON.stringify(data));
    },
  }));
}
