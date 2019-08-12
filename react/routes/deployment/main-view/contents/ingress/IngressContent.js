import React, { Fragment, useState } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Action } from '@choerodon/boot';
import {
  Tooltip,
  Icon,
  Popover,
} from 'choerodon-ui';
import { Table } from 'choerodon-ui/pro';
import map from 'lodash/map';
import classnames from 'classnames';
import StatusIcon from '../../../../../components/StatusIcon';
import { useDeploymentStore } from '../../../stores';
import { useIngressStore } from './stores';
import Modals from './modals';
import MouserOverWrapper from '../../../../../components/MouseOverWrapper';
import StatusTags from '../../../../../components/StatusTags';

import './index.less';
import DomainModal from '../application/modals/domain';

const serviceStyle = {
  minWidth: 40,
  marginRight: 8,
};


const { Column } = Table;

const IngressContent = observer(() => {
  const {
    prefixCls,
    intlPrefix,
    deploymentStore: { getSelectedMenu: { parentId } },
  } = useDeploymentStore();
  const {
    ingressDs,
    intl: { formatMessage },
    ingressStore,
  } = useIngressStore();

  const [showModal, setShowModal] = useState(false);

  function refresh() {
    ingressDs.query();
  }

  function renderName({ value, record }) {
    return (
      <StatusIcon
        name={value}
        status={record.get('commandStatus') || ''}
        error={record.get('error') || ''}
      />
    );
  }

  function renderPath({ value }) {
    return (
      map(value, ({ path }) => (
        <div
          className="c7n-network-col_border"
          key={path}
        >
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
    const buttons = [
      {
        service: [],
        text: formatMessage({ id: 'edit' }),
        action: openModal,
      },
      {
        service: ['devops-service.devops-ingress.delete'],
        text: formatMessage({ id: 'delete' }),
        action: handleDelete,
      },
    ];

    return (<Action data={buttons} />);
  }

  function handleDelete() {
    ingressDs.delete(ingressDs.current);
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
        queryBar="none"
      >
        <Column name="name" renderer={renderName} />
        <Column renderer={renderAction} width="0.7rem" />
        <Column name="domain" />
        <Column name="pathList" renderer={renderPath} />
        <Column name="pathList" renderer={renderService} header={formatMessage({ id: 'network' })} />
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
