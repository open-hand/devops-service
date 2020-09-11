import React, { Fragment, useState } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Action } from '@choerodon/boot';
import { Tooltip, Icon, Popover } from 'choerodon-ui';
import { Table, Modal } from 'choerodon-ui/pro';
import _ from 'lodash';
import classnames from 'classnames';
import MouserOverWrapper from '../../../../../../components/MouseOverWrapper/MouserOverWrapper';
import StatusIcon from '../../../../../../components/StatusIcon';
import { useResourceStore } from '../../../../stores';
import { useApplicationStore } from '../stores';
import EditNetwork from '../modals/network2';
import { useMainStore } from '../../../stores';
import DomainForm from '../../../components/domain-form';

import './index.less';


const { Column } = Table;
const editNetWorkKey = Modal.key();
const editDomainKey = Modal.key();
const modalStyle = {
  width: 740,
};

const Networking = observer(() => {
  const {
    prefixCls,
    intlPrefix,
    resourceStore: { getSelectedMenu: { id, parentId } },
    intl: { formatMessage },
    AppState: { currentMenuType: { id: projectId } },
    treeDs,
  } = useResourceStore();
  const {
    netDs,
    domainStore,
    networkStore,
  } = useApplicationStore();
  const { mainStore: { openDeleteModal } } = useMainStore();

  function refresh() {
    treeDs.query();
    netDs.query();
  }

  function getEnvIsNotRunning() {
    const envRecord = treeDs.find((record) => record.get('key') === parentId);
    const connect = envRecord.get('connect');
    return !connect;
  }

  function renderName({ value, record }) {
    const status = record.get('status');
    const error = record.get('error');
    const disabled = getEnvIsNotRunning() || status === 'operating';

    return (<StatusIcon
      name={value}
      status={status || ''}
      error={error || ''}
      clickAble={!disabled}
      onClick={openNetworkEdit}
      permissionCode={['choerodon.code.project.deploy.app-deployment.resource.ps.net-detail']}
    />);
  }

  function renderTargetType({ record }) {
    const { instances, selectors, targetAppServiceId } = record.get('target') || {};
    const appId = record.get('appServiceId');

    let type = 'EndPoints';
    if (targetAppServiceId) {
      type = formatMessage({ id: 'all_instance' });
    } else if (instances && instances.length) {
      type = formatMessage({ id: 'instance' });
    } else if (selectors) {
      type = formatMessage({ id: 'label' });
    }

    return <span>{type}</span>;
  }

  function renderTarget({ record }) {
    const { instances, selectors, endPoints, targetAppServiceId, targetAppServiceName } = record.get('target') || {};
    const node = [];
    const port = [];
    const len = endPoints ? 2 : 1;
    if (targetAppServiceId && targetAppServiceName) {
      node.push(
        <div className="net-target-item">
          <span>{targetAppServiceName}</span>
        </div>,
      );
    } else if (instances && instances.length) {
      _.forEach(instances, ({ id: itemId, code, status }) => {
        const targetClass = classnames({
          'net-target-item': true,
          'net-target-item-failed': status !== 'operating' && status !== 'running',
        });
        if (code) {
          node.push(
            <div className={targetClass} key={itemId}>
              <Tooltip
                title={formatMessage({ id: status || `${intlPrefix}.application.net.deleted` })}
                placement="top"
              >
                {code}
              </Tooltip>
            </div>,
          );
        }
      });
    }
    if (!_.isEmpty(selectors)) {
      _.forEach(selectors, (value, key) => node.push(
        <div className="net-target-item" key={key}>
          <span>{key}</span>=<span>{value}</span>
        </div>,
      ));
    }
    if (endPoints) {
      const targetIps = _.split(_.keys(endPoints)[0], ',');
      const portList = _.values(endPoints)[0];
      _.map(targetIps, (item, index) => node.push(
        <div className="net-target-item" key={index}>
          <span>{item}</span>
        </div>,
      ));
      _.map(portList, (item, index) => {
        port.push(
          <div className="net-target-item" key={index}>
            <span>{item.port}</span>
          </div>,
        );
      });
    }
    return (
      <Fragment>
        {
          _.map([node, port], (item, index) => {
            if (item.length) {
              return (
                <div className="net-target-wrap" key={index}>
                  {item[0]}
                  {endPoints && (<div className="net-target-Ip">{item[1] || null}</div>)}
                  {item.length > len && (
                    <Popover
                      arrowPointAtCenter
                      placement="bottomRight"
                      content={<Fragment>{item}</Fragment>}
                      overlayClassName={`${prefixCls}-application-net`}
                    >
                      <Icon type="expand_more" className="net-expend-icon" />
                    </Popover>
                  )}
                </div>
              );
            }
          })
        }
      </Fragment>
    );
  }

  function renderConfigType({ record }) {
    const { externalIps, ports } = record.get('config') || {};
    const loadBalanceIp = record.get('loadBalanceIp');
    const type = record.get('type');
    const iPArr = [];
    const portArr = [];
    if (externalIps && externalIps.length) {
      _.forEach(externalIps, (item) => (
        iPArr.push(
          <div key={item} className="net-config-item">
            {item}
          </div>,
        )
      ));
    }
    if (ports && ports.length) {
      _.forEach(ports, ({ nodePort, port, targetPort }) => {
        portArr.push(
          <div key={port} className="net-config-item">
            {nodePort || (type !== 'ClusterIP' && formatMessage({ id: 'null' }))} {port} {targetPort}
          </div>,
        );
      });
    }

    let content = null;
    switch (type) {
      case 'ClusterIP':
        content = (
          <Fragment>
            <div className="net-config-wrap">
              <div className="net-type-title">
                <FormattedMessage id={`${intlPrefix}.application.net.ip`} />
              </div>
              <div>{externalIps ? iPArr : '-'}</div>
            </div>
            <div className="net-config-wrap">
              <div className="net-type-title">
                <FormattedMessage id={`${intlPrefix}.application.net.port`} />
              </div>
              <div>{portArr}</div>
            </div>
          </Fragment>
        );
        break;
      case 'NodePort':
        content = (
          <Fragment>
            <div className="net-config-item">
              <FormattedMessage id={`${intlPrefix}.application.net.nport`} />
            </div>
            <div>{portArr}</div>
          </Fragment>
        );
        break;
      case 'LoadBalancer':
        content = (
          <Fragment>
            <div className="net-config-wrap">
              <div className="net-type-title">
                <FormattedMessage id={`${intlPrefix}.application.net.nport`} />
              </div>
              <div>{portArr}</div>
            </div>
            {loadBalanceIp && (
              <div className="net-config-wrap">
                <div className="net-type-title">
                  <span>LoadBalancer IP</span>
                </div>
                <div>{loadBalanceIp}</div>
              </div>
            )}
          </Fragment>
        );
        break;
      default:
        break;
    }

    return (
      <div className="net-config-content">
        <span className="net-config-type">{type}</span>
        <Tooltip
          arrowPointAtCenter
          placement="bottomRight"
          title={content}
        >
          <Icon type="expand_more" className="net-expend-icon" />
        </Tooltip>
      </div>
    );
  }

  function renderAction({ record }) {
    const status = record.get('status');
    const netId = record.get('id');
    const name = record.get('name');
    const disabled = getEnvIsNotRunning() || status === 'operating';
    if (disabled) {
      return null;
    }
    const buttons = [
      {
        service: ['choerodon.code.project.deploy.app-deployment.resource.ps.delete-net'],
        text: formatMessage({ id: 'delete' }),
        action: () => openDeleteModal(parentId, netId, name, 'service', refresh),
      },
    ];

    return (<Action data={buttons} />);
  }

  function openDomainEdit(itemId) {
    Modal.open({
      key: editDomainKey,
      style: modalStyle,
      drawer: true,
      title: formatMessage({ id: 'domain.update.head' }),
      children: <DomainForm
        envId={parentId}
        appServiceId={id}
        ingressId={itemId}
        ingressStore={domainStore}
        refresh={refresh}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
      />,
      okText: formatMessage({ id: 'save' }),
    });
  }

  function openNetworkEdit() {
    Modal.open({
      key: editNetWorkKey,
      title: formatMessage({ id: 'network.header.create' }),
      style: { width: 740 },
      okText: formatMessage({ id: 'edit' }),
      drawer: true,
      children: <EditNetwork
        envId={parentId}
        appId={id}
        networkStore={networkStore}
        refresh={refresh}
        networkId={netDs.current.get('id')}
      />,
    });
  }

  function renderExpandedRow({ record }) {
    const devopsIngressDTOS = record.get('devopsIngressVOS');
    const content = devopsIngressDTOS && devopsIngressDTOS.length ? (
      _.map(devopsIngressDTOS, ({ id: itemId, name, domain, error, status, pathList }) => {
        const buttons = [
          {
            service: ['choerodon.code.project.deploy.app-deployment.resource.ps.delete-domain'],
            text: formatMessage({ id: 'delete' }),
            action: () => openDeleteModal(parentId, itemId, name, 'ingress', refresh),
          },
        ];
        const disabled = getEnvIsNotRunning() || status === 'operating';
        return (
          <div key={itemId} className="net-expandedRow-detail">
            <FormattedMessage id={`${intlPrefix}.application.net.ingress`} />：
            <div className="net-ingress-text">
              <StatusIcon
                name={name}
                status={status}
                error={error}
                clickAble={!disabled}
                onClick={() => openDomainEdit(itemId)}
                permissionCode={['choerodon.code.project.deploy.app-deployment.resource.ps.domain-detail']}
              />
            </div>
            {!disabled && (
              <div className="net-ingress-action">
                <Action data={buttons} />
              </div>
            )}
            <FormattedMessage id="address" />：
            <div className="net-ingress-text">
              <MouserOverWrapper text={domain} width={0.2} style={{ display: 'block' }}>
                {domain}
              </MouserOverWrapper>
            </div>
            <FormattedMessage id="path" />：
            <div className="net-ingress-text net-ingress-path">
              <MouserOverWrapper text={pathList[0] ? pathList[0].path : ''} width={0.2}>
                {pathList[0] ? pathList[0].path : ''}
              </MouserOverWrapper>
              {pathList.length > 1 && (
                <Tooltip
                  title={_.map(pathList, ({ path }) => <div>{path}</div>)}
                >
                  <Icon type="expand_more" className="net-expend-icon" />
                </Tooltip>
              )}
            </div>
          </div>
        );
      })
    ) : (<span className="net-no-ingress">{formatMessage({ id: `${intlPrefix}.application.net.empty` })}</span>);
    return (
      <div className="net-expandedRow-content">
        {content}
      </div>
    );
  }

  return (
    <div className={`${prefixCls}-application-net`}>
      <div className="c7ncd-tab-table">
        <Table
          dataSet={netDs}
          border={false}
          queryBar="none"
          expandedRowRenderer={renderExpandedRow}
        >
          <Column name="name" renderer={renderName} />
          <Column renderer={renderAction} width={70} />
          <Column renderer={renderTargetType} header={formatMessage({ id: `${intlPrefix}.application.net.targetType` })} />
          <Column renderer={renderTarget} header={formatMessage({ id: `${intlPrefix}.application.net.target` })} />
          <Column name="type" renderer={renderConfigType} />
        </Table>
      </div>
    </div>
  );
});

export default Networking;
