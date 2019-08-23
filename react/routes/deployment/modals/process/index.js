import React, { Fragment, useEffect } from 'react';
import { Table } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import UserInfo from '../../../../components/userInfo';

const { Column } = Table;

export default injectIntl(observer(({ store, dataSet, refresh, projectId, intlPrefix, prefixCls, modal }) => {
  useEffect(() => {
    dataSet.query();
  }, []);

  modal.handleOk(async () => {
    const pipelineIds = map(dataSet.selected, (record) => record.get('id'));
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
        name={record.get('createUserRealName') || 'admin'}
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
