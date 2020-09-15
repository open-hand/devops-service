import { axios } from '@choerodon/boot';

export default class HostConfigApi {
  static axiosCheckName(projectId: number, name: string) {
    return axios.get('');
  }
}
