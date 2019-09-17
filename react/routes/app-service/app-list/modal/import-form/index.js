import React, { Fragment, useCallback, useState, useEffect } from 'react';
import { Form, TextField, Select, SelectBox } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import pickBy from 'lodash/pickBy';
import forEach from 'lodash/forEach';
import includes from 'lodash/includes';
import isEmpty from 'lodash/isEmpty';
import countBy from 'lodash/countBy';
import keys from 'lodash/keys';
import PlatForm from './Platform';
import { handlePromptError } from '../../../../../utils';

import './index.less';

const { Option } = Select;

const ImportForm = injectIntl(observer((props) => {
  const { dataSet, tableDs, selectedDs, record, AppStore, projectId, intl: { formatMessage }, intlPrefix, prefixCls, refresh } = props;
  const [hasFailed, setHasFailed] = useState(false);

  props.modal.handleOk(async () => {
    if ((record.get('platformType') === 'share' || record.get('platformType') === 'market') && await selectedDs.validate() === false) {
      setHasFailed(true);
      return false;
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
          <Option value="share">
            <FormattedMessage id={`${intlPrefix}.import.type.share`} />
          </Option>
          <Option value="github">
            <FormattedMessage id={`${intlPrefix}.import.type.github`} />
          </Option>
          <Option value="gitlab">
            <FormattedMessage id={`${intlPrefix}.import.type.gitlab`} />
          </Option>
          <Option value="market">
            <FormattedMessage id={`${intlPrefix}.import.type.market`} />
          </Option>
        </SelectBox>
      </Form>
      {record.get('platformType') === 'share' || record.get('platformType') === 'market' ? (
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
