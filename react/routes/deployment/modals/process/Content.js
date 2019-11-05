import React, { Fragment, useEffect } from 'react';
import { Table } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import isEmpty from 'lodash/isEmpty';
import UserInfo from '../../../../components/userInfo';
import { useProcessStore } from './stores';

const { Column } = Table;

export default injectIntl(observer((props) => {
  const {
    pipelineDs,
    deployStore,
    refresh,
    intlPrefix,
    prefixCls,
    modal,
    AppState: { currentMenuType: { projectId } },
  } = useProcessStore();

  useEffect(() => {
    if (pipelineDs.selected && pipelineDs.selected.length) {
      modal.update({ okProps: { disabled: false } });
    } else {
      modal.update({ okProps: { disabled: true } });
    }
  }, [pipelineDs.selected]);

  modal.handleOk(async () => {
    const pipelineIds = map(pipelineDs.selected, (record) => record.get('id'));
    if (isEmpty(pipelineIds)) {
      return true;
    }
    try {
      if (await deployStore.startPipeline(projectId, pipelineIds) !== false) {
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
      <Table dataSet={pipelineDs}>
        <Column name="name" />
        <Column name="createUserName" renderer={renderCreator} />
      </Table>
    </div>
  );
}));
