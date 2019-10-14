import React, { Component } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { Form, Modal } from 'choerodon-ui';
import { injectIntl } from 'react-intl';
import _ from 'lodash';
import { Choerodon } from '@choerodon/boot';
import DomainForm from '../../../../../../../components/domain-form';
import InterceptMask from '../../../../../../../components/intercept-mask';

import '../../../../../../main.less';
import './index.less';

const { Sidebar } = Modal;

@Form.create({})
@injectIntl
@withRouter
@inject('AppState')
@observer
class CreateDomain extends Component {
  constructor(props) {
    super(props);
    this.state = {};
  }

  componentDidMount() {
    const {
      AppState: { currentMenuType: { projectId } },
      store,
      envId,
      appServiceId,
    } = this.props;
    store.loadNetwork(projectId, envId, appServiceId);
  }

  handleSubmit = (e) => {
    e.preventDefault();

    const {
      AppState: { currentMenuType: { projectId } },
      store,
      id,
      type,
      envId,
      appServiceId,
      form,
    } = this.props;
    this.setState({ submitting: true });
    form.validateFieldsAndScroll((err, data) => {
      if (!err) {
        const { domain, name, certId, paths, path, network, port } = data;
        const postData = { domain, name, envId, appServiceId };
        if (certId) {
          postData.certId = certId;
        }
        let promise = null;
        const pathList = [];
        const networkList = store.getNetwork;
        _.forEach(paths, (item) => {
          const pt = path[item];
          const serviceId = network[item];
          const servicePort = port[item];
          const serviceName = _.filter(networkList, ['id', serviceId])[0].name;
          pathList.push({
            path: pt,
            serviceId,
            servicePort,
            serviceName,
          });
        });
        postData.pathList = pathList;
        if (type === 'create') {
          promise = store.addData(projectId, postData);
        } else {
          postData.domainId = id;
          promise = store.updateData(projectId, id, postData);
        }
        this.handleResponse(promise);
      } else {
        this.setState({ submitting: false });
      }
    });
  };

  /**
   * 处理创建修改域名请求返回的数据
   * @param promise
   */
  handleResponse = (promise) => {
    if (promise) {
      promise
        .then((data) => {
          this.setState({ submitting: false });
          if (data) {
            this.handleClose(true);
          }
        })
        .catch((err) => {
          this.setState({ submitting: false });
          Choerodon.handleResponseError(err);
        });
    }
  };

  /**
   * 关闭弹框
   */
  handleClose = (isload = false) => {
    const { store, onClose, form } = this.props;
    store.setNetwork([]);
    store.setSingleData(null);
    store.setCertificates([]);
    form.resetFields();
    onClose(isload);
  };

  render() {
    const {
      store,
      intl: { formatMessage },
      type,
      visible,
      envId,
      id,
      form,
    } = this.props;
    const { submitting } = this.state;

    return (
      <div className="c7n-region">
        <Sidebar
          destroyOnClose
          visible={visible}
          okText={formatMessage({ id: type === 'create' ? 'create' : 'save' })}
          cancelText={formatMessage({ id: 'cancel' })}
          title={formatMessage({ id: `domain.${type === 'create' ? 'create' : 'update'}.head` })}
          onCancel={() => this.handleClose()}
          onOk={this.handleSubmit}
          confirmLoading={submitting}
          maskClosable={false}
          width={740}
        >
          <DomainForm
            form={form}
            type={type}
            envId={envId}
            ingressId={id}
            DomainStore={store}
          />
          <InterceptMask visible={submitting} />
        </Sidebar>
      </div>
    );
  }
}

export default CreateDomain;
