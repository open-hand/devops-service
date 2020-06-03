import React, { Fragment } from 'react';
import { observer } from 'mobx-react-lite';
import { Action } from '@choerodon/boot';
import { Table, Tooltip } from 'choerodon-ui/pro';
import StatusTags from '../../../../../components/status-tag';
import { useResourceStore } from '../../../stores';
import { usePVCStore } from './stores';
import Modals from './modals';
import ResourceListTitle from '../../components/resource-list-title';

import './index.less';

const { Column } = Table;
const statusStyle = {
  width: 54,
  marginRight: 8,
  height: '.16rem',
  lineHeight: '.16rem',
};

const pvcContent = observer((props) => {
  const {
    prefixCls,
    intlPrefix,
    resourceStore: { getSelectedMenu: { parentId } },
    treeDs,
  } = useResourceStore();

  const {
    tableDs,
    intl: { formatMessage },
  } = usePVCStore();

  function refresh() {
    tableDs.query();
    tableDs.query();
  }

  function getEnvIsNotRunning() {
    const envRecord = treeDs.find((record) => record.get('key') === parentId);
    const connect = envRecord.get('connect');
    return !connect;
  }

  function renderName({ record, value }) {
    const status = record.get('status');
    let color = 'rgba(0, 0, 0, 0.26)';
    switch (status) {
      case 'Pending':
      case 'Terminating':
      case 'Operating':
        color = '#4D90FE';
        break;
      case 'Bound':
        color = '#FFB100';
        break;
      case 'Lost':
        color = 'rgba(0, 0, 0, 0.26)';
        break;
      default:
    }
    if (status) {
      return (
        <Fragment>
          <StatusTags
            name={status}
            color={color}
            style={statusStyle}
          />
          <Tooltip title={value}>
            <span>
              {value}
            </span>
          </Tooltip>
        </Fragment>
      );
    }
  }

  function handleDelete() {
    const record = tableDs.current;
    const modalProps = {
      title: formatMessage({ id: `${intlPrefix}.pvc.delete.title` }, { name: record.get('name') }),
      children: formatMessage({ id: `${intlPrefix}.pvc.delete.des` }),
      okText: formatMessage({ id: 'delete' }),
      okProps: { color: 'red' },
      cancelProps: { color: 'dark' },
    };
    tableDs.delete(record, modalProps);
  }

  function renderAction({ record }) {
    const status = record.get('status');
    const disabled = getEnvIsNotRunning() || status === 'Terminating' || status === 'Operating';
    if (disabled) {
      return;
    }
    const action = [
      {
        service: ['choerodon.code.project.deploy.app-deployment.resource.ps.delete-pvc'],
        text: formatMessage({ id: 'delete' }),
        action: handleDelete,
      },
    ];
    return (<Action data={action} />);
  }

  return (
    <div className={`${prefixCls}-PVC`}>
      <Modals />
      <ResourceListTitle type="pvcs" />
      <Table
        dataSet={tableDs}
        border={false}
        queryBar="bar"
      >
        <Column name="name" renderer={renderName} sortable />
        <Column renderer={renderAction} width={70} />
        <Column name="pvName" />
        <Column name="type" />
        <Column name="accessModes" sortable />
        <Column name="requestResource" />
      </Table>
    </div>
  );
});

export default pvcContent;
