import getTablePostData from '../../../../../../utils/getTablePostData';

export default ({ formatMessage, intlPrefix, projectId }) => ({
  selection: false,
  pageSize: 10,
  transport: {
    read: {
      method: 'get',
    },
  },
  fields: [
    {
      name: 'nodeName',
      label: formatMessage({ id: `${intlPrefix}.node.ip` }),
    },
    {
      name: 'status',
      type: 'string',
      label: formatMessage({ id: `${intlPrefix}.node.status` }),
    },
    {
      name: 'type',
      type: 'string',
      label: formatMessage({ id: `${intlPrefix}.node.type` }),
    },
    {
      name: 'createTime',
      type: 'string',
      label: formatMessage({ id: 'createDate' }),
    }, 
  ],
});
