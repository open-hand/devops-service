import React, { Component } from 'react';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Content, stores } from '@choerodon/master';
import { Modal } from 'choerodon-ui';

import MdEditor from '../../../../components/MdEditor';
import '../../../main.less';
import InterceptMask from '../../../../components/intercept-mask';

const { AppState } = stores;
const { Sidebar } = Modal;

@observer
class AppTagEdit extends Component {
  constructor(props) {
    super(props);
    const { release } = this.props;
    this.state = {
      submitting: false,
      notes: (release && release.description !== 'empty') ? release.description : '',
    };
  }

  /**
   * 点击创建
   * @param e
   */
  handleOk = (e) => {
    e.preventDefault();
    const { store, tag } = this.props;
    const { id: projectId } = AppState.currentMenuType;
    const { notes } = this.state;
    this.setState({ submitting: true });
    store.editTag(projectId, tag, notes || 'empty').then((req) => {
      this.setState({ submitting: false });
      if (req && req.failed) {
        Choerodon.prompt(req.message);
      } else {
        store.queryTagData(projectId, 0, 10);
        this.handleCancel();
      }
    }).catch((error) => {
      Choerodon.handleResponseError(error);
      this.setState({
        submitting: false,
      });
    });
  };


  /**
   * 取消创建tag
   */
  handleCancel = () => this.props.close(false);

  /**
   * release note 内容变化
   * @param e
   */
  handleNoteChange = (e) => this.setState({ notes: e });

  render() {
    const { intl: { formatMessage }, show, tag } = this.props;
    const { submitting, notes } = this.state;
    return (<Sidebar
      destroyOnClose
      title={<FormattedMessage id="apptag.update" />}
      visible={show}
      onOk={this.handleOk}
      okText={<FormattedMessage id="save" />}
      cancelText={<FormattedMessage id="cancel" />}
      confirmLoading={submitting}
      onCancel={this.handleCancel}
    >
      <Content code="apptag.update" values={{ name: tag }} className="c7n-tag-create sidebar-content">
        <div className="c7n-apptag-release-title">{formatMessage({ id: 'apptag.release.title' })}</div>
        <MdEditor
          value={notes}
          onChange={this.handleNoteChange}
        />
        <InterceptMask visible={submitting} />
      </Content>
    </Sidebar>);
  }
}

export default injectIntl(AppTagEdit);
