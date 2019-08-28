import React, { Fragment, useState } from 'react';
import { Permission, Action } from '@choerodon/master';
import { Table } from 'choerodon-ui/pro';
import StatusTags from '../../../../../../components/status-tag';
import TimePopover from '../../../../../../components/timePopover';
import MouserOverWrapper from '../../../../../../components/MouseOverWrapper';
import { useNodeContentStore } from '../stores';
import LogSiderbar from '../../../../../../components/log-siderbar';
import { useClusterStore } from '../../../../stores';

import './index.less';

const { Column } = Table;
const NodePodsTable = () => {
  const { intlPrefix, formatMessage,
    NodePodsDs } = useNodeContentStore();
  const { clusterStore: { getSelectedMenu: { parentId } } } = useClusterStore();
  const [visible, setVisible] = useState(false);
  const [record, setRecord] = useState();
  const renderStatus = ({ record }) => {
    const status = record.get('status');
    const name = record.get('name');
    return <Fragment>
      <StatusTags name={status} colorCode={status} />
      <span>{name}</span>
    </Fragment>;
  }; 

  const renderPodName = ({ record }) => {
    const name = record.get('name');
    return <MouserOverWrapper text={name} width={0.2}>{name}</MouserOverWrapper>;
  };

  const renderCreationDate = ({ record }) => {
    const creationDate = record.get('creationDate');
    return <TimePopover content={creationDate} />;
  };

  const logData = [{
    namespace: 'envtestenv',
    podName: 'testlog-zzy-78c96bf788-lxmh5',
    clusterId: 61,
    containerName: 'testlog-zzy',
    logId: '6e79892f-6e7a-4f23-8008-7ff404b09538',
  }];

  function openLog() {
    setVisible(true);
  }
  function closeLog() {
    setVisible(false);
  }
  
  function renderActions({ record }) {
    const actionData = [
      {
        service: [],
        text: formatMessage({ id: 'node.log' }),
        action: () => {
          setRecord(record.toData());
          openLog();
        },
      },
    ];
    return (<Action data={actionData} />);
  }

  return (
    <Fragment>
      <Table
        dataSet={NodePodsDs}
        border={false}
        queryBar="none"
        className="c7ncd-node-pods-table"
      >
        <Column header={formatMessage({ id: 'status' })} renderer={renderStatus} />
        <Column renderer={renderActions} />
        {/* <Column header={formatMessage({ id: 'node.rTimes' })} name="node.rTimes" /> */}
        <Column header={formatMessage({ id: 'ciPipeline.createdAt' })} renderer={renderCreationDate} />
      </Table>
      {visible && <LogSiderbar data={logData} visible={visible} onClose={closeLog} record={record} clusterId={parentId} />}
    </Fragment>
  );
};

export default NodePodsTable;
