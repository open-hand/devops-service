import React, { useEffect, useState, Fragment, useMemo } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Choerodon } from '@choerodon/boot';
import { Select, Form, TextField } from 'choerodon-ui/pro';
import map from 'lodash/map';
import find from 'lodash/find';
import Tips from '../../../../../components/new-tips';
import { useShareFormStore } from './stores';

import './index.less';

const { Option } = Select;

export default observer(() => {
  const {
    intl: { formatMessage },
    intlPrefix,
    prefixCls,
    VERSION_TYPE,
    modal,
    formDs,
    refresh,
  } = useShareFormStore();
  const record = useMemo(() => formDs.current, [formDs.current]);

  modal.handleOk(async () => {
    if (!record.get('version') && !record.get('versionType')) {
      record.set('hasFailed', true);
      return false;
    }
    try {
      if (await formDs.submit() !== false) {
        refresh();
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

  if (!record) {
    return;
  }

  return (<Fragment>
    <Form record={record}>
      <Select
        name="versionType"
        combo
        addonAfter={<Tips helpText={formatMessage({ id: `${intlPrefix}.detail.type.tips` })} />}
      >
        {map(VERSION_TYPE, (item) => (
          <Option value={item} key={item}>{item}</Option>
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
        searchMatcher="name"
        addonAfter={<Tips helpText={formatMessage({ id: `${intlPrefix}.detail.scope.tips` })} />}
      />
    </Form>
    {record.get('hasFailed') && (
      <span className={`${prefixCls}-share-failed`}>
        {formatMessage({ id: `${intlPrefix}.share.failed` })}
      </span>
    )}
  </Fragment>);
});
