import React, { Component } from 'react/index';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { Modal } from 'choerodon-ui';
import { injectIntl } from 'react-intl';
import { Content } from '@choerodon/boot';
import _ from 'lodash';
import '../../main.scss';
import './index.scss';
import EnvOverviewStore from '../../envOverview/stores';
import InterceptMask from '../../../components/interceptMask/InterceptMask';
import DomainForm from '../components/domainForm/DomainForm';

const { Sidebar } = Modal;

@injectIntl
@withRouter
@inject('AppState')
@observer
class CreateDomain extends Component {
  constructor(props) {
    super(props);
    this.state = {
    };
  }

  componentDidMount() {
    const {
      AppState: { currentMenuType: { projectId } },
      store,
      envId,
    } = this.props;
    store.loadNetwork(projectId, envId);
    EnvOverviewStore.loadActiveEnv(projectId);
  }

  handleSubmit = e => {
    e.preventDefault();

    const {
      AppState: { currentMenuType: { projectId } },
      store,
      id,
      type,
    } = this.props;
    this.setState({ submitting: true });
    this.formRef.props.form.validateFieldsAndScroll((err, data) => {
      if (!err) {
        const {
          domain,
          name,
          envId,
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
        const networkList = store.getNetwork;
        _.forEach(paths, item => {
          const pt = path[item];
          const serviceId = network[item];
          const servicePort = port[item];
          const serviceName = _.filter(networkList, ['id', serviceId])[0].name;
          pathList.push({ path: pt, serviceId, servicePort, serviceName });
        });
        postData.pathList = pathList;
        if (type === 'create') {
          promise = store.addData(projectId, postData);
        } else {
          postData.domainId = id;
          promise = store.updateData(projectId, id, postData);
        }
        this.handleResponse(promise, envId);
      } else {
        this.setState({ submitting: false });
      }
    });
  };

  /**
   * 处理创建修改域名请求返回的数据
   * @param promise
   */
  handleResponse = (promise, envId) => {
    if (promise) {
      promise
        .then(data => {
          this.setState({ submitting: false });
          if (data) {
            EnvOverviewStore.setTpEnvId(envId);
            this.handleClose();
          }
        })
        .catch(err => {
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
    store.setEnv([]);
    store.setNetwork([]);
    store.setSingleData(null);
    store.setCertificates([]);
    onClose(isload);
  };

  render() {
    const {
      AppState: { currentMenuType: { name: menuName } },
      store: { getSingleData },
      intl: { formatMessage },
      type,
      visible,
      envId,
      id,
    } = this.props;
    const env = EnvOverviewStore.getEnvcard;
    const {
      submitting,
    } = this.state;
    const { name } = getSingleData || {};

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
        >
          <Content
            code={`domain.${type === 'create' ? 'create' : 'update'}`}
            values={{ name: type === 'create' ? menuName : name }}
            className="sidebar-content c7n-domainCreate-wrapper"
          >
            <DomainForm
              wrappedComponentRef={(form) => this.formRef = form}
              type={type}
              envId={env && env.length ? envId : null}
              ingressId={id}
            />
            <InterceptMask visible={submitting} />
          </Content>
        </Sidebar>
      </div>
    );
  }
}

export default CreateDomain;
