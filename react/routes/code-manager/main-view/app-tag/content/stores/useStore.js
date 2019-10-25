import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/boot';
import { handlePromptError } from '../../../../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    branchData: [],
    branchTotal: 0,
    queryBranchData({ projectId, sorter = { field: 'createDate', order: 'asc' }, postData = { searchParam: {}, param: '' }, size = 3, appServiceId }) {
      axios.post(`/devops/v1/projects/${projectId}/app_service/${appServiceId}/git/page_branch_by_options?page=1&size=${size}`, JSON.stringify(postData)).then((data) => {
        if (handlePromptError(data)) {
          this.branchData = data.list || [];
          this.branchTotal = data.total;
        }
      });
    },
    checkTagName(projectId, name, appServiceId) { return axios.get(`/devops/v1/projects/${projectId}/app_service/${appServiceId}/git/check_tag?tag_name=${name}`); },
    deleteTag(projectId, tag, appServiceId) { return axios.delete(`/devops/v1/projects/${projectId}/app_service/${appServiceId}/git/tags?tag=${tag}`); },
    editTag(projectId, tag, release, appServiceId) {
      return axios.put(`/devops/v1/projects/${projectId}/app_service/${appServiceId}/git/tags?tag=${tag}`, release); 
    },
  }));
}
