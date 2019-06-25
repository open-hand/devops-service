import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Modal } from 'choerodon-ui';
import PropTypes from 'prop-types';
import DeploymentConfigStore from '../../../../../stores/project/deploymentConfig';

import '../../../../main.scss';

@injectIntl
@withRouter
@inject('AppState')
@observer
export default class CoverConfigModal extends Component {
  state = {
    submitting: false,
  };

  /**
   * 关闭弹窗
   * @param 是否覆盖
   */
  onClose = (flag) => {
    const { onClose } = this.props;
    onClose(flag);
  };

  /**
   * 覆盖至部署配置
   */
  handleSubmit = () => {
    const {
      id,
      configValue,
      objectVersionNumber,
      AppState: { currentMenuType: { projectId } },
    } = this.props;
    this.setState({ submitting: true });
    DeploymentConfigStore.createData(projectId, {
      id,
      objectVersionNumber,
      value: configValue,
    })
      .then(res => {
        if (res && res.failed) {
          Choerodon.prompt(res.message);
        } else {
          this.onClose(true);
        }
        this.setState({ submitting: false });
      })
      .catch(err => {
        this.setState({ submitting: false });
        Choerodon.handleResponseError(err);
      });
  };

  render() {
    const {
      intl: { formatMessage },
      show,
    } = this.props;
    const { submitting } = this.state;

    return (
      <Modal
        confirmLoading={submitting}
        visible={show}
        title={formatMessage({ id: 'deploymentConfig.cover.title' })}
        closable={false}
        onOk={this.handleSubmit}
        onCancel={this.onClose.bind(this, false)}
        okText={formatMessage({ id: 'cover' })}
        cancelText={formatMessage({ id: 'notCovered' })}
      >
        <div className="c7n-padding-top_8">
          <FormattedMessage id={'deploymentConfig.cover.des'} />
        </div>
      </Modal>
    );
  }
}

CoverConfigModal.propTypes = {
  id: PropTypes.number,
  objectVersionNumber: PropTypes.number,
  show: PropTypes.bool,
  configValue: PropTypes.string,
  onClose: PropTypes.func,
};
