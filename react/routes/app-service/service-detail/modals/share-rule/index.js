import React, { useEffect } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Select, Form } from 'choerodon-ui/pro';
import { handlePromptError } from '../../../../../utils';

import './index.less';

const { Option } = Select;

export default observer(({ record, versionOptions, levelOptions, projectId, store, formatMessage, appServiceId, intlPrefix }) => {
  useEffect(() => {
    async function loadShareById() {
      try {
        const res = await store.loadShareById(projectId, record.get('id'));
        if (handlePromptError(res)) {
          record.set('objectVersionNumber', res.objectVersionNumber);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    }
    record.status !== 'add' && loadShareById();
  }, []);

  useEffect(() => {
    async function createOption() {
      await levelOptions.query();
      const createdOption = levelOptions.create({
        id: 'all',
        name: formatMessage({ id: `${intlPrefix}.project.all` }),
      });
      levelOptions.unshift(createdOption);
    }
    createOption();
    record.getField('version').set('options', versionOptions);
    record.getField('shareLevel').set('options', levelOptions);
  }, []);

  useEffect(() => {
    const url = record.get('versionType') ? `?version=${record.get('versionType')}` : '';
    versionOptions.transport.read.url = `/devops/v1/projects/${projectId}/app_service_versions/list_app_services/${appServiceId}${url}`;
    versionOptions.query();
  }, [record.get('versionType')]);

  return (
    <Form record={record}>
      <Select name="versionType" combo>
        <Option value="master">master</Option>
        <Option value="feature">feature</Option>
        <Option value="hotfix">hotfix</Option>
        <Option value="bugfix">bugfix</Option>
        <Option value="release">release</Option>
      </Select>
      <Select name="version" searchable />
      <Select name="shareLevel" searchable />
    </Form>
  );
});
