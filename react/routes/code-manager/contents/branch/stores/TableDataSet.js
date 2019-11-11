// 需要替换
import getTablePostData from '../../../../../utils/getTablePostData';

export default ({ projectId, formatMessage, appServiceId }) => ({
  selection: false,
  paging: true,
  queryFields: [
    {
      name: 'branchName',
      type: 'string',
      label: formatMessage({ id: 'branch.name' }),
    },
  ],
  fields: [
    {
      name: 'branchName',
      type: 'string',
      label: formatMessage({ id: 'branch.name' }),
    },
    {
      name: 'commitContent',
      type: 'string',
      label: formatMessage({ id: 'branch.commit' }),
    },
    {
      name: 'createUserRealName',
      type: 'string',
      label: formatMessage({ id: 'branch.time' }),
    },
    {
      name: 'issueName',
      type: 'string',
      label: formatMessage({ id: 'branch.issue' }),
    },
  ],
  transport: {
    read: ({ data }) => ({
      url: `devops/v1/projects/${projectId}/app_service/${appServiceId}/git/page_branch_by_options`,
      method: 'post',
      data: JSON.stringify(getTablePostData(data)),
    }),
    destroy: ({ data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/app_service/${appServiceId}/git/branch?branch_name=${data.branchName}`,
      method: 'delete',
    }),
  },
});
