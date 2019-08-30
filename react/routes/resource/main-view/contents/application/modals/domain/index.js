import React, { Component } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { Modal } from 'choerodon-ui';
import { injectIntl } from 'react-intl';
import { Content } from '@choerodon/master';
import _ from 'lodash';
import DomainForm from '../../../../../../../components/domain-form';
import InterceptMask from '../../../../../../../components/intercept-mask';

import '../../../../../../main.less';
import './index.less';

const { Sidebar } = Modal;

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
    } = this.props;
    this.setState({ submitting: true });
    this.formRef.props.form.validateFieldsAndScroll((err, data) => {
      if (!err) {
        const {
          domain,
          name,
          certId,
          paths,
          path,
          network,
          port,
        } = data;
        const postData = {
          domain,
          name,
          envId,
          appServiceId,
        };
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
            this.handleClose();
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
  handleClose = (isload = true) => {
    const { store, onClose } = this.props;
    store.setNetwork([]);
    store.setSingleData(null);
    store.setCertificates([]);
    this.formRef.props.form.resetFields();
    onClose(isload);
  };

  render() {
    const {
      AppState: { currentMenuType: { name: menuName } },
      store,
      intl: { formatMessage },
      type,
      visible,
      envId,
      id,
    } = this.props;
    const {
      submitting,
    } = this.state;

    const { name } = store.getSingleData || {};

    return (
      <div className="c7n-region">
        <Sidebar
          destroyOnClose
          okText={
            type === 'create'
              ? formatMessage({ id: 'create' })
              : formatMessage({ id: 'save' })
          }
          cancelText={formatMessage({ id: 'cancel' })}
          visible={visible}
          title={formatMessage({
            id: `domain.${type === 'create' ? 'create' : 'update'}.head`,
          })}
          onCancel={this.handleClose.bind(this, false)}
          onOk={this.handleSubmit}
          confirmLoading={submitting}
          maskClosable={false}
          width={380}
        >
          <DomainForm
            wrappedComponentRef={(form) => {
              this.formRef = form;
            }}
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
