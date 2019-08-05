import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import {
  Content,
  Header,
  Page,
  Permission,
} from '@choerodon/boot';
import { Select, Button, Tooltip } from 'choerodon-ui';
import _ from 'lodash';
import classnames from 'classnames';
import CertTable from '../certTable';
import CertificateCreate from '../certificateCreate';
import EnvOverviewStore from '../../../stores/project/envOverview';
import DepPipelineEmpty from '../../../components/DepPipelineEmpty/DepPipelineEmpty';
import RefreshBtn from '../../../components/refreshBtn';
import DevopsStore from '../../../stores/DevopsStore';

import '../../main.scss';

const { Option } = Select;

@withRouter
@injectIntl
@inject('AppState')
@observer
export default class Certificate extends Component {
  state = {
    createDisplay: false,
  };

  componentDidMount() {
    const {
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;

    EnvOverviewStore.loadActiveEnv(projectId, 'certificate');
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
    const {
      CertificateStore,
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;
    const envId = value || EnvOverviewStore.getTpEnvId;

    CertificateStore.loadCertData(spin, projectId, envId);
  };

  /**
   * 环境选择
   * @param value
   */
  handleEnvSelect = value => {
    EnvOverviewStore.setTpEnvId(value);
    this.loadCertData(true, value);
  };

  get getContent() {
    const {
      CertificateStore,
      intl: { formatMessage },
      AppState: {
        currentMenuType: {
          name,
        },
      },
    } = this.props;

    const envData = EnvOverviewStore.getEnvcard;
    const envId = EnvOverviewStore.getTpEnvId;
    const envState = _.filter(envData, { id: envId, connect: true });

    const selectClass = classnames({
      'c7n-header-select': true,
      'c7n-select_min100': !envId,
    });

    const envOptions = _.map(envData, ({ connect, id, permission, name }) => {
      const envOptionClass = classnames({
        'c7ncd-status': true,
        'c7ncd-status-success': connect,
        'c7ncd-status-disconnect': !connect,
      });

      return (<Option
        key={id}
        value={id}
        disabled={!permission}
        title={name}
      >
        <Tooltip placement="right" title={name}>
          <span className="c7n-ib-width_100">
            <span className={envOptionClass} />
            {name}
          </span>
        </Tooltip>
      </Option>);
    });

    return (<Fragment>
      <Header title={<FormattedMessage id="ctf.head" />}>
        <Select
          className={selectClass}
          dropdownClassName="c7n-header-env_drop"
          placeholder={formatMessage({ id: 'envoverview.noEnv' })}
          value={envData && envData.length ? envId : undefined}
          disabled={envData && envData.length === 0}
          onChange={this.handleEnvSelect}
        >
          {envOptions}
        </Select>
        <Permission
          service={['devops-service.certification.create']}
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
      <Content
        className="page-content"
        code="ctf"
        values={{ name }}
      >
        <CertTable
          store={CertificateStore}
          envId={envId}
        />
      </Content>
    </Fragment>);
  }

  render() {
    const {
      CertificateStore,
    } = this.props;
    const { createDisplay } = this.state;

    const envData = EnvOverviewStore.getEnvcard;
    const envId = EnvOverviewStore.getTpEnvId;

    const hasEnv = envData && envData.length && envId;

    if (hasEnv) {
      DevopsStore.initAutoRefresh('cert', this.reload);
    }

    return (
      <Page
        className="c7n-region c7n-ctf-wrapper"
        service={[
          'devops-service.devops-environment.listByProjectIdAndActive',
          'devops-service.certification.listByOptions',
          'devops-service.certification.create',
          'devops-service.certification.delete',
          'devops-service.certification.listOrgCert',
        ]}
      >
        {hasEnv
          ? (this.getContent)
          : (<DepPipelineEmpty
            title={<FormattedMessage id="ctf.head" />}
            type="env"
          />)
        }
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
