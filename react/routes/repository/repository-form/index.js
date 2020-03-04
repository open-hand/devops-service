import React, { Fragment, useEffect } from 'react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import isEmpty from 'lodash/isEmpty';
import forEach from 'lodash/forEach';
import pick from 'lodash/pick';
import { SelectBox, Select, Form, TextField, UrlField, Password, EmailField, Icon } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui/pro';
import { handlePromptError } from '../../../utils';

import './index.less';

const { Option } = Select;

export default injectIntl(observer(({
  record,
  dataSet,
  store,
  id,
  intl: { formatMessage },
  prefixCls,
  intlPrefix,
  modal,
  isProject,

}) => {
  useEffect(() => {
    handleDefault();
  }, [dataSet.current]);

  async function refresh() {
    await dataSet.query();
  }

  function handleDefault() {
    if (!isEmpty(record.get('harbor'))) {
      record.init('harborCustom', 'custom');
      forEach(record.get('harbor').config, (value, key) => {
        if (key !== 'project' || isProject) {
          record.init(key, value);
        }
      });
      isProject && record.init('harborPrivate', record.get('harbor').harborPrivate);
    } else {
      record.init('harborCustom', 'default');
    }
    if (!isEmpty(record.get('chart'))) {
      const { url } = record.get('chart').config || {};
      record.init('chartCustom', 'custom');
      record.init('chartUrl', url);
    } else {
      record.init('chartCustom', 'default');
    }
  }

  async function handleSave() {
    const statusDs = await dataSet.submit();
    if (record.get('harborStatus') === 'failed' || record.get('chartStatus') === 'failed') return false;
    const harborTestFailed = record.get('harborCustom') === 'custom' && !record.get('harborStatus') && !await handleTestHarbor();
    const chartTestFailed = record.get('chartCustom') === 'custom' && !record.get('chartStatus') && !await handleTestChart();
    if (!harborTestFailed && !chartTestFailed && statusDs !== false) {
      refresh();
    } else {
      return false;
    }
  }

  async function handleTestHarbor() {
    try {
      const postData = pick(record.toData(), ['url', 'userName', 'password', 'email', 'project']);
      const res = await store.checkHarbor(id, postData);
      if (handlePromptError(res, false)) {
        record.set('harborStatus', 'success');
        return true;
      } else {
        record.set('harborStatus', 'failed');
        return false;
      }
    } catch (e) {
      record.set('harborStatus', 'failed');
      return false;
    }
  }

  async function handleTestChart() {
    try {
      const res = await store.checkChart(id, record.get('chartUrl'));
      if (handlePromptError(res, false)) {
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

  return (
    <div className={`${prefixCls}-form`}>
      <div className={`${prefixCls}-form-info`}>
        <Icon type="info" className={`${prefixCls}-form-info-icon`} />
        <FormattedMessage id={`${intlPrefix}.info`} />
      </div>
      <Form record={record}>
        <SelectBox name="harborCustom">
          <Option value="default">{formatMessage({ id: `${intlPrefix}.harbor.default` })}</Option>
          <Option value="custom">{formatMessage({ id: `${intlPrefix}.harbor.custom` })}</Option>
        </SelectBox>
      </Form>
      {isProject && record.get('harborCustom') === 'default' && (
        <Form record={record}>
          <SelectBox name="harborPrivate">
            <Option value={false}>{formatMessage({ id: `${intlPrefix}.public` })}</Option>
            <Option value>{formatMessage({ id: `${intlPrefix}.private` })}</Option>
          </SelectBox>
        </Form>
      )}
      {record.get('harborCustom') === 'custom' && (<Fragment>
        <Form record={record}>
          <UrlField name="url" />
          <TextField name="userName" />
          <Password name="password" />
          <EmailField name="email" />
          {isProject && <TextField name="project" />}
        </Form>
        {renderTestButton(record.get('harborStatus'), handleTestHarbor)}
      </Fragment>)}
      <Form record={record}>
        <SelectBox name="chartCustom">
          <Option value="default">{formatMessage({ id: `${intlPrefix}.chart.default` })}</Option>
          <Option value="custom">{formatMessage({ id: `${intlPrefix}.chart.custom` })}</Option>
        </SelectBox>
      </Form>
      {record.get('chartCustom') === 'custom' && (<Fragment>
        <Form record={record}>
          <UrlField name="chartUrl" />
        </Form>
        {renderTestButton(record.get('chartStatus'), handleTestChart)}
      </Fragment>)}
      <div style={{ display: 'flex' }}>
        <Button
          color="primary"
          funcType="raised"
          onClick={handleSave}
          style={{ marginRight: '.12rem' }}
        >{formatMessage({ id: 'save' })}</Button>
        <Button
          funcType="raised"
          onClick={() => refresh()}
        >
          <span style={{ color: '#3f51b5' }}>{formatMessage({ id: 'cancel' })}</span>
        </Button>
      </div>
    </div>
  );
}));
