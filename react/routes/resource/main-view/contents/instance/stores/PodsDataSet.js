import getTablePostData from '../../../../../../utils/getTablePostData';

export default ({ intl, intlPrefix }) => ({
  selection: false,
  pageSize: 10,
  transport: {
    read: ({ data }) => {
      const postData = getTablePostData(data);
      return {
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
