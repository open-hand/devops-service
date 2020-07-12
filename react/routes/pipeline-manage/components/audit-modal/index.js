import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { inject } from 'mobx-react';
import { FormattedMessage, injectIntl } from 'react-intl';
import { Button } from 'choerodon-ui';
import _ from 'lodash';

const AuditModal = ({
  AppState: {
    currentMenuType: { projectId },
    userInfo: { id: userId },
  },
  intl: { formatMessage },
  cdRecordId,
  checkData: {
    type,
    stageRecordId,
    taskRecordId,
    stageName,
  },
  mainStore,
  modal,
  onClose,
  name,
}) => {
  const [stopLoading, changeStopLoading] = useState(false);
  const [passLoading, changePassLoading] = useState(false);
  const [canCheck, setCanCheck] = useState(false);
  const [checkTips, setCheckTips] = useState(null);

  useEffect(() => {
    check();
  }, []);

  function changeModalFooter(flag) {
    if (flag) {
      modal.update({
        footer: [
          <Button
            key="back"
            onClick={handClose.bind(this, false)}
            disabled={stopLoading || passLoading}
          >
            <FormattedMessage id="pipelineRecord.check.cancel" />
          </Button>,
          <Button
            key="stop"
            loading={stopLoading}
            type="primary"
            onClick={handleSubmit.bind(this, false)}
            disabled={passLoading}
          >
            <FormattedMessage id="pipelineRecord.check.stop" />
          </Button>,
          <Button
            key="pass"
            loading={passLoading}
            type="primary"
            onClick={handleSubmit.bind(this, true)}
            disabled={stopLoading}
          >
            <FormattedMessage id="pipelineRecord.check.pass" />
          </Button>,
        ],
      });
    } else {
      modal.update({
        footer: [
          <Button
            key="back"
            type="primary"
            onClick={handClose.bind(this, true)}
          >
            <FormattedMessage id="pipelineRecord.check.tips.button" />
          </Button>,
        ],
      });
    }
  }

  async function check() {
    try {
      const postData = {
        pipelineRecordId: cdRecordId,
        userId,
        type,
        stageRecordId,
        taskRecordId,
      };
      const data = await mainStore.canCheck(projectId, postData);
      if (data && !data.failed) {
        if ((data.isCountersigned || data.isCountersigned === 0) && data.userName) {
          // 会签已被终止、或签已被审核，返回数据：{ isCountersigned: 0 或签 | 1 会签, userName: "string"}
          setCanCheck(false);
          changeModalFooter(false);
          setCheckTips(formatMessage({ id: `pipeline.canCheck.tips.${data.isCountersigned}` }, { userName: data.userName }));
        } else {
          // 预检通过，返回数据：{ isCountersigned: null, userName: null }
          setCanCheck(true);
          changeModalFooter(true);
        }
      }
    } catch (e) {
      modal.close();
      return false;
    }
  }

  /**
   * 中止或通过人工审核
   * @param flag 是否通过
   */
  async function handleSubmit(flag) {
    const postData = {
      pipelineRecordId: cdRecordId,
      userId,
      isApprove: flag,
      type,
      stageRecordId,
      taskRecordId,
    };
    flag ? changePassLoading(true) : changeStopLoading(true);
    try {
      const data = await mainStore.checkData(projectId, postData);
      if (data && !data.failed) {
        if (data.length) {
          // 会签，非最后一人审核，返回数据：[{ audit: true 已审核 | false 未审核, loginName: "工号", realName: "姓名"}]
          const users = {
            check: [],
            unCheck: [],
          };
          _.forEach(data, ({ audit, loginName, realName }) => {
            users[audit ? 'check' : 'unCheck'].push(realName);
          });
          setCanCheck(false);
          changeModalFooter(false);
          setCheckTips(formatMessage({ id: 'pipeline.check.tips.text' }, {
            checkUsers: users.check.join('，'),
            unCheckUsers: users.unCheck.join('，'),
          }));
        } else {
          // 或签、会签最后一人，返回数据[]
          handClose(true);
        }
      }
    } catch (e) {
      return false;
    }
  }

  /**
   * 关闭弹窗
   * @param flag 是否重新加载列表数据
   */
  function handClose(flag) {
    onClose(flag);
    modal.close();
  }

  return (canCheck ? (
    <FormattedMessage
      id={`pipelineRecord.check.${type}.des`}
      values={{ name, stage: stageName }}
    />) : <span>{checkTips}</span>
  );
};

AuditModal.propTypes = {
  cdRecordId: PropTypes.number,
  name: PropTypes.string,
  checkData: PropTypes.object,
  onClose: PropTypes.func,
};

export default injectIntl(inject('AppState')(observer(AuditModal)));
