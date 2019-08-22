import { axios } from '@choerodon/master';
import getTablePostData from '../../../utils/getTablePostData';

export default ((intlPrefix, formatMessage, projectId) => {
  const pattern = /^([a-z0-9]([-a-z0-9]*[a-z0-9])?(\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)+)$/;

  async function checkName(value) {
    const pa = /^\S+$/;
    if (value && pa.test(value)) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/certs/check_name?name=${value}`);
        if (res && res.failed) {
          return formatMessage({ id: 'checkNameExist' });
        } else {
          return true;
        }
      } catch (err) {
        return formatMessage({ id: 'checkNameFailed' });
      }
    } else {
      return formatMessage({ id: 'nameCanNotHasSpaces' });
    }
  }

  return ({
    autoQuery: true,
    selection: false,
    transport: {
      read: ({ data }) => {
        const postData = getTablePostData(data);

        return ({
          url: `/devops/v1/projects/${projectId}/certs/page_cert`,
          method: 'post',
          data: postData,
        });
      },
      destroy: ({ data: [data] }) => ({
        url: `/devops/v1/projects/${projectId}/certs/${data.id}`,
        method: 'delete',
      }),
    },
    fields: [
      { name: 'id', type: 'number' },
      { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.name` }), validator: checkName },
      { name: 'domain', type: 'string', label: formatMessage({ id: `${intlPrefix}.domain`, pattern }) },
      { name: 'keyValue', type: 'string' },
      { name: 'certValue', type: 'string' },
      { name: 'skipCheckProjectPermission', type: 'boolean', defaultValue: true, label: formatMessage({ id: `${intlPrefix}.share` }) },
      { name: 'projects', type: 'object' },
    ],
  });
});
