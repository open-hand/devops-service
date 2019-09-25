import React, { Fragment, useState } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Action } from '@choerodon/master';
import { Table } from 'choerodon-ui/pro';
import map from 'lodash/map';
import StatusIcon from '../../../../../components/StatusIcon';
import StatusTags from '../../../../../components/status-tag';

import { useResourceStore } from '../../../stores';
import { useIngressStore } from './stores';
import Modals from './modals';
import MouserOverWrapper from '../../../../../components/MouseOverWrapper';
import DomainModal from '../application/modals/domain';
import { useMainStore } from '../../stores';

import './index.less';


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

  const [showModal, setShowModal] = useState(false);

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
    return <MouserOverWrapper text={value} width={0.25}>{value}</MouserOverWrapper>;
  }

  function renderPath({ value }) {
    return (
      map(value, ({ path }) => (
        <div key={path}>
          <span>{path}</span>
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
    setShowModal(true);
  }

  function closeModal(isLoad) {
    setShowModal(false);
    isLoad && refresh();
  }

  return (
    <div className={`${prefixCls}-ingress-table`}>
      <Modals />
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
      {showModal && (
        <DomainModal
          envId={parentId}
          id={ingressDs.current.get('id')}
          visible={showModal}
          type="edit"
          store={ingressStore}
          onClose={closeModal}
        />
      )}
    </div>
  );
});

export default IngressContent;
