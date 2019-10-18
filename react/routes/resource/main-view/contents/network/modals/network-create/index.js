/* eslint-disable no-useless-return, no-return-assign */
import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Modal } from 'choerodon-ui';
import _ from 'lodash';
import { Choerodon } from '@choerodon/boot';
import InterceptMask from '../../../../../../../components/intercept-mask';
import NetworkForm from './networkForm';

import '../../../../../../main.less';
import './index.less';
import { handlePromptError } from '../../../../../../../utils';

const { Sidebar } = Modal;

@injectIntl
@withRouter
@inject('AppState')
@observer
class CreateNetwork extends Component {
  componentDidMount() {
    const { modal } = this.props;
    modal.handleOk(this.handleSubmit);
  }

  formValidate = () => {
    const { envId } = this.props;
    return new Promise((resolve) => {
      this.formRef.props.form.validateFieldsAndScroll((err, data) => {
        if (!err) {
          const {
            name,
            appId,
            appInstance,
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
          const appIst = appInstance ? _.map(appInstance, (item) => item) : null;
          const ports = [];
          const label = {};
          const endPoints = {};

          if (portKeys) {
            _.forEach(portKeys, (item) => {
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
            _.forEach(targetKeys, (item) => {
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
            endPoints[targetIps.join(',')] = _.map(
              _.filter(endps, (item) => item || item === 0),
              (item) => ({
                name: null,
                port: Number(targetport[item]),
              })
            );
          }

          const network = {
            name,
            appServiceId: appId || null,
            instances: appIst,
            envId,
            externalIp: externalIps && externalIps.length ? externalIps.join(',') : null,
            ports,
            label: !_.isEmpty(label) ? label : null,
            type: config,
            endPoints: !_.isEmpty(endPoints) ? endPoints : null,
          };
          resolve(network);
        }
        resolve(false);
      });
    });
  };

  handleSubmit = async () => {
    const {
      AppState: { currentMenuType: { projectId } },
      store,
      refresh,
    } = this.props;
    const postData = await this.formValidate();
    if (!postData) {
      return false;
    }
    try {
      const res = await store.createNetwork(projectId, postData);
      if (handlePromptError(res)) {
        refresh();
      } else {
        return false;
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
      return false;
    }
  };

  render() {
    const {
      envId,
      store,
    } = this.props;

    return (
      <div className="c7n-region c7ncd-deployment-network-form">
        <NetworkForm
          wrappedComponentRef={(form) => this.formRef = form}
          envId={envId}
          store={store}
        />
      </div>
    );
  }
}

export default CreateNetwork;
