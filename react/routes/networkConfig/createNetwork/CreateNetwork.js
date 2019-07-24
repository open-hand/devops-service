/* eslint-disable no-useless-return */
import React, { Component, Fragment } from "react/index";
import { observer, inject } from "mobx-react";
import { withRouter } from "react-router-dom";
import { injectIntl, FormattedMessage } from "react-intl";
import { Modal } from "choerodon-ui";
import { Content } from "@choerodon/boot";
import _ from "lodash";
import "../../main.scss";
import "./CreateNetwork.scss";
import EnvOverviewStore from "../../../../stores/project/envOverview";
import InterceptMask from "../../../../components/interceptMask/InterceptMask";
import NetworkForm from "../components/networkForm/NetworkForm";

const { Sidebar } = Modal;

@injectIntl
@withRouter
@inject('AppState')
@observer
class CreateNetwork extends Component {
  state = {
    submitting: false,
  };

  componentDidMount() {
    const {
      AppState: { currentMenuType: { projectId } },
    } = this.props;
    EnvOverviewStore.loadActiveEnv(projectId);
  }

  handleSubmit = e => {
    e.preventDefault();

    const {
      AppState: { currentMenuType: { projectId } },
      store,
    } = this.props;
    this.setState({ submitting: true });

    this.formRef.props.form.validateFieldsAndScroll((err, data) => {
      if (!err) {
        const {
          name,
          appId,
          appInstance,
          envId,
          endPoints: endps,
          targetIps,
          targetport,
          externalIps,
          portKeys,
          port,
          tport,
          nport,
          protocol,
          targetKeys,
          keywords,
          config,
          values,
        } = data;
        const appIst = appInstance ? _.map(appInstance, item => item) : null;
        const ports = [];
        const label = {};
        const endPoints = {};

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

        if (targetKeys) {
          _.forEach(targetKeys, item => {
            if (item || item === 0) {
              const key = keywords[item];
              label[key] = values[item];
            }
          });
        }

        // targetIps是必填项，当输入值不为空，但是记录值为空时，点击提交会判断为空
        // 此时会改变为表单校验未通过，但是又会自动把输入值填入记录值，使该项正常
        // 造成需要点击两次才能提交。属于不当操作造成，暂未做处理
        if (endps && endps.length && targetIps) {
          endPoints[targetIps.join(",")] = _.map(
            _.filter(endps, item => item || item === 0),
            item => ({
              name: null,
              port: Number(targetport[item]),
            })
          );
        }

        const network = {
          name,
          appId: appId || null,
          appInstance: appIst,
          envId,
          externalIp: externalIps && externalIps.length ? externalIps.join(",") : null,
          ports,
          label: !_.isEmpty(label) ? label : null,
          type: config,
          endPoints: !_.isEmpty(endPoints) ? endPoints : null,
        };

        store
          .createNetwork(projectId, network)
          .then(res => {
            this.setState({ submitting: false });
            if (res) {
              EnvOverviewStore.setTpEnvId(envId);
              this.handleClose();
            }
          })
          .catch(error => {
            this.setState({ submitting: false });
            Choerodon.handleResponseError(error);
          });
      } else {
        this.setState({ submitting: false });
      }
    });
  };

  handleClose = (isload = true) => {
    const { onClose } = this.props;
    onClose(isload);
  };

  render() {
    const {
      AppState: { currentMenuType: { name: menuName } },
      visible,
      envId,
      store,
    } = this.props;
    const {
      submitting,
    } = this.state;

    return (
      <div className="c7n-region">
        <Sidebar
          destroyOnClose
          cancelText={<FormattedMessage id="cancel" />}
          okText={<FormattedMessage id="create" />}
          title={<FormattedMessage id="network.header.create" />}
          visible={visible}
          onOk={this.handleSubmit}
          onCancel={this.handleClose.bind(this, false)}
          confirmLoading={submitting}
        >
          <Content
            code="network.create"
            values={{ name: menuName }}
            className="c7n-network-create sidebar-content"
          >
            <NetworkForm
              wrappedComponentRef={(form) => this.formRef = form}
              envId={envId}
              store={store}
            />
            <InterceptMask visible={submitting} />
          </Content>
        </Sidebar>
      </div>
    );
  }
}

export default CreateNetwork;
