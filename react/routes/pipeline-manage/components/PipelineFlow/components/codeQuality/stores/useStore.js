import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/boot';
import { handlePromptError } from '../../../../../../../utils';

const data = {
  status: 'OK',
  date: '2020-04-07T07:41:52+0000',
  mode: 'previous_version',
  parameter: '2020.4.7-153957-master',
  sonarContents: [{
    key: 'new_code_smells',
    value: '0',
    rate: null,
    url: 'https://sonarqube.choerodon.com.cn/project/issues?id=hzero-c7ncd:devops-service&resolved=false&sinceLeakPeriod=true&types=CODE_SMELL',
  }, {
    key: 'new_coverage',
    value: '0.0',
    rate: null,
    url: 'https://sonarqube.choerodon.com.cn/component_measures?id=hzero-c7ncd:devops-service&metric=new_coverage',
  }, {
    key: 'new_technical_debt',
    value: '0',
    rate: null,
    url: 'https://sonarqube.choerodon.com.cn/project/issues?facetMode=effort&id=hzero-c7ncd:devops-service&resolved=false&sinceLeakPeriod=true&types=CODE_SMELL',
  }, {
    key: 'duplicated_blocks',
    value: '475',
    rate: null,
    url: 'https://sonarqube.choerodon.com.cn/component_measures?id=hzero-c7ncd:devops-service&metric=duplicated_blocks',
  }, {
    key: 'coverage',
    value: '0.0',
    rate: null,
    url: 'https://sonarqube.choerodon.com.cn/component_measures?id=hzero-c7ncd:devops-service&metric=coverage',
  }, {
    key: 'bugs',
    value: '0',
    rate: 'A',
    url: 'https://sonarqube.choerodon.com.cn/project/issues?id=hzero-c7ncd:devops-service&resolved=false&types=BUG',
  }, {
    key: 'duplicated_lines_density',
    value: '9.9',
    rate: 'B',
    url: 'https://sonarqube.choerodon.com.cn/component_measures?id=hzero-c7ncd:devops-service&metric=duplicated_lines_density',
  }, {
    key: 'sqale_index',
    value: '24d',
    rate: null,
    url: 'https://sonarqube.choerodon.com.cn/project/issues?facetMode=effort&id=hzero-c7ncd:devops-service&resolved=false&types=CODE_SMELL',
  }, {
    key: 'ncloc',
    value: '84.4K',
    rate: 'M',
    url: null,
  }, {
    key: 'new_bugs',
    value: '0',
    rate: 'A',
    url: 'https://sonarqube.choerodon.com.cn/project/issues?id=hzero-c7ncd:devops-service&resolved=false&sinceLeakPeriod=true&types=BUG',
  }, {
    key: 'ncloc_language_distribution',
    value: 'java=84117;xml=270',
    rate: null,
    url: null,
  }, {
    key: 'code_smells',
    value: '780',
    rate: null,
    url: 'https://sonarqube.choerodon.com.cn/project/issues?id=hzero-c7ncd:devops-service&resolved=false&types=CODE_SMELL',
  }, {
    key: 'new_duplicated_lines_density',
    value: '0',
    rate: null,
    url: 'https://sonarqube.choerodon.com.cn/component_measures?id=hzero-c7ncd:devops-service&metric=new_duplicated_lines_density',
  }, {
    key: 'vulnerabilities',
    value: '1',
    rate: 'E',
    url: 'https://sonarqube.choerodon.com.cn/project/issues?id=hzero-c7ncd:devops-service&resolved=false&types=VULNERABILITY',
  }, {
    key: 'new_vulnerabilities',
    value: '0',
    rate: 'A',
    url: 'https://sonarqube.choerodon.com.cn/project/issues?id=hzero-c7ncd:devops-service&resolved=false&sinceLeakPeriod=true&types=VULNERABILITY',
  }],
};

export default function useStore() {
  return useLocalStore(() => ({
    data: null,
    loading: false,
    loadCodeQualityData(projectId, appServeiceId) {
      this.loading = true;
      axios.get(`/devops/v1/projects/${projectId}/app_service/${appServeiceId}/sonarqube`).then((res) => {
        if (res && !res.failed) {
          this.data = res;
          this.loading = false;
        }
      });
    },
  }));
}
