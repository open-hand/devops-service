import React, { Fragment, useCallback, useState, useEffect } from 'react';
import { Form, TextField, Select, SelectBox } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Choerodon } from '@choerodon/boot';
import keys from 'lodash/keys';
import countBy from 'lodash/countBy';
import pickBy from 'lodash/pickBy';
import isEmpty from 'lodash/isEmpty';
import includes from 'lodash/includes';
import map from 'lodash/map';
import PlatForm from './Platform';
import { handlePromptError } from '../../../../../utils';
import Tips from '../../../../../components/new-tips';

import './index.less';

const { Option } = Select;

const IMPORT_METHOD = ['share', 'github', 'gitlab'];

const ImportForm = injectIntl(observer((props) => {
  const { dataSet, selectedDs, record, appServiceStore, projectId, intl: { formatMessage }, intlPrefix, prefixCls, refresh, modal } = props;
  const [hasFailed, setHasFailed] = useState(false);

  useEffect(() => {
    setHasFailed(false);
  }, [record.get('platformType')]);

  modal.handleOk(async () => {
    if (record.get('platformType') === 'share' || record.get('platformType') === 'market') {
      if (!selectedDs.length) return true;
      if (selectedDs.some((item) => item.get('nameFailed') || item.get('codeFailed'))) return false;

      const result = await checkData();
      if (!result) {
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

  async function checkData() {
    const lists = selectedDs.toData();
    const { listName, listCode, repeatCode, repeatName } = getRepeatData(lists);

    try {
      const res = await appServiceStore.batchCheck(projectId, listCode, listName);
      if (handlePromptError(res)) {
        if (isEmpty(repeatName) && isEmpty(repeatCode) && isEmpty(res.listCode) && isEmpty(res.listName)) {
          setHasFailed(false);
          record.set('appServiceList', lists);
          return true;
        } else {
          selectedDs.forEach((item) => {
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

  function getRepeatData(lists) {
    const nameData = countBy(lists, 'name');
    const codeData = countBy(lists, 'code');
    const listName = keys(nameData);
    const listCode = keys(codeData);
    const repeatName = keys(pickBy(nameData, (value) => value > 1) || {});
    const repeatCode = keys(pickBy(codeData, (value) => value > 1) || {});

    return { listCode, listName, repeatName, repeatCode };
  }


  return (
    <div className={`${prefixCls}-import-wrap`}>
      <Form record={record}>
        <SelectBox name="platformType">
          {map(IMPORT_METHOD, (item) => (
            <Option value={item}>
              <span className={`${prefixCls}-import-wrap-radio`}>
                <Tips
                  helpText={formatMessage({ id: `${intlPrefix}.${item}.tips` })}
                  title={formatMessage({ id: `${intlPrefix}.import.type.${item}` })}
                />
              </span>
            </Option>
          ))}
        </SelectBox>
      </Form>
      {record.get('platformType') === 'share' || record.get('platformType') === 'market' ? (
        <Fragment>
          <PlatForm {...props} checkData={checkData} />
          {hasFailed && (
            <span className={`${prefixCls}-import-wrap-failed`}>
              {formatMessage({ id: `${intlPrefix}.platform.failed` })}
            </span>
          )}
        </Fragment>
      ) : (
        <Form record={record} style={{ width: '3.6rem' }}>
          <TextField
            name="repositoryUrl"
            addonAfter={<Tips helpText={formatMessage({ id: `${intlPrefix}.address.tips` })} />}
          />
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
