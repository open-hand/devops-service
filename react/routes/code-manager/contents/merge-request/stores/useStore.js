import { useLocalStore } from 'mobx-react-lite';
import { axios, Choerodon } from '@choerodon/boot';

export default function useStore() {
  return useLocalStore(() => ({
    id: null,

    setUserId(id) {
      this.id = id;
    },
    get getUserId() {
      return this.id;
    },
    async loadUser() {
      axios.get('iam/choerodon/v1/users/self').then((data) => {
        this.setUserId(data.id);
      });
    },

    count: {
      closeCount: 0,
      mergeCount: 0,
      openCount: 0,
      totalCount: 0,
      auditCount: 0,
    },
    setCount(data) {
      this.count = data;
    },
    get getCount() {
      return this.count;
    },

    tabKey: 'opened',
    setTabKey(key) {
      this.tabKey = key;
    },
    get getTabKey() {
      return this.tabKey;
    },

    url: '',
    setUrl(data) {
      this.url = data;
    },

    isEmpty: false,
    setIsEmpty(flag) {
      this.isEmpty = flag;
    },
    get getIsEmpty() {
      return this.isEmpty;
    },

    loadUrl(projectId, appId) {
      return axios.get(`/devops/v1/projects/${projectId}/app_service/${appId}/git/url`)
        .then((data) => {
          if (data && data.failed) {
            Choerodon.prompt(data.message);
          } else {
            this.setUrl(data);
          }
        });
    },
    get getUrl() {
      return this.url;
    },

    checkMerge(projectId, appServiceId) {
      return axios.get(`/devops/v1/projects/${projectId}/member-check/${appServiceId}?type=MERGE_REQUEST_CREATE`);
    },

  }));
}
