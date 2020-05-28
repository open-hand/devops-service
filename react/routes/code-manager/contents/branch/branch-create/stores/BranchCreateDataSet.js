import { axios } from '@choerodon/boot';

export default ({ projectId, appServiceId, formatMessage, contentStore }) => {
  async function checkBranchName(value) {
    const endWith = /(\/|\.|\.lock)$/;
    const contain = /(\s|~|\^|:|\?|\*|\[|\\|\.\.|@\{|\/{2,}){1}/;
    const single = /^@+$/;
    let mess = true;
    const prefix = contentStore.getBranchPrefix;
    let branchName = '';
    if (prefix) {
      branchName = `${prefix}${value}`;
    } else {
      branchName = value;
    }
    if (endWith.test(branchName)) {
      mess = formatMessage({ id: 'branch.checkNameEnd' });
    } else if (contain.test(branchName) || single.test(branchName)) {
      mess = formatMessage({ id: 'branch.check' });
    } else {
      await axios.get(`/devops/v1/projects/${projectId}/app_service/${appServiceId}/git/check_branch_name?branch_name=${branchName}`)
        .then((res) => {
          if ((res && res.failed) || !res) {
            mess = formatMessage({ id: 'branch.check.existence' });
          }
        });
    }
    return mess;
  }
  return {
    autoCreate: true,
    fields: [
      {
        name: 'issue',
        type: 'object',
        textField: 'summary',
        valueField: 'issueId',
        label: formatMessage({ id: 'branch.issueName' }),
        lookupUrl: `/agile/v1/projects/${projectId}/issues/summary?onlyActiveSprint=false&self=true`,
      },
      {
        name: 'branchOrigin',
        type: 'string',
        label: formatMessage({ id: 'branch.source' }),
        required: true,
      },
      {
        name: 'branchType',
        type: 'string',
        required: true,
        label: formatMessage({ id: 'branch.type' }),
      },
      {
        name: 'branchName',
        label: formatMessage({ id: 'branch.name' }),
        required: true,
        type: 'string',
        defaultValue: '',
        validator: checkBranchName,
      },
    ],
    transport: {
      create: ({ data: [data] }) => ({
        url: `/devops/v1/projects/${projectId}/app_service/${appServiceId}/git/branch`,
        method: 'post',
        transformRequest: () => {
          const { issueId } = data.issue || {};
          const originBranch = data.branchOrigin;
          const type = data.branchType;
          const branchName = type === 'custom' ? data.branchName : `${data.branchType}-${data.branchName}`;

          const postData = {
            branchName,
            issueId,
            originBranch: originBranch && originBranch.slice(0, -7),
            type,
          };
          return JSON.stringify(postData);
        },
      }),
    },
  };
};
