import React, { Fragment } from 'react';
import { observer } from 'mobx-react-lite';
import { Table } from 'choerodon-ui/pro';
import StatusIcon from '../../../../../components/StatusIcon';
import StatusTags from '../../../../../components/status-tag';
import AppName from '../../../../../components/appName';
import PodStatus from './components/pod-status';
import { useResourceStore } from '../../../stores';
import { useIstListStore } from './stores';
import Modals from './modals';
import UploadIcon from './components/upload-icon';
import ResourceListTitle from '../../components/resource-list-title';

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
    const status = record.get('status');
    const error = record.get('error');
    return (
      <StatusIcon
        name={value}
        width={0.2}
        status={status || ''}
        error={error || ''}
      />
    );
  }

  function renderVersion({ record }) {
    return <UploadIcon dataSource={record.toData()} />;
  }

  function renderAppName({ value, record }) {
    const appServiceType = record.get('appServiceType');
    let iconType;
    if (appServiceType === 'share_service') {
      iconType = 'share';
    } else if (appServiceType === 'market_service') {
      iconType = 'application_market';
    } else {
      iconType = 'project';
    }
    return (
      <AppName
        width={0.18}
        name={value}
        showIcon={!!record.get('projectId')}
        self={iconType}
        isInstance
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
      <ResourceListTitle type="instances" />
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
