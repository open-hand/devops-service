import React, { Component, Fragment } from "react";
import { observer } from "mobx-react";
import { withRouter } from "react-router-dom";
import { injectIntl, FormattedMessage } from "react-intl";
import {
  Content,
  Header,
  Page,
  Permission,
  stores,
} from "@choerodon/boot";
import { Select, Button, Tooltip } from "choerodon-ui";
import _ from "lodash";
import "../../../main.scss";
import CertTable from "../certTable";
import CertificateCreate from "../certificateCreate";
import EnvOverviewStore from "../../../../stores/project/envOverview";
import DepPipelineEmpty from "../../../../components/DepPipelineEmpty/DepPipelineEmpty";
import RefreshBtn from "../../../../components/refreshBtn";
import DevopsStore from "../../../../stores/DevopsStore";

const { AppState } = stores;
const { Option } = Select;

@observer
class Certificate extends Component {
  constructor(props) {
    super(props);
    this.state = {
      createDisplay: false,
    };
  }

  componentDidMount() {
    const { id: projectId } = AppState.currentMenuType;
    EnvOverviewStore.loadActiveEnv(projectId, "certificate");
  }

  componentWillUnmount() {
    DevopsStore.clearAutoRefresh();
  }

  /**
   * 创建证书侧边栏
   */
  openCreateModal = () => {
    const { CertificateStore } = this.props;
    CertificateStore.setEnvData([]);
    this.setState({ createDisplay: true });
  };

  /**
   * 关闭创建侧边栏
   */
  closeCreateModal = () => this.setState({ createDisplay: false });

  /**
   * 刷新
   */
  reload = (spin = true) => this.loadCertData(spin);

  loadCertData = (spin, value) => {
    const envId = value || EnvOverviewStore.getTpEnvId;
    const { CertificateStore } = this.props;
    const {
      page,
      pageSize,
      sorter,
      postData,
    } = CertificateStore.getTableFilter;
    const { id: projectId } = AppState.currentMenuType;
    CertificateStore.loadCertData(
      spin,
      projectId,
      page,
      pageSize,
      sorter,
      postData,
      envId
    );
  };

  /**
   * 环境选择
   * @param value
   */
  handleEnvSelect = value => {
    EnvOverviewStore.setTpEnvId(value);
    this.loadCertData(true, value);
  };

  render() {
    const {
      CertificateStore,
      intl: { formatMessage },
    } = this.props;
    const { createDisplay } = this.state;
    const {
      type,
      id: projectId,
      organizationId: orgId,
      name,
    } = AppState.currentMenuType;

    const envData = EnvOverviewStore.getEnvcard;
    const envId = EnvOverviewStore.getTpEnvId;
    const envState = _.filter(envData, {id: envId, connect: true});

    if (envData && envData.length && envId) {
      DevopsStore.initAutoRefresh("cert", this.reload);
    }

    return (
      <Page
        className="c7n-region c7n-ctf-wrapper"
        service={[
          "devops-service.devops-environment.listByProjectIdAndActive",
          "devops-service.certification.listByOptions",
          "devops-service.certification.create",
          "devops-service.certification.delete",
          "devops-service.certification.listOrgCert",
        ]}
      >
        {envData && envData.length && envId ? (
          <Fragment>
            <Header title={<FormattedMessage id="ctf.head" />}>
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
                organizationId={orgId}
                service={["devops-service.certification.create"]}
              >
                <Button
                  funcType="flat"
                  onClick={this.openCreateModal}
                  icon="playlist_add"
                  disabled={!(envState && envState.length)}
                >
                  <FormattedMessage id="ctf.create" />
                </Button>
              </Permission>
              <RefreshBtn name="cert" onFresh={this.reload} />
            </Header>
            <Content className="page-content" code="ctf" values={{ name }}>
              <CertTable store={CertificateStore} envId={envId} />
            </Content>
          </Fragment>
        ) : (
          <DepPipelineEmpty
            title={<FormattedMessage id="ctf.head" />}
            type="env"
          />
        )}
        {createDisplay && (
          <CertificateCreate
            visible={createDisplay}
            store={CertificateStore}
            envId={envId}
            onClose={this.closeCreateModal}
          />
        )}
      </Page>
    );
  }
}

export default withRouter(injectIntl(Certificate));
