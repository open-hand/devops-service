import React from 'react';
import { FormattedMessage } from 'react-intl';
import _ from 'lodash';

import './index.less';


export default function ({ intlPrefix, record, prefixCls, formatMessage }) {
  let updater = (record && record.get('lastUpdaterName'));
  if (!updater) {
    if (record.get('creatorDate') === record.get('lastUpdateDate')) {
      updater = '-';
    } else {
      updater = 'GitOps';
    }
  }
  let instanceCode = '-';
  if (record && record.get('target') && record.get('target').instances && record.get('target').instances.length !== 0) {
    const codeArr = _.map(record.get('target').instances, (value, key) => <p className="" key={key}>{value.code}</p>);
    instanceCode = codeArr;
  }
  return (
    <div className={`${prefixCls}-net`}>
      <p className={`${prefixCls}-net-title`}>详情</p>
      <ul className={`${prefixCls}-application-detail-modal`}>
        <li className="detail-item">
          <div className="instance">
            <div>
              <span className="detail-item-text">
                {formatMessage({ id: 'instance' })}:
              </span>
            </div>
            <div>
              {instanceCode}
            </div>
          </div>
        </li>
        <li className="detail-item">
          <span className="detail-item-text">
            {formatMessage({ id: `${intlPrefix}.application.net.configType` })}:
          </span>
          <span>{(record 
            && record.get('type')) || '-'}</span>
        </li>
        <li className="detail-item">
          <span className="detail-item-text">
            {formatMessage({ id: `${intlPrefix}.application.net.ip` })}:
          </span>
          <span>{(record 
            && record.get('config') 
            && record.get('config').externalIps
            && record.get('config').externalIps.join(' ,')) || '-' }</span>
        </li>
        <li className="detail-item">
          <span className="detail-item-text">
            {formatMessage({ id: `${intlPrefix}.net.selecter` })}:
          </span>
          <span>{(record 
            && record.get('target') 
            && record.get('target').labels
            && _.map(record.get('target').labels, (value, key) => `${key}=${value}`).join(' ,')) || '-' }</span>
        </li>
        <li className="detail-item">
          <span className="detail-item-text">
            {formatMessage({ id: `${intlPrefix}.net.dns` })}:
          </span>
          <span>{(record 
            && record.get('dns')) || '-' }</span>
        </li>
        <li className="detail-item">
          <span className="detail-item-text">
            {formatMessage({ id: 'createDate' })}:
          </span>
          <span>{
            (record 
            && record.get('creatorDate')) || '-' 
          }</span>
        </li>
        <li className="detail-item">
          <span className="detail-item-text">
            {formatMessage({ id: 'creator' })}:
          </span>
          <span>{
            (record 
            && record.get('creatorName')) || 'GitOps' 
          }</span>
        </li>
        <li className="detail-item">
          <span className="detail-item-text">
            {formatMessage({ id: 'updateDate' })}:
          </span>
          <span>{(record 
            && record.get('lastUpdateDate')) || '-' }</span>
        </li>
        <li className="detail-item">
          <span className="detail-item-text">
            {formatMessage({ id: 'updater' })}:
          </span>
          <span>{updater}</span>
        </li>
      </ul>
      <hr className="net-hr" />
      <p className={`${prefixCls}-net-title`}>标签</p>
      { record 
        && record.get('labels')
        && _.map(record.get('labels'), (value, key) => (
          <div className={`${prefixCls}-application-label`}>
            <div className={`${prefixCls}-application-label-head`}>
              {key}
            </div>
            <span className={`${prefixCls}-application-label-text`}>{value}</span>
          </div>
        ))
      }
      
    </div>
  );
}
