import React, { Component, Fragment } from 'react';
import { observer } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import {
  Content,
  Header,
  Page,
  Permission,
  stores,
} from '@choerodon/boot';
import { Button, Popover, Tooltip, Table } from 'choerodon-ui';
import { injectIntl, FormattedMessage } from 'react-intl';
import _ from 'lodash';
import MouserOverWrapper from '../../../../components/MouseOverWrapper';
import AppName from '../../../../components/appName';
import '../../instances/Instances.scss';
import '../../../main.scss';
import DepPipelineEmpty from '../../../../components/DepPipelineEmpty/DepPipelineEmpty';
import DeploymentPipelineStore from '../../../../stores/project/deploymentPipeline';

const { AppState } = stores;

@observer
class DeployOverview extends Component {
  constructor(props, context) {
    super(props, context);
    this.state = {};
  }

  componentDidMount() {
    this.loadEnvCards();
  }

  componentWillUnmount() {
    const { InstancesStore } = this.props;
    InstancesStore.setMutiData([]);
  }

  /**
   * 刷新函数
   */
  reload = () => {
    this.loadEnvCards();
  };

  /**
   * 获取可用环境
   */
  loadEnvCards = () => {
    const projectId = AppState.currentMenuType.id;
    DeploymentPipelineStore.loadActiveEnv(projectId);
    this.loadMulti();
  };

  /**
   * 查询多应用部署数据
   */
  loadMulti = () => {
    const { InstancesStore } = this.props;
    const projectId = AppState.currentMenuType.id;
    InstancesStore.loadMultiData(projectId);
  };

  /**
   * 快速部署
   */
  quickDeploy = ({ applicationId, latestVersion, projectId }) => {
    const {
      history,
      location: {
        search,
      },
    } = this.props;
    const currentProject = parseInt(AppState.currentMenuType.id, 10);

    history.push({
      pathname: '/devops/deployment-app',
      search,
      state: {
        appId: applicationId,
        version: latestVersion,
        prevPage: 'deploy',
        isLocalApp: projectId === currentProject,
      },
    });
  };

  /**
   * 处理页面跳转
   */
  linkToReports = () => {
    const { history } = this.props;
    const {
      id: projectId,
      name,
      organizationId,
    } = AppState.currentMenuType;
    history.push(`/devops/reports/deploy-times?type=${type}&id=${projectId}&name=${name}&organizationId=${organizationId}&deploy-overview`);
  };

  /**
   * 表格渲染
   * @returns {*}
   */
  renderTable() {
    const {
      InstancesStore,
      intl: { formatMessage },
    } = this.props;
    const projectId = parseInt(AppState.currentMenuType.id, 10);
    const appList = InstancesStore.getMutiData;
    const envNames = _.filter(DeploymentPipelineStore.getEnvLine, [
      'permission',
      true,
    ]);
    const { type, organizationId: orgId } = AppState.currentMenuType;

    const columns = [
      {
        title: formatMessage({ id: 'deploy.app' }),
        width: 152,
        key: 'apps',
        fixed: 'left',
        render: record => (
          <AppName
            name={record.applicationName}
            showIcon={!!record.projectId}
            self={record.projectId === projectId}
            width="108px"
          />
        ),
      },
      {
        title: formatMessage({ id: 'ist.lastVer' }),
        width: 227,
        key: 'latestVersion',
        fixed: 'left',
        render: record => (
          <div className="c7n-deploy-last">
            <div className="c7n-deploy-muti_card last_177">
              <MouserOverWrapper text={record.latestVersion} width="161px">
                {record.latestVersion}
              </MouserOverWrapper>
            </div>
            <Permission
              service={['devops-service.application-instance.deploy']}
              type={type}
              projectId={projectId}
              organizationId={orgId}
            >
              <Tooltip title={<FormattedMessage id="dpOverview.deploy" />}>
                <Button
                  shape="circle"
                  icon="jsfiddle"
                  onClick={() => this.quickDeploy(record)}
                />
              </Tooltip>
            </Permission>
          </div>
        ),
      },
    ];

    _.map(envNames, (e, index) => {
      columns.push({
        title: (
          <div>
            {e.connect ? (
              <Tooltip title={<FormattedMessage id="connect" />}>
                <span className="c7ncd-status c7ncd-status-success" />
              </Tooltip>
            ) : (
              <Tooltip title={<FormattedMessage id="disconnect" />}>
                <span className="c7ncd-status c7ncd-status-disconnect" />
              </Tooltip>
            )}
            {e.name}
          </div>
        ),
        width: 230,
        key: `${e.name}${index}`,
        render: record => <span>{this.renderEc(e.id, record)}</span>,
      });
    });
    /**
     * 处理环境列变换时fixed列自适应宽度问题
     */
    columns.push({
      key: 'blank',
    });

    return (
      <Table
        className={`${!appList.length && 'no-value'} c7n-multi-table`}
        pagination={false}
        filterBar={false}
        loading={InstancesStore.getIsLoading}
        columns={columns}
        dataSource={appList}
        rowKey={record => record.applicationId}
        scroll={{ x: 377 + envNames.length * 230 }}
      />
    );
  }

  /**
   * 处理返回环境列渲染
   * @param id
   * @param record
   * @returns {any[]}
   */
  renderEc = (id, record) => {
    const { intl } = this.props;
    let dom = [];
    _.map(record.envInstances, i => {
      if (id === i.envId) {
        dom = i.envVersions;
      }
    });
    return dom.map(version => (
      <div className="c7n-deploy-muti-row" key={version.versionId}>
        <div className="c7n-deploy-muti_card">
          <Popover
            placement="bottom"
            title={<FormattedMessage id="ist.head" />}
            content={
              version.instances.length ? (
                version.instances.map(ist => (
                  <div key={ist.instanceId}>
                    <div
                      className={`c7n-ist-status c7n-ist-status_${
                        ist.instanceStatus
                        }`}
                    >
                      <div>
                        {intl.formatMessage({
                          id: ist.instanceStatus || 'null',
                        })}
                      </div>
                    </div>
                    <span>{ist.instanceName}</span>
                  </div>
                ))
              ) : (
                <FormattedMessage id="ist.noIst" />
              )
            }
            trigger="hover"
          >
            <Button className="c7n-multi-ist" funcType="flat" shape="circle">
              <div>{version.instances.length}</div>
            </Button>
          </Popover>
          <MouserOverWrapper text={version.version} width="161px">
            {version.version}
          </MouserOverWrapper>
          {version.latest ? null : (
            <Tooltip title={<FormattedMessage id="dpOverview.update" />}>
              <span className="c7ncd-status c7ncd-status-update" />
            </Tooltip>
          )}
        </div>
      </div>
    ));
  };

  render() {
    const {
      projectId,
      name,
      organizationId,
      type,
    } = AppState.currentMenuType;
    const envNames = _.filter(DeploymentPipelineStore.getEnvLine, [
      'permission',
      true,
    ]);

    return (
      <Page
        className="c7n-region"
        service={[
          'devops-service.application.listAll',
          'devops-service.devops-environment.listByProjectIdAndActive',
          'devops-service.application-instance.deploy',
        ]}
      >
        {envNames && envNames.length ? (
          <Fragment>
            <Header title={<FormattedMessage id="dpOverview.head" />}>
              <Button
                icon="poll"
                onClick={this.linkToReports}
              >
                <FormattedMessage id="dpOverview.reports" />
              </Button>
              <Button funcType="flat" onClick={this.reload}>
                <i className="icon-refresh icon" />
                <FormattedMessage id="refresh" />
              </Button>
            </Header>
            <Content
              code="dpOverview"
              values={{ name }}
              className="page-content"
            >
              <div className="c7n-multi-wrap">{this.renderTable()}</div>
            </Content>
          </Fragment>
        ) : (
          <DepPipelineEmpty
            title={<FormattedMessage id="dpOverview.head" />}
            type="env"
          />
        )}
      </Page>
    );
  }
}

export default withRouter(injectIntl(DeployOverview));
