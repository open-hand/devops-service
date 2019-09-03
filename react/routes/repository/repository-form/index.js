import React, { Fragment, useEffect } from 'react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import isEmpty from 'lodash/isEmpty';
import forEach from 'lodash/forEach';
import { SelectBox, Select, Form, TextField, UrlField, Password, EmailField } from 'choerodon-ui/pro';
import { Button, Icon } from 'choerodon-ui';

import './index.less';

const { Option } = Select;

export default injectIntl(observer(({ record, dataSet, intl: { formatMessage }, prefixCls, intlPrefix, modal }) => {
  useEffect(() => {
    if (!isEmpty(record.get('harbor'))) {
      record.set('harborCustom', true);
      forEach(record.get('harbor').config, (value, key) => {
        record.set(key, value);
      });
    }
    if (!isEmpty(record.get('chart'))) {
      const { url } = record.get('chart').config || {};
      record.set('chartCustom', true);
      record.set('chartUrl', url);
    }
  });
  modal.handleOk(async () => {

  });

  return (
    <div className={`${prefixCls}-form`}>
      <div className={`${prefixCls}-form-info`}>
        <Icon type="info" className={`${prefixCls}-form-info-icon`} />
        <FormattedMessage id={`${intlPrefix}.info`} />
      </div>
      <Form record={record}>
        <SelectBox name="harborCustom">
          <Option value={false}>{formatMessage({ id: `${intlPrefix}.harbor.default` })}</Option>
          <Option value>{formatMessage({ id: `${intlPrefix}.harbor.custom` })}</Option>
        </SelectBox>
      </Form>
      {record.get('harborCustom') && (<Fragment>
        <Form record={record}>
          <UrlField name="url" />
          <TextField name="userName" />
          <Password name="password" />
          <EmailField name="email" />
          <TextField name="project" />
        </Form>
        <div>
          <Button
            // onClick={handleTestHarbor}
            funcType="raised"
            className={`${prefixCls}-form-button`}
          >
            <FormattedMessage id={`${intlPrefix}.test`} />
          </Button>
          {record.get('harborStatus') && (
            <span>
              <Icon
                type={record.get('harborStatus') === 'success' ? 'check_circle' : 'cancel'}
                className={`${prefixCls}-form-test-${record.get('harborStatus')}`}
              />
              {formatMessage({ id: `${intlPrefix}.test.${record.get('harborStatus')}` })}
            </span>
          )}
        </div>
      </Fragment>)}
      <Form record={record}>
        <SelectBox name="chartCustom">
          <Option value={false}>{formatMessage({ id: `${intlPrefix}.chart.default` })}</Option>
          <Option value>{formatMessage({ id: `${intlPrefix}.chart.custom` })}</Option>
        </SelectBox>
      </Form>
      {record.get('chartCustom') && (<Fragment>
        <Form record={record}>
          <UrlField name="chartUrl" />
        </Form>
        <div>
          <Button
            // onClick={handleTestChart}
            funcType="raised"
            className={`${prefixCls}-form-button`}
          >
            <FormattedMessage id={`${intlPrefix}.test`} />
          </Button>
          {record.get('chartStatus') && (
            <span>
              <Icon
                type={record.get('chartStatus') === 'success' ? 'check_circle' : 'cancel'}
                className={`${prefixCls}-form-test-${record.get('chartStatus')}`}
              />
              {formatMessage({ id: `${intlPrefix}.test.${record.get('chartStatus')}` })}
            </span>
          )}
        </div>
      </Fragment>)}
    </div>
  );
}));
