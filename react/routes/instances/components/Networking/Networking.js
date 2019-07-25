import React, { Component, Fragment } from 'react/index';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Content, Permission } from '@choerodon/boot';
import { Modal, Button, Table, Tooltip, Popover, Icon } from 'choerodon-ui';
import PropTypes from 'prop-types';
import _ from 'lodash';
import EnvOverviewStore from '../../../envOverview/stores';
import StatusIcon from '../../../../components/StatusIcon/StatusIcon';
import CreateNetwork from './CreateNetwork';
import CreateIngress from './CreateIngress';

import '../../../main.scss';
import './Networking.scss';

const { Sidebar } = Modal;

@injectIntl
@withRouter
@inject('AppState')
@observer
export default class Networking extends Component {
  state = {
    modalType: '',
    expandedRowKeys: [],
    isExpanded: true,
  };

  componentDidMount() {
    this.loadData();
  };

  componentWillUnmount() {
    const { store } = this.props;
    store.setNetworking([]);
  }

  loadData = () => {
    const {
      AppState: { currentMenuType: { projectId } },
      id,
      store,
    } = this.props;
    store.loadNetworking(projectId, id)
      .then(data => {
        if (data && !data.failed) {
          this.setState({
            expandedRowKeys: _.map(data.list, item => item.id),
          });
        }
      })
      .catch(err => {
        store.changeNetworkingLoading(false);
        Choerodon.handleResponseError(err);
      });
  };

  handleRefresh = () => {
    const {
      store: { getNetworkingPageInfo },
    } = this.props;
    this.tableChange(getNetworkingPageInfo);
  };

  /**
   * table 操作
   * @param pagination
   * @param filters
   * @param sorter
   * @param paras
   */
  tableChange = ({ current, pageSize }) => {
    const {
      store,
      id,
      AppState: { currentMenuType: { projectId } },
    } = this.props;

    store.loadNetworking(
      projectId,
      id,
      current,
      pageSize,
    );
  };

  /**
   * 关闭侧边栏
   */
  onClose = () => {
    const { onClose } = this.props;
    onClose();
  };

  /**
   * 打开创建弹窗
   * @param type 网络或域名
   */
  showModal = (type) => {
    this.setState({ modalType: type });
  };

  closeModal = (flag) => {
    this.setState({ modalType: '' });
    flag && this.loadData();
  };

  /**
   * 全部展开或收起
   */
  expendOrCollapse = () => {
    const { store: { getNetworking } } = this.props;
    this.setState(({ isExpanded }) => ({
      isExpanded: !isExpanded,
      expandedRowKeys: isExpanded ? [] : _.map(getNetworking, ({ id }) => id),
    }));
  };

  /**
   * 点击展开行
   * @param rows 展开的行
   */
  expandedRowsChange = (rows) => {
    this.setState(({ expandedRowKeys }) => ({
      isExpanded: expandedRowKeys.length !== rows.length,
      expandedRowKeys: rows,
    }));
  };

  getExpandedRow = ({ devopsIngressDTOS }) => {
    const {
      intl: { formatMessage },
    } = this.props;
    const content = devopsIngressDTOS && devopsIngressDTOS.length ? (
      _.map(devopsIngressDTOS, ({ id, name, domain, error, status }) => (
        <div key={id} className='c7n-networking-expandedRow-detail'>
          <FormattedMessage id='ist.networking.ingress.name' />
          <div className='c7n-networking-ingress c7n-networking-ingress-name'>
            <StatusIcon
              name={name}
              status={status}
              error={error}
            />
          </div>
          <FormattedMessage id='ist.networking.ingress.address' />
          <span className='c7n-networking-ingress'>{domain}</span>
        </div>
      ))
    ) : (<span className='c7n-networking-no-ingress'>{formatMessage({ id: 'ist.networking.no.ingress' })}</span>);
    return content;
  };

  renderExternalIp = (record) => {
    const { config: { externalIps } } = record;
    const ipDom = _.map(externalIps, item => (
      <div className='c7n-networking-tag-dom' key={item}>
        <div className='c7n-networking-tag'>{item}</div>
      </div>
    ));
    const popoverDom = externalIps && externalIps.length > 2 ? (
      <Popover
        arrowPointAtCenter
        placement='bottomRight'
        getPopupContainer={triggerNode => triggerNode.parentNode}
        content={ipDom}
      >
        <Icon type='expand_more' className='c7n-networking-expend-icon' />
      </Popover>
    ) : null;
    const content = externalIps && externalIps.length ? ipDom[0] : '<none>';

    return (
      <div>
        {content}
        <div className='c7n-networking-tag-more'>
          {ipDom[1] || null}
          {popoverDom}
        </div>
      </div>
    );
  };

  renderPorts = (record) => {
    const {
      intl: { formatMessage },
    } = this.props;
    const { config: { ports } } = record;
    const portDom = _.map(ports, ({ nodePort, port, targetPort }) => (
      <div className='c7n-networking-tag-dom' key={port}>
        <div className='c7n-networking-tag'>
          {nodePort || formatMessage({ id: 'null' })} {port} {targetPort}
        </div>
      </div>
    ));
    const popoverDom = ports && ports.length > 2 ? (
      <Popover
        arrowPointAtCenter
        placement='bottomRight'
        getPopupContainer={triggerNode => triggerNode.parentNode}
        content={portDom}
      >
        <Icon type='expand_more' className='c7n-networking-expend-icon' />
      </Popover>
    ) : null;
    return (
      <div>
        {portDom[0]}
        <div className='c7n-networking-tag-more'>
          {portDom[1] || null}
          {popoverDom}
        </div>
      </div>
    );
  };

  render() {
    const {
      intl: { formatMessage },
      show,
      name,
      id,
      appId,
      store: {
        getNetworking,
        getNetworkingLoading,
        getNetworkingPageInfo,
      },
    } = this.props;
    const { modalType, expandedRowKeys, isExpanded } = this.state;

    const envData = EnvOverviewStore.getEnvcard;
    const envId = EnvOverviewStore.getTpEnvId;
    const envState = envData.length ? _.filter(envData, ['id', envId])[0] : { connect: false };

    let buttonTips;
    if (envState && !envState.connect) {
      buttonTips = <FormattedMessage id='envoverview.envinfo' />;
    } else if (!getNetworking.length) {
      buttonTips = <FormattedMessage id='ist.networking.ingress.disabled' />;
    }

    const columns = [
      {
        title: <FormattedMessage id='ist.networking.service.name' />,
        key: 'name',
        dataIndex: 'name',
        render: (text, { status, error }) => (
          <StatusIcon
            name={text}
            status={status}
            error={error}
          />
        ),
      },
      {
        title: <FormattedMessage id='ist.networking.service.type' />,
        key: 'type',
        dataIndex: 'type',
      },
      {
        title: <FormattedMessage id='ist.networking.service.ip' />,
        key: 'externalIp',
        render: this.renderExternalIp,
      },
      {
        title: <FormattedMessage id='ist.networking.service.port' />,
        key: 'ports',
        render: this.renderPorts,
      },
    ];

    return (<Fragment>
      <Sidebar
        title={formatMessage({ id: 'ist.networking.header' })}
        visible={show}
        className='c7n-networking-wrap'
        footer={
          [<Button
            key="back"
            funcType="raised"
            type='primary'
            onClick={this.onClose.bind(this, false)}
          >
            {formatMessage({ id: 'close' })}
          </Button>]
        }
      >
        <Content
          code={`ist.networking`}
          values={{ name }}
          className="sidebar-content"
        >
          <Permission
            service={['devops-service.devops-service.create']}
          >
            <Tooltip
              title={
                envState && !envState.connect ? (
                  <FormattedMessage id="envoverview.envinfo" />
                ) : null
              }
            >
              <Button
                icon='playlist_add'
                type='primary'
                className='mg-right-20'
                disabled={envState && !envState.connect}
                onClick={this.showModal.bind(this, 'service')}
              >
                <FormattedMessage id="network.header.create" />
              </Button>
            </Tooltip>
          </Permission>
          <Permission
            service={['devops-service.devops-ingress.create']}
          >
            <Tooltip
              title={buttonTips}
            >
              <Button
                icon="playlist_add"
                type='primary'
                className='mg-right-20'
                disabled={(envState && !envState.connect) || !getNetworking.length}
                onClick={this.showModal.bind(this, 'ingress')}
              >
                <FormattedMessage id="domain.header.create" />
              </Button>
            </Tooltip>
          </Permission>
          <Button
            type='primary'
            onClick={this.expendOrCollapse}
            icon={isExpanded ? 'expand_less' : 'expand_more'}
          >
            <FormattedMessage id={isExpanded ? 'collapseAll' : 'expandAll'} />
          </Button>
          <Button
            type='primary'
            onClick={this.handleRefresh}
            icon='refresh'
          >
            <FormattedMessage id='refresh' />
          </Button>
          <Table
            className='c7n-expand-table mg-top-15'
            dataSource={getNetworking}
            loading={getNetworkingLoading}
            columns={columns}
            filterBar={false}
            pagination={getNetworkingPageInfo}
            onChange={this.tableChange}
            rowKey={record => record.id}
            expandedRowKeys={expandedRowKeys}
            onExpandedRowsChange={this.expandedRowsChange}
            expandedRowRender={record => (
              <div className='c7n-networking-expandedRow'>
                {this.getExpandedRow(record)}
              </div>
            )}
          />
        </Content>
      </Sidebar>
      {modalType === 'service' && (
        <CreateNetwork
          instanceId={id}
          appId={appId}
          istName={name}
          show={modalType === 'service'}
          onClose={this.closeModal}
          envId={envId}
        />
      )}
      {modalType === 'ingress' && (
        <CreateIngress
          istName={name}
          show={modalType === 'ingress'}
          type='create'
          envId={envId}
          onClose={this.closeModal}
        />
      )}
    </Fragment>);
  }
}

Networking.propTypes = {
  id: PropTypes.number,
  appId: PropTypes.number,
  name: PropTypes.string,
  show: PropTypes.bool,
  onClose: PropTypes.func,
  store: PropTypes.object,
};
