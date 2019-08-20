import { axios } from '@choerodon/master';
import forEach from 'lodash/forEach';
import getTablePostData from '../../../../utils/getTablePostData';

function getNode(node, res, name = 'appServiceList') {
  res.push(node);
  if (node[name]) {
    node[name].forEach((n) => {
      const { version, versionId } = n;
      n.share = node.share;
      n.appName = node.name;
      n.version = { id: versionId, version };
      getNode(n, res, name = 'appServiceList');
    });
  }
}

function getNodesByTree(tree, res, name = 'appServiceList') {
  forEach(tree, (node) => {
    getNode(node, res, name = 'appServiceList');
  });
}

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
    autoQuery: false,
    selection: false,
    paging: false,
    idField: 'id',
    parentField: 'appId',
    transport: {
      read: {
        url: `/devops/v1/projects/${projectId}/app_service/list_app_group`,
        method: 'get',
        transformResponse(data) {
          const arr = JSON.parse(data);
          const roleArray = [];
          getNodesByTree(arr, roleArray, 'appServiceList');
          return {
            list: roleArray,
          };
        },
      },
    },
    fields: [
      { name: 'id', type: 'number' },
      { name: 'appId', type: 'number' },
      { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.name` }), validator: checkName },
      { name: 'code', type: 'string', label: formatMessage({ id: `${intlPrefix}.code` }), validator: checkCode },
      { name: 'type', type: 'string', label: formatMessage({ id: `${intlPrefix}.type` }) },
      { name: 'appName', type: 'string', label: formatMessage({ id: `${intlPrefix}.app` }) },
      { name: 'share', type: 'boolean', label: formatMessage({ id: `${intlPrefix}.source` }) },
      { name: 'selected', type: 'boolean', defaultValue: false },
      { name: 'version', type: 'object', label: formatMessage({ id: `${intlPrefix}.version` }), textField: 'version', valueField: 'id' },
      { name: 'nameFailed', type: 'boolean', defaultValue: false },
      { name: 'codeFailed', type: 'boolean', defaultValue: false },
    ],
  });
});
