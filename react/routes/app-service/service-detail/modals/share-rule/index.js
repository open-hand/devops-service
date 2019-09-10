import React, { useEffect, useState, Fragment } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Select, Form } from 'choerodon-ui/pro';
import map from 'lodash/map';
import { handlePromptError } from '../../../../../utils';

import './index.less';

const { Option } = Select;
const VERSION_TYPE = ['master', 'feature', 'hotfix', 'bugfix', 'release'];

export default observer(({ record, dataSet, versionOptions, levelOptions, projectId, store, formatMessage, appServiceId, intlPrefix, prefixCls, modal }) => {
  const [hasFailed, setHasFailed] = useState(false);
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
      if (levelOptions.length > 1) {
        const createdOption = levelOptions.create({
          id: 'all',
          name: formatMessage({ id: `${intlPrefix}.project.all` }),
          appName: levelOptions.current.get('appName'),
        });
        levelOptions.unshift(createdOption);
      }
    }
    createOption();
    record.getField('version').set('options', versionOptions);
    record.getField('shareLevel').set('options', levelOptions);
  }, []);

  useEffect(() => {
    const url = record.get('versionType') ? `&version=${record.get('versionType')}` : '';
    versionOptions.transport.read.url = `/devops/v1/projects/${projectId}/app_service_versions/list_app_services/${appServiceId}?deploy_only=false${url}`;
    versionOptions.query();
  }, [record.get('versionType')]);

  useEffect(() => {
    setHasFailed(false);
  }, [record.get('versionType'), record.get('version')]);

  modal.handleOk(async () => {
    if (!record.get('version') && !record.get('versionType')) {
      setHasFailed(true);
      return false;
    }
    try {
      if (await dataSet.submit() !== false) {
        dataSet.query();
      } else {
        return false;
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
      return false;
    }
  });
  
  function optionRenderer({ record: tableRecord, text, value }) {
    return (
      <div>{text}-{tableRecord.get('appName')}</div>
    );
  }

  return (<Fragment>
    <Form record={record}>
      <Select name="versionType" combo>
        {map(VERSION_TYPE, (item) => (
          <Option value={item}>{item}</Option>
        ))}
      </Select>
      <Select name="version" searchable />
      <Select name="shareLevel" searchable optionRenderer={optionRenderer} />
    </Form>
    {hasFailed && (
      <span className={`${prefixCls}-share-failed`}>
        {formatMessage({ id: `${intlPrefix}.share.failed` })}
      </span>
    )}
  </Fragment>);
});
