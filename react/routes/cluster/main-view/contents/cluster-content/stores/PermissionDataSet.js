import getTablePostData from '../../../../../../utils/getTablePostData';

export default ({ formatMessage, projectId, id, skipCheckProjectPermission }) => ({
  selection: false,
  pageSize: 10,
  transport: {
    read: ({ data }) => {
      const postData = getTablePostData(data);
      let URL = '';
      if (skipCheckProjectPermission) {
        URL = `/devops/v1/projects/${projectId}/page_projects`;
      } else {
        URL = `/devops/v1/projects/${projectId}/clusters/${id}/permission/page_related`;
      }
      return {
        url: URL,
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
    {
      name: 'projectId',
      type: 'string',
      label: formatMessage({ id: 'c7ncd.cluster.project' }),
      valueField: 'id',
      textField: 'name',
    },
  ],
  queryFields: [
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
