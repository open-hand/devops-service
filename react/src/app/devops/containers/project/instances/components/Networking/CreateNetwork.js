import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl } from 'react-intl';
import { Modal } from 'choerodon-ui';
import PropTypes from 'prop-types';
import _ from 'lodash';
import EnvOverviewStore from '../../../../../stores/project/envOverview';
import NetworkConfigStore from '../../../../../stores/project/networkConfig';
import NetworkForm from '../../../networkConfig/components/networkForm/NetworkForm';

import '../../../../main.scss';
import './Networking.scss';

@injectIntl
@withRouter
@inject('AppState')
@observer
export default class CreateNetwork extends Component {
  state = {
    submitting: false,
  };

  /**
   * 关闭侧边栏
   */
  onClose = (flag) => {
    const { onClose } = this.props;
    onClose(flag);
  };

  handleSubmit = e => {
    e.preventDefault();

    const {
      AppState: { currentMenuType: { projectId } },
      appId,
      istName,
      envId,
    } = this.props;
    this.setState({ submitting: true });
    this.formRef.props.form.validateFields((err, data) => {
      if (!err) {
        const {
          externalIps,
          portKeys,
          port,
          tport,
          nport,
          config,
          protocol,
          name,
        } = data;
        const ports = [];
        if (portKeys) {
          _.forEach(portKeys, item => {
            if (item || item === 0) {
              const node = {
                port: Number(port[item]),
                targetPort: Number(tport[item]),
                nodePort: nport ? Number(nport[item]) : null,
              };
              config === 'NodePort' && (node.protocol = protocol[item]);
              ports.push(node);
            }
          });
        }

        const network = {
          name,
          appId,
          appInstance: [istName],
          envId,
          externalIp: externalIps && externalIps.length ? externalIps.join(",") : null,
          ports,
          type: config,
        };

        NetworkConfigStore
          .createNetwork(projectId, network)
          .then(res => {
            this.setState({submitting: false});
            if (res) {
              this.onClose(true);
            }
          })
          .catch(error => {
            this.setState({submitting: false});
            Choerodon.handleResponseError(error);
          });
      } else {
        this.setState({submitting: false});
      }
    });
  };

  render() {
    const {
      intl: { formatMessage },
      show,
      istName,
      envId,
    } = this.props;
    const { submitting } = this.state;

    return (
      <Modal
        title={formatMessage({ id: 'ist.networking.create.service'}, { istName })}
        className='c7n-ist-networking-modal'
        visible={show}
        onOk={this.handleSubmit}
        okText={formatMessage({ id: 'create' })}
        onCancel={this.onClose.bind(this, false)}
        confirmLoading={submitting}
        maskClosable={false}
      >
        <div className="c7n-padding-top_8">
          <NetworkForm
            wrappedComponentRef={(form) => this.formRef = form}
            envId={envId}
            store={NetworkConfigStore}
            isInstancePage
          />
        </div>
      </Modal>
    )
  }
}

CreateNetwork.propTypes = {
  instanceId: PropTypes.number.isRequired,
  appId: PropTypes.number.isRequired,
  envId: PropTypes.number,
  istName: PropTypes.string,
  show: PropTypes.bool,
  onClose: PropTypes.func,
};

CreateNetwork.defaultProps = {
  envId: EnvOverviewStore.getTpEnvId,
};
