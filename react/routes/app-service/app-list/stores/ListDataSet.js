import { axios } from '@choerodon/boot';
import getTablePostData from '../../../../utils/getTablePostData';

export default ((intlPrefix, formatMessage, projectId) => {
  async function checkCode(value) {
    const pa = /^[a-z]([-a-z0-9]*[a-z0-9])?$/;
    if (value && pa.test(value)) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/app_service/check_code?code=${value}`);
        if (res && res.failed) {
          return formatMessage({ id: 'checkCodeExist' });
        } else {
          return true;
        }
      } catch (err) {
        return formatMessage({ id: 'checkCodeFailed' });
      }
    } else {
      return formatMessage({ id: 'checkCodeReg' });
    }
  }

  async function checkName(value) {
    const pa = /^\S+$/;
    if (value && pa.test(value)) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/app_service/check_name?name=${value}`);
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
    pageSize: 10,
    transport: {
      read: ({ data }) => {
        const postData = getTablePostData(data);
  
        return {
          url: `/devops/v1/projects/${projectId}/app_service/page_by_options`,
          method: 'post',
          data: postData,
        };
      },
      create: ({ data: [data] }) => {
        const { type, code, name, imgUrl } = data;
        return ({
          url: `/devops/v1/projects/${projectId}/app_service`,
          method: 'post',
          data: { type, code, name, imgUrl, isSkipCheckPermission: true },
        });
      },
      destroy: ({ data: [data] }) => ({
        url: `/devops/v1/projects/${projectId}/app_service/${data.id}`,
        method: 'delete',
      }),
    },
    fields: [
      { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.name` }), required: true, validator: checkName },
      { name: 'code', type: 'string', label: formatMessage({ id: `${intlPrefix}.code` }), required: true, validator: checkCode },
      { name: 'id', type: 'number' },
      { name: 'type', type: 'string', defaultValue: 'normal', label: formatMessage({ id: `${intlPrefix}.type` }), required: true },
      { name: 'active', type: 'boolean' },
      { name: 'creationDate', type: 'dateTime', label: formatMessage({ id: 'createDate' }) },
      { name: 'permission', type: 'boolean' },
      { name: 'repoUrl', type: 'string' },
      { name: 'imgUrl', type: 'string' },
      { name: 'fail', type: 'boolean' },
      { name: 'chartConfigId', type: 'number' },
      { name: 'harborConfigId', type: 'number' },
      { name: 'gitlabProjectId', type: 'number' },
      { name: 'synchro', type: 'boolean' },
      { name: 'sonarUrl', type: 'string' },
      { name: 'dockerType', type: 'string', label: formatMessage({ id: `${intlPrefix}.docker` }), defaultValue: 'default' },
      { name: 'helmType', type: 'string', label: formatMessage({ id: `${intlPrefix}.helm` }), defaultValue: 'default' },
      { name: 'helmAddress', type: 'url', label: formatMessage({ id: 'address' }), required: true },
      { name: 'dockerAddress', type: 'url', label: formatMessage({ id: 'address' }), required: true },
      { name: 'loginName', type: 'string', label: formatMessage({ id: 'loginName' }), required: true },
      { name: 'password', type: 'string', label: formatMessage({ id: 'password' }), required: true },
      { name: 'email', type: 'email', label: formatMessage({ id: 'mailbox' }), required: true },
      { name: 'harborProject', type: 'url', label: 'Harbor Project', required: true },
    ],
  });
});
