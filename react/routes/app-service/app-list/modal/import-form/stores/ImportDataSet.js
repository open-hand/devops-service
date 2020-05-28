import { axios } from '@choerodon/boot';
import map from 'lodash/map';
import pick from 'lodash/pick';

function getRequestData(appServiceList) {
  const res = map(appServiceList, ({ id, name, code, type, versionId }) => ({
    appServiceId: id,
    appName: name,
    appCode: code,
    type,
    versionId,
  }));
  return res;
}

function isGit({ record }) {
  const flag = record.get('platformType') === 'github' || record.get('platformType') === 'gitlab';
  return flag;
}

export default ({ intlPrefix, formatMessage, projectId, serviceTypeDs, selectedDs, importTableDs }) => {
  async function checkCode(value) {
    const pa = /^[a-z]([-a-z0-9]*[a-z0-9])?$/;
    if (value) {
      if (pa.test(value)) {
        try {
          const res = await axios.get(`/devops/v1/projects/${projectId}/app_service/check_code?code=${value}`);
          if ((res && res.failed) || !res) {
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
  }

  async function checkName(value) {
    const pa = /^\S+$/;
    if (value) {
      if (pa.test(value)) {
        try {
          const res = await axios.get(`/devops/v1/projects/${projectId}/app_service/check_name?name=${encodeURIComponent(value)}`);
          if ((res && res.failed) || !res) {
            return formatMessage({ id: 'checkNameExist' });
          } else {
            return true;
          }
        } catch (err) {
          return formatMessage({ id: `${intlPrefix}.name.failed` });
        }
      } else {
        return formatMessage({ id: 'nameCanNotHasSpaces' });
      }
    }
  }

  function handleUpdate({ record, name, value }) {
    if (name === 'platformType') {
      selectedDs.removeAll();
      switch (value) {
        case 'share':
          importTableDs.setQueryParameter('share', true);
          break;
        case 'github':
          if (record.get('repositoryUrl') || !record.getField('repositoryUrl').isValid()) {
            record.set('repositoryUrl', null);
          }
          if (record.get('isTemplate')) {
            record.getField('githubTemplate').fetchLookup();
            record.get('githubTemplate') && record.set('repositoryUrl', record.get('githubTemplate'));
          }
          break;
        case 'gitlab':
          if (record.get('repositoryUrl') || !record.getField('repositoryUrl').isValid()) {
            record.set('repositoryUrl', null);
          }
          break;
        default:
          break;
      }
    }
    if (name === 'githubTemplate') {
      record.set('repositoryUrl', value);
    }
    if (name === 'isTemplate') {
      record.get('repositoryUrl') && record.set('repositoryUrl', null);
      if (value && record.get('githubTemplate')) {
        record.set('repositoryUrl', record.get('githubTemplate'));
      }
    }
  }

  return ({
    autoCreate: true,
    autoQuery: false,
    selection: false,
    paging: false,
    transport: {
      create: ({ data: [data] }) => {
        const { platformType } = data;
        const appServiceList = selectedDs.toData();
        let url = 'external';
        let res;
        if (platformType === 'gitlab') {
          res = pick(data, ['code', 'name', 'type', 'accessToken', 'repositoryUrl']);
        }
        if (platformType === 'github') {
          url = `${url}${data.isTemplate ? '?is_template=true' : ''}`;
          res = pick(data, ['code', 'name', 'type', 'repositoryUrl']);
        }
        if (platformType === 'share' || platformType === 'market') {
          url = 'internal';
          res = getRequestData(appServiceList);
        }
        return ({
          url: `/devops/v1/projects/${projectId}/app_service/import/${url}`,
          method: 'post',
          data: res,
        });
      },
    },
    fields: [
      {
        name: 'name',
        type: 'string',
        maxLength: 40,
        dynamicProps: {
          required: isGit,
          validator: ({ record }) => isGit({ record }) && checkName,
        },
        label: formatMessage({ id: `${intlPrefix}.name` }),
      },
      {
        name: 'code',
        type: 'string',
        maxLength: 30,
        dynamicProps: {
          required: isGit,
          validator: ({ record }) => isGit({ record }) && checkCode,
        },
        label: formatMessage({ id: `${intlPrefix}.code` }),
      },
      {
        name: 'type',
        type: 'string',
        defaultValue: 'normal',
        textField: 'text',
        valueField: 'value',
        options: serviceTypeDs,
        dynamicProps: {
          required: isGit,
        },
        label: formatMessage({ id: `${intlPrefix}.type` }),
      },
      {
        name: 'platformType',
        type: 'string',
        defaultValue: 'share',
        label: formatMessage({ id: `${intlPrefix}.import.type` }),
      },
      {
        name: 'repositoryUrl',
        type: 'url',
        dynamicProps: {
          required: isGit,
          label: ({ record }) => {
            if (record.get('platformType') === 'gitlab' || record.get('platformType') === 'github') {
              return formatMessage({ id: `${intlPrefix}.url.${record.get('platformType')}` });
            }
          },
        },
      },
      {
        name: 'accessToken',
        type: 'string',
        dynamicProps: {
          required: ({ record }) => record.get('platformType') === 'gitlab',
        },
        label: formatMessage({ id: `${intlPrefix}.token` }),
      },
      {
        name: 'isTemplate',
        type: 'bool',
        defaultValue: true,
        label: formatMessage({ id: `${intlPrefix}.github.source` }),
      },
      {
        name: 'githubTemplate',
        type: 'string',
        textField: 'name',
        valueField: 'path',
        dynamicProps: {
          lookupUrl: ({ record }) => (record.get('platformType') === 'github' ? `/devops/v1/projects/${projectId}/app_service/list_service_templates` : ''),
          required: ({ record }) => record.get('platformType') === 'github' && record.get('isTemplate'),
        },
        label: formatMessage({ id: `${intlPrefix}.github.template` }),
      },
    ],
    events: {
      update: handleUpdate,
    },
  });
};
