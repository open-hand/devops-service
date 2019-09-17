import React from 'react';
import { Modal } from 'choerodon-ui/pro';
import { FormattedMessage } from 'react-intl';


class CustomConfirm {
  constructor(config) {
    const { formatMessage } = config;
    this.formatMessage = formatMessage;
  }

  delete = (config = {}) => {
    const { formatMessage } = this;
    const { handleOk, titleId, titleCom, titleVal, contentId, contentCom, contentVal } = config;
    let title = formatMessage({ id: 'delete' });
    if (titleId) {
      title = formatMessage({ id: titleId }, titleVal);
    } else if (titleCom) {
      title = titleCom;
    }
    let child = '确定要删除吗？';
    if (contentId) {
      child = formatMessage({ id: contentId, values: contentVal });
    } else if (contentCom) {
      child = contentCom;
    }
    Modal.open({
      title,
      okText: formatMessage({ id: 'delete' }),
      header: true,
      children: child,
      onOk: handleOk,
    });
  }
}

function isString(obj) {
  if (obj && typeof obj === 'string') return true;
  return false;
}

export default CustomConfirm;
