import React, { Fragment, useCallback, useState, useEffect } from 'react';
import { Form, TextField, Select, SelectBox } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import reduce from 'lodash/reduce';
import pickBy from 'lodash/pickBy';
import forEach from 'lodash/forEach';
import includes from 'lodash/includes';
import isEmpty from 'lodash/isEmpty';
import PlatForm from './Platform';

import './index.less';
import { handlePromptError } from '../../../../../utils';

const { Option } = Select;

const ImportForm = injectIntl(observer((props) => {
  const { dataSet, tableDs, record, AppStore, projectId, intl: { formatMessage }, intlPrefix, prefixCls, refresh } = props;
  const [hasFailed, setHasFailed] = useState(false);

  useEffect(() => {
    tableDs.query();
  }, []);

  props.modal.handleOk(async () => {
    if (record.get('platformType') === 'platform') {
      const lists = tableDs.toData().filter((item) => item.selected && item.appId);
      if (isEmpty(lists)) return false;

      const { listName, listCode, repeatCode, repeatName } = getRepeatData(lists);

      try {
        const res = await AppStore.batchCheck(projectId, listCode, listName);
        if (handlePromptError(res)) {
          if (isEmpty(repeatName) && isEmpty(repeatCode) && isEmpty(res.listCode) && isEmpty(res.listName)) {
            setHasFailed(false);
            record.set('appServiceList', lists);
          } else {
            const selectedRecords = tableDs.filter((item) => item.get('selected') && item.get('appId'));
            forEach(selectedRecords, (item) => {
              if (includes(repeatName.concat(res.listName), item.get('name'))) {
                item.set('nameFailed', true);
              } else {
                item.set('nameFailed', false);
              }
              if (includes(repeatCode.concat(res.listCode), item.get('code'))) {
                item.set('codeFailed', true);
              } else {
                item.set('codeFailed', false);
              }
            });
            setHasFailed(true);
            return false;
          }
        } else {
          return false;
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
        return false;
      }
    }
    try {
      if ((await dataSet.submit()) !== false) {
        refresh();
      } else {
        return false;
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
      return false;
    }
  });


  function getRepeatData(lists) {
    const repeatData = reduce(lists, (res, obj) => {
      (res.name[obj.name] || (res.name[obj.name] = [])).push(obj);
      (res.code[obj.code] || (res.code[obj.code] = [])).push(obj);
      return res;
    }, { name: {}, code: {} });

    const listCode = Object.keys(repeatData.code);
    const listName = Object.keys(repeatData.name);
    const repeatName = Object.keys(pickBy(repeatData.name, (value) => value.length > 1) || {});
    const repeatCode = Object.keys(pickBy(repeatData.code, (value) => value.length > 1) || {});

    return { listCode, listName, repeatName, repeatCode };
  }

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
      {record.get('platformType') === 'platform' ? (
        <Fragment>
          <PlatForm {...props} />
          {hasFailed && (
            <span className={`${prefixCls}-import-wrap-failed`}>
              {formatMessage({ id: `${intlPrefix}.platform.failed` })}
            </span>
          )}
        </Fragment>
      ) : (
        <Form record={record} style={{ width: '3.6rem' }}>
          <TextField name="repositoryUrl" />
          {record.get('platformType') === 'gitlab' && <TextField name="accessToken" />}
          <Select name="type" clearButton={false}>
            <Option value="normal">
              {formatMessage({ id: `${intlPrefix}.type.normal` })}
            </Option>
            <Option value="test">
              {formatMessage({ id: `${intlPrefix}.type.test` })}
            </Option>
          </Select>
          <TextField name="name" />
          <TextField name="code" />
        </Form>
      )}
    </div>
  );
}));

export default ImportForm;
