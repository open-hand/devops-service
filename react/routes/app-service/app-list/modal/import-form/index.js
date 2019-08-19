import React, { Fragment, useCallback, useState, useEffect } from 'react';
import { Form, TextField, Select, SelectBox } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import PlatForm from './Platform';

import './index.less';

const { Option } = Select;

const ImportForm = injectIntl(observer((props) => {
  const { dataSet, tableDs, record, AppStore, projectId, intl: { formatMessage }, intlPrefix, prefixCls, refresh } = props;

  props.modal.handleOk(async () => {
    if (record.get('platformType') === 'platform') {
      const lists = tableDs.toData().filter((item) => item.selected === true);
      // const selectedRecords = tableDs.filter((item) => item.selected === true);
      // const result = reduce(lists, (res, obj) => {
      //   (res[obj.name] || (res[obj.name] = [])).push(obj);
      //   return res;
      // }, {});
      // const repeatData = Object.keys(pickBy(result, (value, key) => value.length > 1) || {});
      // forEach(selectedRecords, (item) => {
      //   if (indexOf(repeatData, item.get('name'))) {
      //     record.set('nameFailed', true);
      //   }
      // });
      record.set('appServiceList', lists);
    }
    try {
      if ((await dataSet.submit()) !== false) {
        refresh();
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  });

  return (
    <div className={`${prefixCls}-import-wrap`}>
      <Form record={record}>
        <SelectBox name="platformType">
          <Option value="platform">
            <FormattedMessage id={`${intlPrefix}.import.type.platform`} />
          </Option>
          <Option value="github">
            <FormattedMessage id={`${intlPrefix}.import.type.github`} />
          </Option>
          <Option value="gitlab">
            <FormattedMessage id={`${intlPrefix}.import.type.gitlab`} />
          </Option>
        </SelectBox>
      </Form>
      {record.get('platformType') === 'platform' ? <PlatForm {...props} /> : (
        <Form record={record} style={{ width: '3.6rem' }}>
          <TextField name="repositoryUrl" />
          {record.get('platformType') === 'gitlab' && <TextField name="accessToken" />}
          <TextField name="name" />
          <TextField name="code" />
        </Form>
      )}
    </div>
  );
}));

export default ImportForm;
