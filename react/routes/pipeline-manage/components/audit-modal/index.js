import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { inject } from 'mobx-react';
import { FormattedMessage, injectIntl } from 'react-intl';
import { Button } from 'choerodon-ui';
import isEmpty from 'lodash/isEmpty';

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
            onClick={handleSubmit.bind(this, 'refused')}
            disabled={passLoading}
          >
            <FormattedMessage id="pipelineRecord.check.stop" />
          </Button>,
          <Button
            key="pass"
            loading={passLoading}
            type="primary"
            onClick={handleSubmit.bind(this, 'passed')}
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
        sourceId: type === 'stage' ? stageRecordId : taskRecordId,
        sourceType: type,
      };
      const data = await mainStore.canCheck(projectId, cdRecordId, postData);
      if (data && !data.failed) {
        if (data.auditStatusChanged) {
          // 会签已被终止、或签已被审核
          setCanCheck(false);
          changeModalFooter(false);
          setCheckTips(formatMessage({ id: `c7ncd.pipelineManage.canCheck.tips.${data.currentStatus}` }, { userName: data.auditUserName }));
        } else {
          // 预检通过
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
  async function handleSubmit(result) {
    const postData = {
      projectId,
      cdRecordId,
      stageRecordId,
      result,
    };
    result === 'passed' ? changePassLoading(true) : changeStopLoading(true);
    try {
      let data;
      if (type === 'stage') {
        data = await mainStore.auditStage(postData);
      } else {
        postData.jobRecordId = taskRecordId;
        data = await mainStore.auditJob(postData);
      }
      if (data && !data.failed) {
        // 会签，非最后一人审核，返回数据：[{ audit: true 已审核 | false 未审核, loginName: "工号", realName: "姓名"}]
        const { auditedUserNameList, notAuditUserNameList, countersigned } = data || {};
        if (countersigned === 1 && !isEmpty(auditedUserNameList) && !isEmpty(notAuditUserNameList)) {
          setCanCheck(false);
          changeModalFooter(false);
          setCheckTips(formatMessage({ id: 'pipeline.check.tips.text' }, {
            checkUsers: auditedUserNameList.join('，'),
            unCheckUsers: notAuditUserNameList.join('，'),
          }));
        } else {
          // 或签、会签最后一人审核通过、审核终止，返回数据null
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
    flag && onClose();
    modal.close();
  }

  return (canCheck ? (
    <FormattedMessage
      id={`c7ncd.pipelineManage.record.check.${type}.des`}
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
