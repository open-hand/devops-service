import React, { Component, Fragment } from 'react/index';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Content } from '@choerodon/boot';
import { Modal, Button } from 'choerodon-ui';
import YamlEditor from '../../../../components/yamlEditor';

import '../../../main.scss';
import './ResourceDetail.scss';

const { Sidebar } = Modal;

@injectIntl
@inject('AppState')
@observer
export default class ResourceDetail extends Component {
  componentDidMount() {
    const {
      store,
      AppState: {
        currentMenuType: { projectId },
      },
      id,
    } = this.props;
    store.loadSingleData(projectId, id);
  }

  componentWillUnmount() {
    const { store } = this.props;
    store.setSingleData({});
  }

  /**
   * 关闭弹框
   */
  handleClose = () => {
    const { onClose } = this.props;
    onClose(false);
  };

  render() {
    const {
      visible,
      intl: { formatMessage },
      store,
    } = this.props;
    const {
      getSingleData: {
        description,
        name,
        k8sKind,
      },
    } = store;

    return (
      <div className="c7n-region">
        <Sidebar
          destroyOnClose
          title={<FormattedMessage id="resource.view.header" />}
          visible={visible}
          className="c7ncd-resource-detail-sidebar"
          footer={
            [<Button
              key="back"
              type="primary"
              funcType="raised"
              onClick={this.handleClose}
            >
              {formatMessage({ id: 'close' })}
            </Button>]
          }
        >
          <Content
            code="resource.view"
            values={{ name }}
            className="sidebar-content"
          >
            <div>
              <FormattedMessage id="name" />:
              <span className="c7n-resource-detail-text">{name}</span>
            </div>
            <div className="resource-detail-info">
              <FormattedMessage id="type" />:
              <span className="c7n-resource-detail-text">{k8sKind}</span>
            </div>
            <YamlEditor
              readOnly
              value={description}
            />
          </Content>
        </Sidebar>
      </div>
    );
  }
}
