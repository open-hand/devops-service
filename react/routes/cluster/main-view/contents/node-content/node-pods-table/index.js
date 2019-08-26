import React, { Fragment } from 'react';
import { Permission, Action } from '@choerodon/master';
import { Table } from 'choerodon-ui/pro';
import StatusTags from '../../../../../../components/status-tag';
import TimePopover from '../../../../../../components/timePopover';
import MouserOverWrapper from '../../../../../../components/MouseOverWrapper';
import { useNodeContentStore } from '../stores';
import './index.less';

const { Column } = Table;
const NodePodsTable = () => {
  const { intlPrefix, formatMessage,
    NodePodsDs } = useNodeContentStore();

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
  
  return (
    <Table
      dataSet={NodePodsDs}
      border={false}
      queryBar="none"
      className="c7ncd-node-pods-table"
    >
      <Column header={formatMessage({ id: 'status' })} renderer={renderStatus} />
      {/* <Column header={formatMessage({ id: 'node.rTimes' })} name="node.rTimes" /> */}
      <Column header={formatMessage({ id: 'ciPipeline.createdAt' })} renderer={renderCreationDate} />
    </Table>
  );
};

export default NodePodsTable;
