import React, { Fragment } from 'react';
import { Form, TextField, Select, SelectBox, UrlField, Password, EmailField, Icon, Button } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import Tips from '../../../../components/new-tips';

const { Option } = Select;

const Settings = injectIntl(observer(({ record, appServiceStore, projectId, intl: { formatMessage }, intlPrefix, prefixCls, handleTestHarbor, handleTestChart, isDetailPage }) => (
  <div className="content-settings">
    <div className="content-settings-title">
      <Tips
        helpText={formatMessage({ id: `${intlPrefix}.setting.tips` })}
        title={formatMessage({ id: `${intlPrefix}.create.settings` })}
      />
    </div>
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
)));

export default Settings;
