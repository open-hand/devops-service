import { Choerodon } from '@choerodon/boot';
import forEach from 'lodash/forEach';
import map from 'lodash/map';
import includes from 'lodash/includes';

export default ({ formatMessage, appServiceDs, chartsDs, envDs, tableDs }) => {
  async function handleUpdate({ name, value, record }) {
    chartsDs.setQueryParameter(name, value);
    tableDs.setQueryParameter(name, value);
    if (name === 'appServiceIds' && value.length > 5) {
      const newValue = value.splice(0, 5);
      chartsDs.setQueryParameter(name, newValue);
      tableDs.setQueryParameter(name, newValue);
      record.set('appServiceIds', newValue);
      Choerodon.prompt(formatMessage({ id: 'report.deploy-duration.apps' }));
    }
    if (name === 'envId') {
      appServiceDs.setQueryParameter(name, value);
      await appServiceDs.query();
      const appServiceList = appServiceDs.toData();
      const newAppServiceIds = [];
      const ids = map(appServiceList, 'id');
      forEach(record.get('appServiceIds'), (appServiceId) => {
        if (appServiceId && includes(ids, appServiceId)) {
          newAppServiceIds.push(appServiceId);
        }
      });
      if (!newAppServiceIds.length && appServiceList.length) {
        newAppServiceIds.push(appServiceList[0].id);
      }
      record.set('appServiceIds', newAppServiceIds);
    }
    chartsDs.query();
    tableDs.query();
  }
  return ({
    autoCreate: true,
    fields: [
      {
        name: 'envId',
        type: 'string',
        textField: 'name',
        valueField: 'id',
        label: formatMessage({ id: 'envName' }),
        options: envDs,
      },
      {
        name: 'appServiceIds',
        textField: 'name',
        valueField: 'id',
        multiple: true,
        label: formatMessage({ id: 'deploy.appName' }),
        options: appServiceDs,
      },
    ],
    events: {
      update: handleUpdate,
    },
  });
};
