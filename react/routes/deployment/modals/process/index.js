import React, { Fragment, useEffect } from 'react';
import { Table } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import isEmpty from 'lodash/isEmpty';
import UserInfo from '../../../../components/userInfo';

const { Column } = Table;

export default injectIntl(observer(({ store, dataSet, refresh, projectId, intlPrefix, prefixCls, modal }) => {
  useEffect(() => {
    dataSet.query();
  }, []);

  useEffect(() => {
    if (dataSet.selected && dataSet.selected.length) {
      modal.update({ okProps: { disabled: false } });
    } else {
      modal.update({ okProps: { disabled: true } });
    }
  }, [dataSet.selected]);

  modal.handleOk(async () => {
    const pipelineIds = map(dataSet.selected, (record) => record.get('id'));
    if (isEmpty(pipelineIds)) {
      return true;
    }
    try {
      if (await store.startPipeline(projectId, pipelineIds) !== false) {
        refresh();
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  });

  function renderCreator({ value, record }) {
    return (
      <UserInfo
        name={record.get('createUserRealName') || ''}
        id={value}
        avatar={record.get('createUserUrl')}
      />
    );
  }

  return (
    <div className={`${prefixCls}-process-wrap`}>
      <Table dataSet={dataSet}>
        <Column name="name" />
        <Column name="createUserName" renderer={renderCreator} />
      </Table>
    </div>
  );
}));
