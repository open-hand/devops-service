import React, { Fragment, useCallback, useState, useEffect } from 'react';
import { Form, TextField, Select, Upload, SelectBox, UrlField, Password, EmailField } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Icon, Button } from 'choerodon-ui';
import isEmpty from 'lodash/isEmpty';
import { handlePromptError } from '../../../../../utils';

import '../../index.less';

const { Option } = Select;

const Settings = injectIntl(observer(({ record, AppStore, projectId, intl: { formatMessage }, intlPrefix, prefixCls, handleTestHarbor, handleTestChart }) => {
  const [isExpand, setIsExpand] = useState(false);

  useEffect(() => {
    async function loadData() {
      try {
        const res = await AppStore.loadAppById(projectId, record.get('id'));
        if (handlePromptError(res)) {
          record.set('chart', res.chart);
          record.set('harbor', res.harbor);
          record.set('oldName', res.name);
          record.set('objectVersionNumber', res.objectVersionNumber);
          if (!isEmpty(res.chart)) {
            record.set('chartUrl', res.chart.config.url);
            record.set('chartType', 'custom');
          } else {
            record.set('chartType', 'default');
          }
          if (!isEmpty(res.harbor)) {
            const { url, userName, password, project, email } = res.harbor.config || {};
            record.set('url', url);
            record.set('userName', userName);
            record.set('password', password);
            record.set('email', email);
            record.set('project', project);
            record.set('harborType', 'custom');
          } else {
            record.set('harborType', 'default');
          }
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    }
    loadData();
  }, []);

  function handleExpand() {
    setIsExpand((pre) => !pre);
  }

  return (
    <div className="content-settings">
      <div className="content-settings-title">
        <FormattedMessage id={`${intlPrefix}.create.settings`} />
        <Icon
          type={isExpand ? 'expand_less' : 'expand_more'}
          className="content-settings-title-icon"
          onClick={handleExpand}
        />
      </div>
      <div className={!isExpand ? 'content-settings-detail' : ''}>
        <div className="content-settings-tips">
          <Icon type="info" className="content-settings-tips-icon" />
          <FormattedMessage id={`${intlPrefix}.create.settings.tips`} />
        </div>
        <Form record={record}>
          <SelectBox name="harborType">
            <Option value="default">
              {formatMessage({ id: `${intlPrefix}.docker.default` })}
            </Option>
            <Option value="custom">
              {formatMessage({ id: `${intlPrefix}.docker.custom` })}
            </Option>
          </SelectBox>
        </Form>
        {record.get('harborType') === 'custom' && (<Fragment>
          <Form record={record}>
            <UrlField name="url" />
            <TextField name="userName" />
            <Password name="password" />
            <EmailField name="email" />
            <TextField name="project" />
          </Form>
          <div>
            <Button
              onClick={handleTestHarbor}
              funcType="raised"
              className="content-settings-button"
            >
              <FormattedMessage id={`${intlPrefix}.test`} />
            </Button>
            {record.get('harborStatus') && (
              <span>
                <Icon
                  type={record.get('harborStatus') === 'success' ? 'check_circle' : 'cancel'}
                  className={`content-settings-link-${record.get('harborStatus')}`}
                />
                {formatMessage({ id: `${intlPrefix}.test.${record.get('harborStatus')}` })}
              </span>
            )}
          </div>
        </Fragment>)}
        <Form record={record}>
          <SelectBox name="chartType">
            <Option value="default">
              {formatMessage({ id: `${intlPrefix}.helm.default` })}
            </Option>
            <Option value="custom">
              {formatMessage({ id: `${intlPrefix}.helm.custom` })}
            </Option>
          </SelectBox>
        </Form>
        {record.get('chartType') === 'custom' && (<Fragment>
          <Form record={record}>
            <UrlField name="chartUrl" />
          </Form>
          <div>
            <Button
              onClick={handleTestChart}
              funcType="raised"
              className="content-settings-button"
            >
              <FormattedMessage id={`${intlPrefix}.test`} />
            </Button>
            {record.get('chartStatus') && (
              <span>
                <Icon
                  type={record.get('chartStatus') === 'success' ? 'check_circle' : 'cancel'}
                  className={`content-settings-link-${record.get('chartStatus')}`}
                />
                {formatMessage({ id: `${intlPrefix}.test.${record.get('chartStatus')}` })}
              </span>
            )}
          </div>
        </Fragment>)}
      </div>
    </div>
  );
}));

export default Settings;
