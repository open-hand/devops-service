import { useLocalStore } from 'mobx-react-lite';
import { axios, Choerodon } from '@choerodon/boot';

export default function useStore() {
  return useLocalStore(() => ({
    defaultImage: '',

    axiosGetDefaultImage() {
      return axios.get('/devops/ci/default_image');
    },

    setDefaultImage(data) {
      this.defaultImage = data;
    },

    get getDefaultImage() {
      return this.defaultImage;
    },

    currentAppService: {},
    get getCurrentAppService() {
      return this.currentAppService;
    },
    setCurrentAppService(data) {
      this.currentAppService = data;
    },

    searchAppServiceData: [],
    get getSearchAppServiceData() {
      return this.searchAppServiceData;
    },
    setSearchAppServiceData(data) {
      this.searchAppServiceData = data;
    },

    axiosCreatePipeline(data, projectId) {
      return axios.post(`/devops/v1/projects/${projectId}/cicd_pipelines`, data);
    },
  }));
}
