import React, { Fragment, useEffect } from 'react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { SelectBox, Select, Form, UrlField, Icon, TextField, Password } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui/pro';
import { withRouter } from 'react-router-dom';
import { handlePromptError } from '../../../utils';

import './index.less';

const { Option } = Select;

export default withRouter(injectIntl(observer(({
  record,
  dataSet,
  store,
  id,
  intl: { formatMessage },
  prefixCls,
  intlPrefix,
  modal,
  isProject,
  history,
  location: { search },
}) => {
  async function refresh() {
    await dataSet.query();
  }

  async function handleSave() {
    if (record.get('harborStatus') === 'failed' || record.get('chartStatus') === 'failed') return false;
    const chartTestFailed = record.get('chartCustom') === 'custom' && !record.get('chartStatus') && !await handleTestChart();
    if (!chartTestFailed && await dataSet.submit() !== false) {
      refresh();
    } else {
      return false;
    }
  }

  async function handleTestChart() {
    try {
      if (!await record.validate()) {
        return false;
      }
      const postData = {
        url: record.get('url'),
        userName: record.get('password') && record.get('userName') ? record.get('userName') : null,
        password: record.get('password') && record.get('userName') ? record.get('password') : null,
      };
      const res = await store.checkChart(id, postData);
      if (handlePromptError(res)) {
        record.set('chartStatus', 'success');
        return true;
      } else {
        record.set('chartStatus', 'failed');
        return false;
      }
    } catch (e) {
      record.set('chartStatus', 'failed');
      return false;
    }
  }

  function renderTestButton(status, handleClick) {
    return (
      <div className={`${prefixCls}-form-btnContent`}>
        <Button
          onClick={handleClick}
          funcType="raised"
          className={`${prefixCls}-form-button`}
        >
          <FormattedMessage id={`${intlPrefix}.test`} />
        </Button>
        {status && (
          <span>
            <Icon
              type={status === 'success' ? 'check_circle' : 'cancel'}
              className={`${prefixCls}-form-test-${status}`}
            />
            {formatMessage({ id: `${intlPrefix}.test.${status}` })}
          </span>
        )}
      </div>
    );
  }

  function handleLink() {
    history.push(`/rdupm/product-lib${search}`);
  }

  return (
    <div className={`${prefixCls}-form-wrap`}>
      <div className={`${prefixCls}-form-info`}>
        <Icon type="info" className={`${prefixCls}-form-info-icon`} />
        <FormattedMessage id={`${intlPrefix}.info`} />
      </div>
      {isProject ? (<div>
        <span className={`${prefixCls}-form-config-title`}>
          {formatMessage({ id: `${intlPrefix}.harbor.config` })}
        </span>
        <div className={`${prefixCls}-empty-page`}>
          <div className={`${prefixCls}-empty-page-image`} />
          <div className={`${prefixCls}-empty-page-text`}>
            <div className={`${prefixCls}-empty-page-title`}>
              {formatMessage({ id: `${intlPrefix}.empty.title` })}
            </div>
            <div className={`${prefixCls}-empty-page-des`}>
              {formatMessage({ id: `${intlPrefix}.empty.des` })}
            </div>
            <Button
              color="primary"
              onClick={handleLink}
              funcType="raised"
            >
              {formatMessage({ id: `${intlPrefix}.empty.link` })}
            </Button>
          </div>
        </div>
      </div>) : null}
      <div className={`${prefixCls}-form`}>
        <span className={`${prefixCls}-form-config-title`}>
          {formatMessage({ id: `${intlPrefix}.chart.config` })}
        </span>
        <Form record={record}>
          <SelectBox name="chartCustom">
            <Option value="default">{formatMessage({ id: `${intlPrefix}.chart.default` })}</Option>
            <Option value="custom">{formatMessage({ id: `${intlPrefix}.chart.custom` })}</Option>
          </SelectBox>
          {record.get('chartCustom') === 'custom' && ([
            <UrlField name="url" />,
            <TextField name="userName" />,
            <Password name="password" />,
            renderTestButton(record.get('chartStatus'), handleTestChart),
          ])}
        </Form>
      </div>
      <div style={{ display: 'flex' }}>
        <Button
          color="primary"
          funcType="raised"
          onClick={handleSave}
          style={{ marginRight: '.12rem' }}
        >{formatMessage({ id: 'save' })}</Button>
        <Button
          funcType="raised"
          onClick={refresh}
        >
          <span style={{ color: '#3f51b5' }}>{formatMessage({ id: 'cancel' })}</span>
        </Button>
      </div>
    </div>
  );
})));
