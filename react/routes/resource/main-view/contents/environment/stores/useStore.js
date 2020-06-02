import { useLocalStore } from 'mobx-react-lite';
import { axios, Choerodon } from '@choerodon/boot';
import checkPermission from '../../../../../../utils/checkPermission';
import { handlePromptError } from '../../../../../../utils';

export default function useStore({ defaultTab }) {
  return useLocalStore(() => ({
    tabKey: defaultTab,

    setTabKey(data) {
      this.tabKey = data;
    },
    get getTabKey() {
      return this.tabKey;
    },
    hasPermission: false,
    setPermission(data) {
      this.hasPermission = data;
    },
    get getPermission() {
      return this.hasPermission;
    },

    value: '',
    setValue(value) {
      this.value = value;
    },
    get getValue() {
      return this.value;
    },

    hasInstance: false,
    setHasInstance(data) {
      this.hasInstance = data;
    },
    get getHasInstance() {
      return this.hasInstance;
    },

    polarisLoading: true,
    setPolarisLoading(flag) {
      this.polarisLoading = flag;
    },
    get getPolarisLoading() {
      return this.polarisLoading;
    },

    async checkPermission({ projectId, organizationId, resourceType }) {
      const res = await checkPermission({
        code: 'choerodon.code.project.deploy.app-deployment.resource.ps.permission',
        organizationId,
        projectId,
        resourceType,
      });
      this.setPermission(res);
    },

    async loadValue(projectId, id) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/app_service_versions/value?app_service_id=${id}`);
        if (handlePromptError(res)) {
          this.setValue(res);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },

    checkDelete(projectId, id) {
      return axios.get(`/devops/v1/projects/${projectId}/deploy_value/check_delete?value_id=${id}`);
    },
    deleteRecord(projectId, id) {
      return axios.delete(`/devops/v1/projects/${projectId}/deploy_value?value_id=${id}`);
    },

    async checkHasInstance(projectId, envId) {
      try {
        this.setPolarisLoading(true);
        const res = await axios.get(`devops/v1/projects/${projectId}/app_service_instances/count_by_options?env_id=${envId}&status=&app_service_id=`);
        const result = handlePromptError(res);
        this.setHasInstance(result);
        this.setPolarisLoading(false);
        return result;
      } catch (e) {
        Choerodon.handleResponseError(e);
        this.setPolarisLoading(false);
        return false;
      }
    },

    async ManualScan(projectId, envId) {
      try {
        const res = await axios.post(`/devops/v1/projects/${projectId}/polaris/envs/${envId}`);
        const result = handlePromptError(res);
        return result;
      } catch (e) {
        Choerodon.handleResponseError(e);
        return false;
      }
    },
  }));
}
