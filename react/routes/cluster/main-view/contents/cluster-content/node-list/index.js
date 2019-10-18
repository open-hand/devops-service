import React, { Fragment } from 'react';
import { Permission, Action } from '@choerodon/boot';
import { Table } from 'choerodon-ui/pro';
import { Tooltip } from 'choerodon-ui';
import { useClusterContentStore } from '../stores';
import StatusTags from '../../../../../../components/status-tag';

import './index.less';

const { Column } = Table;
const NodeList = () => {
  const {
    intlPrefix,
    formatMessage,
    NodeListDs,
  } = useClusterContentStore();

  const renderStatusName = ({ record }) => {
    const status = record.get('status');
    const nodeName = record.get('nodeName');
    return (
      <Fragment>
        <StatusTags name={status} colorCode={status} />
        <Tooltip title={nodeName}>
          <span>
            {nodeName}
          </span>
        </Tooltip>
      </Fragment>
    );
  };

  const renderCm = (record, type) => {
    const content = (<div className="c7n-cls-table-cm">
      <span className="c7n-cls-up" />
      <span>{`${record.get(`${type}Request`)} (${record.get(`${type}RequestPercentage`)})`}</span>
      <span className="c7n-cls-down" />
      <span className="c7n-cls-table-one-line-omit">{`${record.get(`${type}Limit`)} (${record.get(`${type}LimitPercentage`)})`}</span>
    </div>);
    return (
      <Tooltip title={content}>
        {content}
      </Tooltip>
    );
  };

  const renderType = ({ value: type }) => (
    <Tooltip title={type}>
      <span>{type}</span>
    </Tooltip>
  );

  const renderTime = ({ value: time }) => (
    <Tooltip title={time}>
      <span>{time}</span>
    </Tooltip>
  );

  const renderCpu = ({ record }) => renderCm(record, 'cpu');
  const renderMemory = ({ record }) => renderCm(record, 'memory');

  return (
    <div className="c7ncd-cluster-table">
      <Table
        dataSet={NodeListDs}
        border={false}
        queryBar="none"
      >
        <Column width={150} header={formatMessage({ id: `${intlPrefix}.node.ip` })} renderer={renderStatusName} />
        <Column name="type" minWidth={80} renderer={renderType} />
        <Column header={formatMessage({ id: `${intlPrefix}.node.cpu` })} renderer={renderCpu} />
        <Column header={formatMessage({ id: `${intlPrefix}.node.memory` })} renderer={renderMemory} />
        <Column name="createTime" width={150} renderer={renderTime} />
      </Table>
    </div>
  );
};

export default NodeList;
