import React from 'react';
import { observer } from 'mobx-react-lite';
import { Table } from 'choerodon-ui/pro';
import StatusIcon from '../../../../../components/StatusIcon';
import AppName from '../../../../../components/appName';
import PodStatus from './components/pod-status';
import { useResourceStore } from '../../../stores';
import { useIstListStore } from './stores';
import Modals from './modals';
import UploadIcon from './components/upload-icon';

import './index.less';


const { Column } = Table;

const Content = observer(() => {
  const {
    prefixCls,
    intlPrefix,
    resourceStore: { getSelectedMenu: { parentId } },
    AppState: { currentMenuType: { id } },
  } = useResourceStore();
  const {
    istListDs,
    intl: { formatMessage },
  } = useIstListStore();

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

  function renderVersion({ record }) {
    return <UploadIcon dataSource={record.toData()} />;
  }

  function renderAppName({ value, record }) {
    return (
      <AppName
        width={0.18}
        name={value}
        showIcon={!!record.get('projectId')}
        self={record.get('projectId') === Number(id)}
      />
    );
  }

  function renderPods({ record }) {
    const dataSource = record.toData();
    return <PodStatus dataSource={dataSource} />;
  }

  return (
    <div className={`${prefixCls}-instance-table`}>
      <Modals />
      <Table
        dataSet={istListDs}
        border={false}
        queryBar="bar"
      >
        <Column name="code" renderer={renderName} />
        <Column name="versionName" renderer={renderVersion} />
        <Column name="appServiceName" renderer={renderAppName} />
        <Column renderer={renderPods} width="1rem" header={formatMessage({ id: `${intlPrefix}.instance.pod.status` })} />
      </Table>
    </div>
  );
});

export default Content;
