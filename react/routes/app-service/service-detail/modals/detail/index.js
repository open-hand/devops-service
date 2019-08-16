import React from 'react';
import { FormattedMessage } from 'react-intl';
import _ from 'lodash';
import { Avatar } from 'choerodon-ui';
import './index.less';

export default function ({ record, formatMessage, prefixCls, intlPrefix }) {
  let statusLabel;
  if (record.get('fail')) {
    // 失败
    statusLabel = {
      status: 'failed',
      color: '#f44336',
    };
  } else if (record.get('synchro') && record.get('active')) {
    // 运行中
    statusLabel = {
      status: 'active',
      color: '#00bf96',
    };
  } else if (record.get('active')) {
    // 创建中
    statusLabel = {
      status: 'ci_created',
      color: '#4d90fe',
    };
  } else {
    // 停止
    statusLabel = {
      status: 'stop',
      color: '#d3d3d3',
    };
  }
  return (
    <ul className={`${prefixCls}-detail`}>
      <li className="avatar">
        {
          record 
          && record.get('imgUrl')
            ? <Avatar size={64} src={record.get('imgUrl')} />
            : <Avatar size={64}>U</Avatar>
        }
        
      </li>
      <li className="detail-item">
        <span className="detail-item-text">
          {formatMessage({ id: `${intlPrefix}.name` })}:
        </span>
        <span>{record
          && record.get('name')}</span>
      </li>
      <li className="detail-item detail-item-has-url">
        <span className="detail-item-text">
          {formatMessage({ id: 'app.status' })}:
        </span>
        <StatusLabel {...statusLabel} />
      </li>
      <li className="detail-item">
        <span className="detail-item-text">
          {formatMessage({ id: `${intlPrefix}.type` })}:
        </span>
        <span>{record
          && record.get('type') && formatMessage({ id: `app.type.${record.get('type')}` })}</span>
      </li>
      <li className="detail-item detail-item-has-url">
        <span className="detail-item-text">
          {formatMessage({ id: 'repository.head' })}:
        </span>
        {record
          && record.get('repoUrl') ? <a
            href={record.get('repoUrl')}
            className="detail-item-url"
            target="_blank"
            rel="nofollow me noopener noreferrer"
          >
            <span>{record.get('repoUrl')}</span>
          </a> : '-'}
      </li>
      <li className="detail-item">
        <span className="detail-item-text">
          {formatMessage({ id: 'createDate' })}:
        </span>
        <span>{
          (record
            && record.get('creationDate')) || '-'
        }</span>
      </li>
      <li className="detail-item">
        <span className="detail-item-text">
          {formatMessage({ id: 'creator' })}:
        </span>
        <span>{
          (record
            && record.get('creatorName')) || '-'
        }</span>
      </li>
      <li className="detail-item">
        <span className="detail-item-text">
          {formatMessage({ id: 'updateDate' })}:
        </span>
        <span>{(record
          && record.get('lastUpdateDate')) || '-'}</span>
      </li>
      <li className="detail-item">
        <span className="detail-item-text">
          {formatMessage({ id: 'updater' })}:
        </span>
        <span>更新者</span>
      </li>
    </ul>
  );
}

function StatusLabel(props) {
  return (
    <div className="status-label" style={{ background: props.color }}>
      <span className="status-label-text"><FormattedMessage id={props.status} /></span>
    </div>
  );
}
