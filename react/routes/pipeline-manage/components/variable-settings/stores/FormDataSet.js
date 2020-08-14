import isEmpty from 'lodash/isEmpty';

function handleLoad({ dataSet }) {
  if (!dataSet.length) {
    dataSet.create();
  }
}

function handleUpdate({ value, name, record, dataSet }) {
  if (name === 'key' && value) {
    dataSet.forEach((eachRecord) => {
      if (record.id !== eachRecord.id) {
        eachRecord.getField('key').checkValidity();
      }
    });
  }
}

function handleRemove({ dataSet }) {
  dataSet.forEach((record) => {
    record.getField('key').checkValidity();
  });
}

export default ({ formatMessage, intlPrefix, projectId, appServiceId, store }) => {
  const urlParams = appServiceId ? `app_service_id=${appServiceId}&level=app` : 'level=project';
  function checkKey(value, name, record) {
    const p = /^([_A-Za-z0-9])+$/;
    if (!value && !record.get('value')) {
      return;
    }
    if (!value && record.get('value')) {
      return formatMessage({ id: `${intlPrefix}.settings.check.empty` });
    }
    if (p.test(value)) {
      const dataSet = record.dataSet;
      const repeatRecord = dataSet.find((eachRecord) => eachRecord.id !== record.id && eachRecord.get('key') === value);
      if (repeatRecord) {
        return formatMessage({ id: `${intlPrefix}.settings.check.exist` });
      }
    } else {
      return formatMessage({ id: `${intlPrefix}.settings.check.failed` });
    }
  }

  return ({
    autoCreate: false,
    autoQuery: true,
    selection: false,
    paging: false,
    dataKey: null,
    transport: {
      read: {
        url: `devops/v1/projects/${projectId}/ci_variable/values?${urlParams}`,
        method: 'get',
      },
      submit: ({ dataSet }) => {
        const res = [];
        dataSet.toData().forEach((item) => {
          if (!isEmpty(item) && item.key) {
            if (!item.value) {
              item.value = '';
            }
            res.push(item);
          }
        });
        return {
          url: `devops/v1/projects/${projectId}/ci_variable?${urlParams}`,
          method: 'post',
          data: res,
        };
      },
    },
    fields: [
      { name: 'key', type: 'string', label: formatMessage({ id: 'key' }), validator: checkKey },
      { name: 'value', type: 'string', label: formatMessage({ id: 'value' }) },
    ],
    events: {
      load: handleLoad,
      update: handleUpdate,
      remove: handleRemove,
    },
  });
};
