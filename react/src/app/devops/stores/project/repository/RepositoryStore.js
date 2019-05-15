import { observable, action, computed } from 'mobx';
import {axios, store, stores} from '@choerodon/boot';
import { handleProptError } from '../../../utils';

const orderMapping = {
  ascend: 'asc',
  descend: 'desc',
};
const HEIGHT = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;

@store('RepositoryStore')
class RepositoryStore {
  @observable repoData = [];

  @observable loading = true;

  @observable pageInfo = {
    current: 1,
    pageSize: HEIGHT <= 900 ? 10 : 15,
    total: 0,
  };

  @action setRepoData(data) {
    this.repoData = data;
  }

  @computed get getRepoData() {
    return this.repoData.slice();
  }

  @action setLoading(flag) {
    this.loading = flag;
  }

  @computed get getLoading() {
    return this.loading;
  }

  @action setPageInfo(page) {
    this.pageInfo = page;
  }

  @computed get getPageInfo() {
    return this.pageInfo;
  }

  /**
   * 查询仓库数据
   * @param projectId
   * @param page
   * @param pageSize
   * @param sorter
   * @param search
   */
  queryRepoData = (projectId, page, pageSize = this.pageInfo.pageSize, sorter, search) => {
    this.setLoading(true);
    const order = sorter.order ? orderMapping[sorter.order] : 'desc';
    axios.post(`/devops/v1/projects/${projectId}/apps/list_code_repository?page=${page}&size=${pageSize}&sort=${sorter.field || 'id'},${order}`, JSON.stringify(search))
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          this.setRepoData(res.content);
          const pageInfo = {
            current: res.number + 1,
            total: res.totalElements,
            pageSize: res.size,
          };
          this.setPageInfo(pageInfo);
        }
        this.setLoading(false);
      })
      .catch((err) => {
        this.setLoading(false);
        Choerodon.handleResponseError(err);
      });
  }
}


const repositoryStore = new RepositoryStore();
export default repositoryStore;
