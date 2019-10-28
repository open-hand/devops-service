import React, { useEffect, useState, Fragment } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Choerodon } from '@choerodon/boot';
import { Select, Form, TextField } from 'choerodon-ui/pro';
import map from 'lodash/map';
import find from 'lodash/find';
import { handlePromptError } from '../../../../../utils';

import './index.less';
import Tips from '../../../../../components/new-tips';

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
    if (record.status !== 'add') {
      loadShareById();
      if (record.get('shareLevel') === 'organization') {
        record.set('shareLevel', {
          id: 'all',
          name: formatMessage({ id: `${intlPrefix}.project.all` }),
        });
      } else {
        record.set('shareLevel', {
          id: record.get('projectId'),
          name: record.get('projectName'),
        });
      }
    }
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
    record.getField('shareLevel').set('options', levelOptions);
  }, []);

  useEffect(() => {
    const field = record.getField('version');
    field.reset();
    field.set('lookupAxiosConfig', {
      url: `/devops/v1/projects/${projectId}/app_service_versions/page_by_options?app_service_id=${appServiceId}&deploy_only=false&do_page=true&page=1&size=40`,
      method: 'post',
      data: { params: [], searchParam: { version: record.get('versionType') } },
    });
    fetchLookup(field);
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

  async function fetchLookup(field) {
    const res = await field.fetchLookup();
    if (record.get('versionType') && record.get('version') && !find(res, ({ version }) => version === record.get('version'))) {
      record.set('version', null);
    }
  }

  return (<Fragment>
    <Form record={record}>
      <Select
        name="versionType"
        combo
        addonAfter={<Tips helpText={formatMessage({ id: `${intlPrefix}.detail.type.tips` })} />}
      >
        {map(VERSION_TYPE, (item) => (
          <Option value={item}>{item}</Option>
        ))}
      </Select>
      <Select
        name="version"
        searchable
        searchMatcher="version"
        addonAfter={<Tips helpText={formatMessage({ id: `${intlPrefix}.detail.version.tips` })} />}
      />
      <Select
        name="shareLevel"
        searchable
        addonAfter={<Tips helpText={formatMessage({ id: `${intlPrefix}.detail.scope.tips` })} />}
      />
    </Form>
    {hasFailed && (
      <span className={`${prefixCls}-share-failed`}>
        {formatMessage({ id: `${intlPrefix}.share.failed` })}
      </span>
    )}
  </Fragment>);
});
