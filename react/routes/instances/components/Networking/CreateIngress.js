import React, { Component, Fragment } from 'react/index';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl } from 'react-intl';
import { Modal } from 'choerodon-ui';
import PropTypes from 'prop-types';
import _ from 'lodash';
import DomainStore from '../../../../stores/project/domain';
import InstancesStore from '../../../../stores/project/instances/InstancesStore';
import DomainForm from '../../../domain/components/domainForm/DomainForm';

import '../../../main.scss';
import './Networking.scss';

@injectIntl
@withRouter
@inject('AppState')
@observer
export default class CreateIngress extends Component {
  state = {
    submitting: false,
  };

  componentDidMount() {
  }

  /**
   * 关闭侧边栏
   */
  onClose = (flag) => {
    const { onClose } = this.props;
    DomainStore.setNetwork([]);
    DomainStore.setCertificates([]);
    DomainStore.setSingleData(null);
    onClose(flag);
  };

  handleSubmit = e => {
    e.preventDefault();

    const {
      AppState: { currentMenuType: { projectId } },
      id,
      type,
      envId,
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
        const postData = { domain, name, envId };
        if (certId) {
          postData.certId = certId;
        }
        let promise = null;
        const pathList = [];
        const networkList = InstancesStore.getNetworking;
        _.forEach(paths, item => {
          const pt = path[item];
          const serviceId = network[item];
          const servicePort = port[item];
          const serviceName = _.filter(networkList, ['id', serviceId])[0].name;
          pathList.push({ path: pt, serviceId, servicePort, serviceName });
        });
        postData.pathList = pathList;
        if (type === 'create') {
          promise = DomainStore.addData(projectId, postData);
        } else {
          postData.domainId = id;
          promise = DomainStore.updateData(projectId, id, postData);
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
  handleResponse = promise => {
    if (promise) {
      promise
        .then(data => {
          this.setState({ submitting: false });
          if (data) {
            this.onClose(true);
          }
        })
        .catch(err => {
          this.setState({ submitting: false });
          Choerodon.handleResponseError(err);
        });
    }
  };

  render() {
    const {
      intl: { formatMessage },
      show,
      istName,
      type,
      envId,
    } = this.props;
    const { submitting } = this.state;
    const { getSingleData } = DomainStore;
    const { name } = getSingleData || {};

    return (
      <Modal
        title={formatMessage({ id: `ist.networking.${type}.ingress`}, { istName, name })}
        className='c7n-ist-networking-modal'
        visible={show}
        onOk={this.handleSubmit}
        okText={formatMessage({ id: 'create' })}
        onCancel={this.onClose.bind(this, false)}
        confirmLoading={submitting}
        maskClosable={false}
      >
        <div className="c7n-padding-top_8">
          <DomainForm
            wrappedComponentRef={(form) => this.formRef = form}
            type={type}
            envId={envId}
            isInstancePage
          />
        </div>
      </Modal>
    )
  }
}

CreateIngress.propTypes = {
  id: PropTypes.number,
  istName: PropTypes.string,
  name: PropTypes.string,
  type: PropTypes.string,
  show: PropTypes.bool,
  onClose: PropTypes.func,
};

CreateIngress.defaultProps = {
  type: 'create',
};
