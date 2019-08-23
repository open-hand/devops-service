import getTablePostData from '../../../../../../utils/getTablePostData';

export default ({ formatMessage, intlPrefix, projectId }) => ({
  selection: false,
  pageSize: 10,
  transport: {
    read: ({ data }) => {
      const postData = getTablePostData(data);
      return {
        url: `/devops/v1/projects/${projectId}/clusters/page_projects`,
        method: 'post',
        data: postData,
      };
    },
    destroy: {
      url: `/devops/v1/projects/${projectId}/clusters/clusterId/permission`,
      method: 'delete',
    },
  },
  fields: [
    {
      name: 'name',
      type: 'string',
      label: formatMessage({ id: 'cluster.project.name' }),
    },
    {
      name: 'code',
      type: 'string',
      label: formatMessage({ id: 'cluster.project.code' }),
    },
  ],
});
