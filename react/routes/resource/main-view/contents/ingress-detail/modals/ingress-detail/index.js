import React from 'react';
import { FormattedMessage } from 'react-intl';
import _ from 'lodash';

import './index.less';


export default function ({ intlPrefix, record, prefixCls, formatMessage }) {
  return (
    <ul className={`${prefixCls}-secret-detail-modal`}>
      <li className="detail-item">
        <span className="detail-item-text">
          {formatMessage({ id: 'instance' })}:
        </span>
        <span>{
            (record 
            && record.get('instances') && record.get('instances').join(',')) || '-' 
          }</span>
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
            && record.get('lastUpdateDate')) || '-' }</span>
      </li>
      <li className="detail-item">
        <span className="detail-item-text">
          {formatMessage({ id: 'updater' })}:
        </span>
        <span>{(record 
            && record.get('lastUpdaterName')) || '-' }</span>
      </li>
    </ul>
     
  );
}
