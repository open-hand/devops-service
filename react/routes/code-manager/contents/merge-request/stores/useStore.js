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
      axios.get('base/v1/users/self').then((data) => {
        this.setUserId(data.id);
      });
    },

    count: {
      closeCount: 0,
      mergeCount: 0,
      openCount: 0,
      totalCount: 0,
    },
    setCount(data) {
      this.count = data;
    },
    get getCount() {
      return this.count;
    },

    assigneeCount: 0,
    setAssigneeCount(assigneeCount) {
      this.assigneeCount = assigneeCount;
    },
    get getAssigneeCount() {
      return this.assigneeCount;
    },

    assignee: {},
    setAssignee(value) {
      this.assignee = value;
    },
    get getAssignee() {
      return this.assignee.slice();
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

  }));
}
