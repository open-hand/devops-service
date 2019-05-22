import React, { Component, Fragment } from 'react';
import { Button, Tooltip, Select } from 'choerodon-ui';
import { observer } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { Content, Header, Page, Permission, stores } from '@choerodon/boot';
import _ from 'lodash';
import { injectIntl, FormattedMessage } from 'react-intl';
import KeyValueTable from '../../configMap/keyValueTable';
import KeyValueSideBar from '../../configMap/keyValueSideBar';
import '../../configMap/configMapHome/ConfigMap.scss';
import '../../../main.scss';
import EnvOverviewStore from "../../../../stores/project/envOverview";
import DevopsStore from "../../../../stores/DevopsStore";
import DepPipelineEmpty from "../../../../components/DepPipelineEmpty/DepPipelineEmpty";
import RefreshBtn from "../../../../components/refreshBtn";

const { AppState } = stores;
const { Option } = Select;

@observer
class Secret extends Component {
  constructor(props) {
    super(props);
    this.state = {
      sideBarDisplay: false,
      secretId: undefined,
    };
  }

  componentDidMount() {
    const { id: projectId } = AppState.currentMenuType;
    EnvOverviewStore.loadActiveEnv(projectId, 'secret');
  }

  /**
   * 开启侧边框
   * @param secretId
   */
  openSideBar = (secretId) => {
    this.setState({ sideBarDisplay: true, secretId });
  };

  /**
   * 刷新函数
   * @param spin
   */
  reload = (spin = true) => {
    const { id: projectId } = AppState.currentMenuType;
    EnvOverviewStore.loadActiveEnv(projectId);
    this.loadSecret(spin);
  };

  /**
   * 加载密文
   * @param spin
   * @param page
   * @param size
   */
  loadSecret = (spin, page, size) => {
    const { SecretStore } = this.props;
    const tpEnvId = EnvOverviewStore.getTpEnvId;
    const { id: projectId } = AppState.currentMenuType;
    SecretStore.loadSecret(spin, projectId, tpEnvId, page, size);
  };

  /**
   * 环境选择
   * @param value
   */
  handleEnvSelect = (value) => {
    EnvOverviewStore.setTpEnvId(value);
    this.loadSecret(true);
  };

  /**
   * 关闭侧边栏
   * @param isLoad
   */
  closeSideBar = (isLoad) => {
    this.setState({ sideBarDisplay: false });
    if(isLoad) {
      this.loadSecret(true);
    }
  };

  render() {
    const { type, organizationId, name, id: projectId } = AppState.currentMenuType;
    const { sideBarDisplay, secretId } = this.state;
    const {
      SecretStore,
      intl: { formatMessage },
    } = this.props;
    const envData = EnvOverviewStore.getEnvcard;
    const envId = EnvOverviewStore.getTpEnvId;
    const title = _.find(envData, ['id', envId]);
    const envState = envData.length
      ? envData.filter(d => d.id === Number(envId))[0]
      : { connect: false };

    if (envData && envData.length && envId) {
      DevopsStore.initAutoRefresh('secret', this.reload);
    }

    return (
      <Page
        className="c7n-region c7n-app-wrapper"
        service={[
          'devops-service.devops-secret.createOrUpdate',
          'devops-service.devops-secret.querySecret',
          'devops-service.devops-secret.deleteSecret',
          'devops-service.devops-secret.checkName',
          'devops-service.devops-secret.listByOption',
        ]}
      >
        {envData && envData.length && envId ? (
          <Fragment>
            <Header title={<FormattedMessage id="secret.head" />}>
              <Select
                className={`${
                  envId
                    ? "c7n-header-select"
                    : "c7n-header-select c7n-select_min100"
                  }`}
                dropdownClassName="c7n-header-env_drop"
                placeholder={formatMessage({ id: "envoverview.noEnv" })}
                value={envData && envData.length ? envId : undefined}
                disabled={envData && envData.length === 0}
                onChange={this.handleEnvSelect}
              >
                {_.map(envData, e => (
                  <Option
                    key={e.id}
                    value={e.id}
                    disabled={!e.permission}
                    title={e.name}
                  >
                    <Tooltip placement="right" title={e.name}>
                      <span className="c7n-ib-width_100">
                        {e.connect ? (
                          <span className="c7ncd-status c7ncd-status-success" />
                        ) : (
                          <span className="c7ncd-status c7ncd-status-disconnect" />
                        )}
                        {e.name}
                      </span>
                    </Tooltip>
                  </Option>
                ))}
              </Select>
              <Permission
                type={type}
                projectId={projectId}
                organizationId={organizationId}
                service={['devops-service.devops-secret.createOrUpdate']}
              >
                <Tooltip
                  title={
                    envState && !envState.connect ? (
                      <FormattedMessage id="envoverview.envinfo" />
                    ) : null
                  }
                >
                  <Button
                    funcType="flat"
                    disabled={envState && !envState.connect}
                    onClick={this.openSideBar.bind(this, false)}
                    icon="playlist_add"
                  >
                    <FormattedMessage id="secret.create" />
                  </Button>
                </Tooltip>
              </Permission>
              <RefreshBtn name="secret" onFresh={this.reload} />
            </Header>
            <Content code={'secret'} values={{ name: title ? title.name : name }}>
              <KeyValueTable
                title="secret"
                store={SecretStore}
                envId={envId}
                editOpen={this.openSideBar}
              />
            </Content>
          </Fragment>
        ) : (
          <DepPipelineEmpty
            title={<FormattedMessage id="secret.head" />}
            type="env"
          />
        )}
        {sideBarDisplay && (
          <KeyValueSideBar
            title="secret"
            visible={sideBarDisplay}
            store={SecretStore}
            envId={envId}
            id={secretId}
            onClose={this.closeSideBar}
          />
        )}
      </Page>
    );
  }
}

export default withRouter(injectIntl(Secret));
