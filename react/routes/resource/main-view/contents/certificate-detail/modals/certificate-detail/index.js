import React from 'react';
import { FormattedMessage } from 'react-intl';
import _ from 'lodash';
import moment from 'moment'; 
import './index.less';


export default function ({ intlPrefix, record, prefixCls, formatMessage }) {
  return (
    <ul className={`${prefixCls}-detail-modal`}>
      <li className="detail-item">
        <span className="detail-item-text">
          {formatMessage({ id: 'ctf.validDate' })}:
        </span>
        <span>{(record 
            && record.get('validFrom')
            && record.get('validUntil')
            && (`${moment(record.get('validFrom')).format('YYYY.MM.DD')}-${moment(record.get('validUntil')).format('YYYY.MM.DD')}`)) || '-' }</span>
      </li>
      <li className="detail-item">
        <span className="detail-item-text">
          {formatMessage({ id: 'createDate' })}:
        </span>
        <span>{
            (record && record.get('creationDate')) || '-' 
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
    </ul>
  );
}
