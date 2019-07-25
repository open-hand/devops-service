import React, { Component, Fragment } from 'react';
import PropTypes from 'prop-types';
import { withRouter } from 'react-router-dom';
import { observer, inject } from 'mobx-react';
import { FormattedMessage, injectIntl } from 'react-intl';
import { Button, Modal } from 'choerodon-ui';
import _ from 'lodash';
import PipelineRecordStore from '../../../../stores/project/pipelineRecord';

@injectIntl
@withRouter
@inject('AppState')
@observer
export default class PendingCheckModal extends Component {
  state = {
    stopLoading: false,
    passLoading: false,
    canCheck: false,
    checkTips: null,
    show: false,
  };

  componentDidMount() {
    const {
      AppState: {
        currentMenuType: { projectId },
        userInfo: { id: userId },
      },
      intl: { formatMessage },
      id,
      checkData: {
        checkType,
        stageRecordId,
        taskRecordId,
      },
    } = this.props;
    const data = {
      pipelineRecordId: id,
      userId,
      type: checkType,
      stageRecordId,
      taskRecordId,
    };
    PipelineRecordStore.canCheck(projectId, data)
      .then(data => {
        if (data) {
          if (data.failed) {
            Choerodon.prompt(data.message);
          } else if ((data.isCountersigned || data.isCountersigned === 0) && data.userName) {
            // 会签已被终止、或签已被审核，返回数据：{ isCountersigned: 0 或签 | 1 会签, userName: "string"}
            this.setState({
              show: true,
              canCheck: false,
              checkTips: formatMessage({ id: `pipeline.canCheck.tips.${data.isCountersigned}` }, { userName: data.userName }),
            });
          } else {
            // 预检通过，返回数据：{ isCountersigned: null, userName: null }
            this.setState({
              show: true,
              canCheck: true,
            });
          }
        }
      });
  }

  /**
   * 中止或通过人工审核
   * @param flag 是否通过
   */
  handleSubmit = (flag) => {
    const {
      AppState: {
        currentMenuType: { projectId },
        userInfo: { id: userId },
      },
      intl: { formatMessage },
      id,
      checkData: {
        stageRecordId,
        taskRecordId,
        checkType,
      },
    } = this.props;
    const data = {
      pipelineRecordId: id,
      userId,
      isApprove: flag,
      type: checkType,
      stageRecordId,
      taskRecordId,
    };
    this.setState({ [flag ? 'passLoading' : 'stopLoading']: true });
    PipelineRecordStore.checkData(projectId, data)
      .then(data => {
        if (data) {
          if (data.failed) {
            Choerodon.prompt(data.message);
          } else if (data.length) {
            //会签，非最后一人审核，返回数据：[{ audit: true 已审核 | false 未审核, loginName: "工号", realName: "姓名"}]
            const users = {
              check: [],
              unCheck: [],
            };
            _.forEach(data, ({ audit, loginName, realName }) => {
              users[audit ? 'check' : 'unCheck'].push(`${loginName} ${realName}`);
            });
            this.setState({
              canCheck: false,
              checkTips: formatMessage({ id: 'pipeline.check.tips.text' }, {
                checkUsers: users['check'].join('，'),
                unCheckUsers: users['unCheck'].join('，'),
              }),
            });
          } else {
            // 或签、会签最后一人，返回数据[]
            this.handClose(true);
          }
        }
        this.setState({ [flag ? 'passLoading' : 'stopLoading']: false });
      });
  };

  /**
   * 关闭弹窗
   * @param flag 是否重新加载列表数据
   */
  handClose = (flag) => {
    const { onClose } = this.props;
    onClose(flag);
    this.setState({ show: false, canCheck: false, checkTips: null });
  };

  render() {
    const {
      intl: { formatMessage },
      name,
      checkData: {
        checkType,
        stageName,
      },
    } = this.props;
    const {
      stopLoading,
      passLoading,
      canCheck,
      checkTips,
      show,
    } = this.state;
    return (
      <Modal
        visible={show}
        title={formatMessage({ id: 'pipelineRecord.check.manual' })}
        closable={false}
        footer={canCheck ? [
          <Button
            key="back"
            onClick={this.handClose.bind(this, false)}
            disabled={stopLoading || passLoading}
          >
            <FormattedMessage id="pipelineRecord.check.cancel" />
          </Button>,
          <Button
            key="stop"
            loading={stopLoading}
            type="primary"
            onClick={this.handleSubmit.bind(this, false)}
            disabled={passLoading}
          >
            <FormattedMessage id="pipelineRecord.check.stop" />
          </Button>,
          <Button
            key="pass"
            loading={passLoading}
            type="primary"
            onClick={this.handleSubmit.bind(this, true)}
            disabled={stopLoading}
          >
            <FormattedMessage id="pipelineRecord.check.pass" />
          </Button>,
        ] : [
          <Button
            key="back"
            type="primary"
            onClick={this.handClose.bind(this, true)}
          >
            <FormattedMessage id="pipelineRecord.check.tips.button" />
          </Button>,
        ]}
      >
        <div className="c7n-padding-top_8">
          {canCheck ?
            <FormattedMessage
              id={`pipelineRecord.check.${checkType}.des`}
              values={{ name, stage: stageName }}
            /> :
            <span>{checkTips}</span>
          }
        </div>
      </Modal>
    );
  }
}

PendingCheckModal.propTypes = {
  id: PropTypes.number,
  name: PropTypes.string,
  checkData: PropTypes.object,
  onClose: PropTypes.func,
};
