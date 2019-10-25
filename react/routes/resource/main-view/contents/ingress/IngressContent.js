import React, { Fragment, useState } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Action } from '@choerodon/boot';
import { Modal, Table } from 'choerodon-ui/pro';
import map from 'lodash/map';
import StatusIcon from '../../../../../components/StatusIcon';
import StatusTags from '../../../../../components/status-tag';
import { useResourceStore } from '../../../stores';
import { useIngressStore } from './stores';
import Modals from './modals';
import MouserOverWrapper from '../../../../../components/MouseOverWrapper';
import DomainModal from './modals/domain-create';
import { useMainStore } from '../../stores';
import ResourceListTitle from '../../components/resource-list-title';

import './index.less';

const modalKey = Modal.key();
const modalStyle = {
  width: 740,
};

const serviceStyle = {
  minWidth: 40,
  marginRight: 8,
  height: '.16rem',
  lineHeight: '.16rem',
};


const { Column } = Table;

const IngressContent = observer(() => {
  const {
    prefixCls,
    intlPrefix,
    resourceStore: { getSelectedMenu: { parentId } },
    treeDs,
  } = useResourceStore();
  const {
    ingressDs,
    intl: { formatMessage },
  } = useIngressStore();
  const {
    ingressStore,
    mainStore: { openDeleteModal },
  } = useMainStore();

  function refresh() {
    treeDs.query();
    ingressDs.query();
  }

  function getEnvIsNotRunning() {
    const envRecord = treeDs.find((record) => record.get('key') === parentId);
    const connect = envRecord.get('connect');
    return !connect;
  }

  function renderName({ record }) {
    const name = record.get('name');
    const status = record.get('status');
    const error = record.get('error');
    const disabled = getEnvIsNotRunning() || status === 'operating';
    return (
      <Fragment>
        <StatusIcon
          name={name}
          status={status || ''}
          error={error || ''}
          clickAble={!disabled}
          onClick={openModal}
          permissionCode={['devops-service.devops-ingress.update']}
        />
      </Fragment>
    );
  }

  function renderDomain({ value }) {
    return <MouserOverWrapper text={value} width={0.18}>{value}</MouserOverWrapper>;
  }

  function renderPath({ value }) {
    return (
      map(value, ({ path }) => (
        <div key={path} className={`${prefixCls}-ingress-path`}>
          <MouserOverWrapper text={path} width={0.1}>
            <span>{path}</span>
          </MouserOverWrapper>
        </div>
      ))
    );
  }

  function renderService({ record }) {
    return (
      map(record.get('pathList'), ({ serviceStatus, serviceName }) => (
        <div
          className="c7n-network-service"
          key={record.get('id')}
        >
          <StatusTags
            colorCode={serviceStatus}
            name={formatMessage({ id: serviceStatus })}
            style={serviceStyle}
          />
          <MouserOverWrapper text={serviceName} width={0.1}>
            {serviceName}
          </MouserOverWrapper>
        </div>
      ))
    );
  }

  function renderAction({ record }) {
    const status = record.get('status');
    const disabled = getEnvIsNotRunning() || status === 'operating';
    if (disabled) {
      return null;
    }
    const id = record.get('id');
    const name = record.get('name');
    const buttons = [
      {
        service: ['devops-service.devops-ingress.delete'],
        text: formatMessage({ id: 'delete' }),
        action: () => openDeleteModal(parentId, id, name, 'ingress', refresh),
      },
    ];

    return (<Action data={buttons} />);
  }

  function openModal() {
    Modal.open({
      key: modalKey,
      style: modalStyle,
      drawer: true,
      title: formatMessage({ id: 'domain.update.head' }),
      children: <DomainModal
        envId={parentId}
        id={ingressDs.current.get('id')}
        type="edit"
        store={ingressStore}
        refresh={refresh}
      />,
      okText: formatMessage({ id: 'save' }),
    });
  }

  return (
    <div className={`${prefixCls}-ingress-table`}>
      <Modals />
      <ResourceListTitle type="ingresses" />
      <Table
        dataSet={ingressDs}
        border={false}
        queryBar="bar"
        rowHeight="auto"
      >
        <Column name="name" renderer={renderName} />
        <Column renderer={renderAction} width="0.7rem" />
        <Column name="domain" renderer={renderDomain} />
        <Column name="pathList" renderer={renderPath} />
        <Column renderer={renderService} header={formatMessage({ id: 'network' })} />
      </Table>
    </div>
  );
});

export default IngressContent;
