import find from 'lodash/find';
import omit from 'lodash/omit';

function formatData(data) {
  const res = omit(data, ['__id', '__status']);
  const { shareLevel } = res;
  if (shareLevel.id !== 'all') {
    res.projectId = shareLevel.id;
    res.projectName = shareLevel.name;
    res.shareLevel = 'project';
  } else {
    res.projectId = null;
    res.projectName = null;
    res.shareLevel = 'organization';
  }
  return res;
}

async function handleUpdate({ name, record }) {
  if (name === 'versionType' || name === 'version') {
    record.set('hasFailed', false);
  }
  if (name === 'versionType') {
    const field = record.getField('version');
    field.reset();
    const res = await record.getField('version').fetchLookup();
    if (record.get('versionType') && record.get('version') && !find(res, ({ version }) => version === record.get('version'))) {
      record.set('version', null);
    }
  }
}

export default (({ intlPrefix, formatMessage, projectId, appServiceId, shareId }) => {
  function handleLoad({ dataSet }) {
    const record = dataSet.current;
    if (record.get('shareLevel') === 'organization') {
      record.init('shareLevel', {
        id: 'all',
        name: formatMessage({ id: `${intlPrefix}.project.all` }),
      });
    } else {
      record.init('shareLevel', {
        id: record.get('projectId'),
        name: record.get('projectName'),
      });
    }
  }

  return ({
    autoQuery: false,
    autoCreate: false,
    selection: false,
    paging: false,
    autoQueryAfterSubmit: false,
    transport: {
      read: {
        url: `/devops/v1/projects/${projectId}/app_service_share/${shareId}`,
        method: 'get',
      },
      create: ({ data: [data] }) => {
        const res = formatData(data);
        res.appServiceId = appServiceId;

        return ({
          url: `/devops/v1/projects/${projectId}/app_service_share`,
          method: 'post',
          data: res,
        });
      },
      update: ({ data: [data] }) => {
        const res = formatData(data);

        return ({
          url: `/devops/v1/projects/${projectId}/app_service_share`,
          method: 'put',
          data: res,
        });
      },
    },
    fields: [
      { name: 'versionType', type: 'string', label: formatMessage({ id: `${intlPrefix}.version.type` }) },
      {
        name: 'version',
        type: 'string',
        textField: 'version',
        valueField: 'version',
        dynamicProps: {
          lookupAxiosConfig: ({ record }) => ({
            url: `/devops/v1/projects/${projectId}/app_service_versions/page_by_options?app_service_id=${appServiceId}&deploy_only=false&do_page=true&page=1&size=40`,
            method: 'post',
            data: { params: [], searchParam: { version: record.get('versionType') } },
          }),
        },
        label: formatMessage({ id: `${intlPrefix}.version.specific` }),
      },
      {
        name: 'shareLevel',
        type: 'object',
        textField: 'name',
        valueField: 'id',
        label: formatMessage({ id: `${intlPrefix}.share.range` }),
        required: true,
        lookupAxiosConfig: ({ params }) => ({
          method: 'get',
          url: `/iam/choerodon/v1/projects/${projectId}/except_self/with_limit`,
          data: params,
          transformResponse(data) {
            try {
              const array = JSON.parse(data);
              if (array.length > 1) {
                const obj = {
                  id: 'all',
                  name: formatMessage({ id: `${intlPrefix}.project.all` }),
                };
                array.unshift(obj);
              }
              return array;
            } catch (e) {
              return data;
            }
          },
        }),
      },
      { name: 'hasFailed', type: 'boolean', defaultValue: false, ignore: 'always' },
    ],
    events: {
      load: handleLoad,
      update: handleUpdate,
    },
  });
});
