import { axios } from '@choerodon/boot';

export default ({ projectId, issueNameOptionDs, selectedApp, formatMessage, searchDS, contentStore }) => {
  async function checkBranchName(value) {
    const endWith = /(\/|\.|\.lock)$/;
    const contain = /(\s|~|\^|:|\?|\*|\[|\\|\.\.|@\{|\/{2,}){1}/;
    const single = /^@+$/;
    let mess = true;
    const prefix = contentStore.getBranchPrefix;
    let branchName = '';
    if (prefix) {
      branchName = `${prefix}-${value}`;
    } else {
      branchName = value;
    }
    if (endWith.test(branchName)) {
      mess = formatMessage({ id: 'branch.checkNameEnd' });
    } else if (contain.test(branchName) || single.test(branchName)) {
      mess = formatMessage({ id: 'branch.check' });
    }
    await axios.get(`/devops/v1/projects/${projectId}/app_service/${selectedApp}/git/check_branch_name?branch_name=${branchName}`)
      .then((res) => {
        if (res && res.failed) {
          mess = res.message;
        }
      });
    return mess;
  }
  return {
    autoCreate: true,
    autoQuery: false,
    selection: false,
    paging: false,
    dataKey: null,
    fields: [
      {
        name: 'issueName',
        type: 'string',
        textField: 'summary',
        valueField: 'issueId',
        label: formatMessage({ id: 'branch.issueName' }),
        options: issueNameOptionDs,
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
        url: `/devops/v1/projects/${projectId}/app_service/${selectedApp}/git/branch`,
        method: 'post',
        transformRequest: () => {
          const issueId = data.issueName;
          const originBranch = data.branchOrigin;
          const type = data.branchType;
          let branchName;
          type === 'custom' ? branchName = data.branchName : branchName = `${data.branchType}-${data.branchName}`;
          const postData = {
            branchName,
            issueId,
            originBranch,
            type,
          };
          return JSON.stringify(postData);
        },
      }),
    },
    events: {
    },
  };
};
