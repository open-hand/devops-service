import getTablePostData from '../../../../../../utils/getTablePostData';

export default ({ intl, intlPrefix, projectId, envId, appId, id }) => ({
  selection: false,
  pageSize: 10,
  transport: {
    read: ({ data }) => {
      const postData = getTablePostData(data);
      const param = appId ? `&app_service_id=${appId}` : '';

      /**
       *
       * 查询 pod 详情时，参数env_id和 app_service_id
       *
       *  全为空时，查询实例相关的所有pods
       *
       *  其他情况相当于按条件筛选
       *
       * */

      return {
        url: `devops/v1/projects/${projectId}/pods/page_by_options?env_id=${envId}&instance_id=${id}${param}`,
        method: 'post',
        data: postData,
      };
    },
  },
  fields: [
    {
      name: 'status',
      type: 'string',
    },
    {
      name: 'name',
      type: 'string',
      label: intl.formatMessage({ id: `${intlPrefix}.instance.pod` }),
    },
    {
      name: 'containers',
      type: 'object',
      label: intl.formatMessage({ id: 'container' }),
    },
    {
      name: 'ip',
      type: 'string',
      label: intl.formatMessage({ id: `${intlPrefix}.instance.ip` }),
    },
    {
      name: 'creationDate',
      type: 'dateTime',
      label: intl.formatMessage({ id: 'createDate' }),
    },
  ],
});
