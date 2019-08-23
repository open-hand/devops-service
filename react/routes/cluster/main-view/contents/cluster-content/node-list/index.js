import React from 'react';
import { Permission, Action } from '@choerodon/master';
import { Table } from 'choerodon-ui/pro';
import { useClusterContentStore } from '../stores';
import StatusTags from '../../../../../../components/status-tag';

import './index.less';

const { Column } = Table;
const NodeList = () => {
  const { intlPrefix, formatMessage,
    NodeListDs } = useClusterContentStore();

  const renderStatusName = ({ record }) => {
    const status = record.get('status');
    const nodeName = record.get('nodeName');
    return (
      <div>
        <StatusTags name={status} colorCode={status} />
        <span>{nodeName}</span>
      </div>
    );   
  };

  const renderCm = (record, type) => (<div className="c7n-cls-table-cm">
    <span className="c7n-cls-up" />
    {`${record.get(`${type}Request`)} (${record.get(`${type}RequestPercentage`)})`}
    <span className="c7n-cls-down" />
    {`${record.get(`${type}Limit`)} (${record.get(`${type}LimitPercentage`)})`}
  </div>);

  const renderCpu = ({ record }) => renderCm(record, 'cpu');
  const renderMemory = ({ record }) => renderCm(record, 'memory');
  
  return (
    <Table
      dataSet={NodeListDs}
      border={false}
      queryBar="none"
    >
      <Column width={150} header={formatMessage({ id: `${intlPrefix}.node.ip` })} renderer={renderStatusName} />
      <Column name="type" width={90} />
      <Column width={215} header={formatMessage({ id: `${intlPrefix}.node.cpu` })} renderer={renderCpu} />
      <Column header={formatMessage({ id: `${intlPrefix}.node.memory` })} renderer={renderMemory} />
      <Column name="createTime" width={140} />
    </Table>
  );
};

export default NodeList;
