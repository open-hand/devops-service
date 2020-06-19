import React, { Fragment, useState, useEffect, useMemo } from 'react';
import { Form, TextField, Select, SelectBox, Spin } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Choerodon } from '@choerodon/boot';
import keys from 'lodash/keys';
import countBy from 'lodash/countBy';
import pickBy from 'lodash/pickBy';
import isEmpty from 'lodash/isEmpty';
import map from 'lodash/map';
import PlatForm from './Platform';
import Tips from '../../../../../components/new-tips';
import { useImportAppServiceStore } from './stores';

import './index.less';

const { Option } = Select;

const ImportForm = injectIntl(observer((props) => {
  const {
    AppState: { currentMenuType: { projectId } },
    intl: { formatMessage },
    intlPrefix,
    prefixCls,
    IMPORT_METHOD,
    refresh,
    modal,
    importDs,
    selectedDs,
    importStore,
  } = useImportAppServiceStore();
  const record = useMemo(() => importDs.current || importDs.records[0], [importDs.current]);
  const [hasFailed, setHasFailed] = useState(false);

  useEffect(() => {
    setHasFailed(false);
  }, [record.get('platformType')]);

  modal.handleOk(async () => {
    if (record.get('platformType') === 'share' || record.get('platformType') === 'market') {
      if (!selectedDs.length) return true;
      const result = await checkData();
      if (!result) {
        return false;
      }
    }
    try {
      if ((await importDs.submit()) !== false) {
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
    const { listCode, listName, repeatName, repeatCode } = getRepeatData(lists);

    try {
      importStore.setSkipCheck(true);
      const res = await importStore.batchCheck(projectId, listCode, listName);
      await selectedDs.validate();
      importStore.setSkipCheck(false);
      if (res && isEmpty(repeatName) && isEmpty(repeatCode) && isEmpty(res.listCode) && isEmpty(res.listName)) {
        setHasFailed(false);
        return true;
      }
      setHasFailed(true);
      return false;
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
            <Option value={item} key={item}>
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
          <PlatForm checkData={checkData} />
          {hasFailed && (
            <span className={`${prefixCls}-import-wrap-failed`}>
              {formatMessage({ id: `${intlPrefix}.platform.failed` })}
            </span>
          )}
        </Fragment>
      ) : (
        <Form record={record} style={{ width: '3.6rem' }}>
          {record.get('platformType') === 'github' && (
            <SelectBox name="isTemplate">
              <Option value>{formatMessage({ id: `${intlPrefix}.github.system` })}</Option>
              <Option value={false}>{formatMessage({ id: `${intlPrefix}.github.custom` })}</Option>
            </SelectBox>
          )}
          {record.get('platformType') === 'github' && record.get('isTemplate') && (
            <Select name="githubTemplate" searchable />
          )}
          <TextField
            name="repositoryUrl"
            disabled={record.get('platformType') === 'github' && record.get('isTemplate')}
            addonAfter={<Tips helpText={formatMessage({ id: `${intlPrefix}.address.${record.get('platformType')}.tips` })} />}
          />
          {record.get('platformType') === 'gitlab' && <TextField name="accessToken" />}
          <Select name="type" clearButton={false} />
          <TextField name="name" />
          <TextField name="code" />
        </Form>
      )}
    </div>
  );
}));

export default ImportForm;
