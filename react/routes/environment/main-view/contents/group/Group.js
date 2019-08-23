import React from 'react';
import { observer } from 'mobx-react-lite';
import { Action } from '@choerodon/master';
import { Table } from 'choerodon-ui/pro';
import StatusIcon from '../../../../../components/StatusIcon';
import { useEnvironmentStore } from '../../../stores';
import { useEnvGroupStore } from './stores';
import Modals from './modals';

// import './index.less';

const { Column } = Table;

const Group = observer(() => {
  const {
    prefixCls,
    intlPrefix,
    envStore: { getSelectedMenu: { id, name } },
    AppState: { currentMenuType: { id: projectId } },
  } = useEnvironmentStore();
  const {
    groupDs,
  } = useEnvGroupStore();

  function refresh() {
  }

  function renderName({ value, record }) {
    return (
      <StatusIcon
        name={value}
        width={0.2}
        status={record.get('status') || ''}
        error={record.get('error') || ''}
      />
    );
  }

  function renderAppName({ value, record }) {
  }

  function renderPods({ record }) {
  }

  function handleDelete() {
  }

  return (
    <div>
      <h2>{name}</h2>
      <Table
        dataSet={groupDs}
        border={false}
        queryBar="none"
      >
        <Column name="name" renderer={renderName} />
        <Column name="versionName" />
        <Column name="appServiceName" renderer={renderAppName} />
      </Table>
      <Modals />
    </div>
  );
});

export default Group;
