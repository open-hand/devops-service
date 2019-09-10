import React, { Fragment } from 'react';
import { Permission, Action } from '@choerodon/master';
import { Table } from 'choerodon-ui/pro';
import { Tooltip } from 'choerodon-ui';
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
      <Fragment>
        <StatusTags name={status} colorCode={status} />
        <Tooltip title={nodeName}>
          <span className="c7n-cls-table-one-line-omit c7n-cls-span-omit">
            {nodeName}
          </span>
        </Tooltip>
      </Fragment>
    );   
  };

  const renderCm = (record, type) => (<div className="c7n-cls-table-cm">
    <span className="c7n-cls-up" />
    <Tooltip title={`${record.get(`${type}Limit`)} (${record.get(`${type}LimitPercentage`)})`}>
      <span>{`${record.get(`${type}Request`)} (${record.get(`${type}RequestPercentage`)})`}</span>
    </Tooltip>
    <span className="c7n-cls-down" />
    <Tooltip title={`${record.get(`${type}Limit`)} (${record.get(`${type}LimitPercentage`)})`}>
      <span className="c7n-cls-table-one-line-omit">{`${record.get(`${type}Limit`)} (${record.get(`${type}LimitPercentage`)})`}</span>
    </Tooltip>
  </div>);

  const renderType = ({ value: type }) => (
    <Tooltip title={type}>
      <span className="c7n-cls-table-one-line-omit c7n-cls-span-omit">{type}</span>
    </Tooltip>
  );

  const renderCpu = ({ record }) => renderCm(record, 'cpu');
  const renderMemory = ({ record }) => renderCm(record, 'memory');
  
  return (
    <Table
      dataSet={NodeListDs}
      border={false}
      queryBar="none"
    >
      <Column width={100} header={formatMessage({ id: `${intlPrefix}.node.ip` })} renderer={renderStatusName} />
      <Column name="type" width={90} renderer={renderType} />
      <Column header={formatMessage({ id: `${intlPrefix}.node.cpu` })} renderer={renderCpu} />
      <Column header={formatMessage({ id: `${intlPrefix}.node.memory` })} renderer={renderMemory} />
      <Column name="createTime" width={140} />
    </Table>
  );
};

export default NodeList;
