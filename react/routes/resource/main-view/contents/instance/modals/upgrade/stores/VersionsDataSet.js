import React, { useState } from 'react';
import forEach from 'lodash/forEach';

export default ({ formatMessage, intlPrefix, projectId, appServiceId, upgradeStore, versionId }) => {
  function handleLoad({ dataSet }) {
    if (dataSet.totalCount) {
      const record = dataSet.find((item) => item.get('id') === versionId);
      record && record.set('version', `${record.get('version')} (${formatMessage({ id: `${intlPrefix}.instance.current.version` })})`);
    }
  }
  return ({
    autoCreate: false,
    autoQuery: false,
    selection: 'single',
    pageSize: 40,
    transport: {
      read: ({ data }) => {
        let url = '';
        forEach(['version', 'app_service_version_id'], (item) => {
          const value = data[item];
          if (value) {
            url = `${url}&${item}=${value}`;
          }
        });
        return ({
          url: `/devops/v1/projects/${projectId}/app_service_versions/page_by_options?app_service_id=${appServiceId}&deploy_only=true&do_page=true${url}`,
          method: 'post',
          data: null,
        });
      },
    },
    fields: [
      { name: 'id', type: 'string' },
      { name: 'version', type: 'string' },
    ],
    events: {
      load: handleLoad,
    },
  });
};
