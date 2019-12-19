import React, { Fragment } from 'react';
import { Icon, Button } from 'choerodon-ui/pro';
import { Tooltip } from 'choerodon-ui';
import map from 'lodash/map';
import { useClusterMainStore } from '../../../../stores';
import { useClusterContentStore } from '../../stores';

import './index.less';

export default ({ name, errorMessage, describe, buttonData = [], status, className, progress }) => {
  const {
    prefixCls,
  } = useClusterMainStore();
  const {
    formatMessage,
  } = useClusterContentStore();

  function getStatus() {
    let color = 'rgba(0, 0, 0, 0.36)';
    let message = '';
    switch (status) {
      case 'uninstalled':
        color = 'rgba(0, 0, 0, 0.36)';
        message = formatMessage({ id: 'not_installed' });
        break;
      case 'available':
        color = '#00bfa5';
        message = formatMessage({ id: 'available' });
        break;
      case 'processing':
        color = '#4d90fe';
        message = formatMessage({ id: 'operating' });
        break;
      case 'disabled':
        color = '#f44336';
        message = formatMessage({ id: 'unavailable' });
        break;
      default:
    }
    return (
      <div
        className={`${prefixCls}-card-wrap-content-status`}
        style={{ borderColor: color, color }}
      >
        {message}
      </div>
    );
  }


  return (
    <div className={`${prefixCls}-card-wrap ${className}`}>
      <div className={`${prefixCls}-card-wrap-title`}>
        <span>{name}</span>
        {errorMessage && (
          <Tooltip placement="top" title={errorMessage}>
            <Icon type="error" className={`${prefixCls}-card-wrap-title-icon`} />
          </Tooltip>
        )}
      </div>
      <div className={`${prefixCls}-card-wrap-content`}>
        {getStatus()}
        <div className={`${prefixCls}-card-wrap-content-text`}>
          {progress || describe}
        </div>
      </div>
      <div className={`${prefixCls}-card-wrap-footer`}>
        {map(buttonData, ({ text, onClick, loading, disabled, popoverContent }, index) => (
          <Tooltip
            placement="bottom"
            title={popoverContent}
            arrowPointAtCenter
            overlayClassName={`${prefixCls}-card-wrap-footer-popover`}
            key={index}
          >
            <Button
              className={`${prefixCls}-card-wrap-footer-button`}
              color="primary"
              onClick={onClick}
              loading={loading}
              disabled={disabled}
            >
              {text}
            </Button>
          </Tooltip>
        ))}
      </div>
    </div>
  );
};
