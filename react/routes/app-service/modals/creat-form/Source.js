import React, { Fragment, useCallback, useState, useEffect } from 'react';
import { Form, TextField, Select, Upload, SelectBox } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Icon, Input, Button, Avatar } from 'choerodon-ui';
import { axios } from '@choerodon/master';
import map from 'lodash/map';
import isEmpty from 'lodash/isEmpty';
import Settings from './Settings';
import { handlePromptError } from '../../../../utils';

import './index.less';

const { Option } = Select;

export default injectIntl(observer((props) => {
  const { modal, dataSet, record, AppStore, projectId, intl: { formatMessage }, intlPrefix, prefixCls } = props;
  const [isExpand, setIsExpand] = useState(false);

  useEffect(() => {
    AppStore.loadAppService(projectId);
  }, []);

  useEffect(() => {
    record.get('templateId') && AppStore.loadVersion(projectId, record.get('templateId'));
    record.get('versionId') && record.set('versionId', null);
  }, [record.get('templateId')]);
  
  function handleExpand() {
    setIsExpand((pre) => !pre);
  }

  return (
    <div className={`${prefixCls}-create-wrap-template`}>
      <div className={`${prefixCls}-create-wrap-template-title`}>
        <span>{formatMessage({ id: `${intlPrefix}.template` })}</span>
        <Icon
          type={isExpand ? 'expand_less' : 'expand_more'}
          onClick={handleExpand}
          className={`${prefixCls}-create-wrap-template-icon`}
        />
      </div>
      {isExpand && (
        <Form record={record} columns={3}>
          <Select name="appServiceSource">
            <Option value="project">{formatMessage({ id: `${intlPrefix}.source.project` })}</Option>
            <Option value="organization">{formatMessage({ id: `${intlPrefix}.source.organization` })}</Option>
            <Option value="market">{formatMessage({ id: `${intlPrefix}.source.market` })}</Option>
          </Select>
          <Select name="templateId" colSpan={2}>
            {map(AppStore.getAppService, ({ id, name }) => (
              <Option value={id}>{name}</Option>
            ))}
          </Select>
          <Select name="versionId" colSpan={3}>
            {map(AppStore.getVersion, ({ id, version }) => (
              <Option value={id}>{version}</Option>
            ))}
          </Select>
        </Form>
      )}
    </div>
  );
}));
