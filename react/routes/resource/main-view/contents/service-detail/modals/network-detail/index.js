import React from 'react';
import { FormattedMessage } from 'react-intl';
import _ from 'lodash';
import './index.less';

/**
 * 传入一个数组，返回这个数组的每一项组成的段落
 * @param arr 遍历的数组 
 * @param valueKey p标签中文本 在数组元素中的key 不传直接取数组的元素
 */
function generateManyP(arr, valueKey) {
  return _.map(arr, (value, key) => <p className="" key={key}>{valueKey ? value[valueKey] : value}</p>);
}

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
    instanceCode = generateManyP(record.get('target').instances, 'code');
  }
  let externalIps = '-';
  if (record && record.get('config') && record.get('config').externalIps) {
    externalIps = generateManyP(record.get('config').externalIps);
  }
  return (
    <div className={`${prefixCls}-net`}>
      <p className={`${prefixCls}-net-title`}>详情</p>
      <ul className={`${prefixCls}-application-detail-modal`}>
        <li className="detail-item">
          <div className="detail-item-one-to-many">
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
          <div className="detail-item-one-to-many">
            <div>
              <span className="detail-item-text">
                {formatMessage({ id: `${intlPrefix}.application.net.ip` })}:
              </span>
            </div>
            <div>
              {externalIps}
            </div>
          </div>
        </li>
        <li className="detail-item">
          <span className="detail-item-text">
            {formatMessage({ id: `${intlPrefix}.net.selecter` })}:
          </span>
          <span className="detail-item-more-text">{(record 
            && record.get('target') 
            && record.get('target').labels
            && _.map(record.get('target').labels, (value, key) => `${key}=${value}`).join(' ,')) || '-' }</span>
        </li>
        <li className="detail-item">
          <span className="detail-item-text">
            {formatMessage({ id: `${intlPrefix}.net.dns` })}:
          </span>
          <span className="detail-item-more-text">{(record 
            && record.get('dns')) || '-' }</span>
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
        ))}
      
    </div>
  );
}
