import { axios } from '@choerodon/boot';

export default class HostConfigApi {
  static axiosCheckName(projectId: number, name: string) {
    return axios.get('');
  }

  static getLoadHostsDetailsUrl(projectId:number) {
    return `devops/v1/projects/${projectId}/hosts/page_by_options`;
  }
}
