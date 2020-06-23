import React, { useEffect, Fragment, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { withRouter, Link } from 'react-router-dom';
import _ from 'lodash';

import { Icon } from 'choerodon-ui/pro';
import { Page } from '@choerodon/boot';

import EmptyPage from '../../../../../../components/empty-page';
import Loading from '../../../../../../components/loading';
import Percentage from '../../../../../../components/percentage/Percentage';
import Rating from '../../../../../../components/rating/Rating';

import { QUALITY_LIST, OBJECT_TYPE } from './components/Constants';

import { useCodeQualityStore } from './stores';

import './index.less';

export default withRouter(observer((props) => {
  const { formatMessage, codeQuality, appServiceId } = useCodeQualityStore();

  const { location: {
    search,
  } } = props;

  function getDetail() {
    const { date, status, sonarContents } = codeQuality.data || {};

    // 合并数据，生成{key, value, icon, url, rate, hasReport}对象数组
    const qualityList = [];
    _.map(QUALITY_LIST, (item) => {
      const data = _.find(sonarContents, ({ key }) => item.key === key) || {};
      qualityList.push({ ...item, ...data });
    });
    const quality = {
      reliability: qualityList.slice(0, 4),
      maintainability: qualityList.slice(4, 8),
      coverage: qualityList.slice(8, 11),
      duplications: qualityList.slice(11, 14),
    };

    return (
      date || status ? (
        <div className="c7n-codeQuality-content">
          <div className="c7n-codeQuality-content-head">
            <span className="codeQuality-head-title">{formatMessage({ id: 'codeQuality.content.title' })}</span>
            <span className={`codeQuality-head-status codeQuality-head-status-${status}`}>{formatMessage({ id: `codeQuality.status.${status}` })}</span>
            <span className="codeQuality-head-date">执行时间：{date.split('+')[0].replace(/T/g, ' ')}</span>
          </div>
          {_.map(quality, (value, objKey) => (
            <div className="c7n-codeQuality-detail" key={objKey}>
              <div className="codeQuality-detail-title">{formatMessage({ id: `codeQuality.detail.${objKey}` })}</div>
              <div className="codeQuality-detail-content">
                {
                  _.map(value, ({ icon, key, hasReport, isPercent, value: innerValue, rate, url }) => (
                    <div className="detail-content-block" key={key}>
                      <Icon type={icon} />
                      <span className="detail-content-block-title">{formatMessage({ id: `codeQuality.${key}` })}：</span>
                      {url ? (
                        <a href={url} target="_blank" rel="nofollow me noopener noreferrer">
                          <span className="block-number-link">{innerValue.match(/\d+(\.\d+)?/g)}</span>
                          <span className="block-number-percentage">{innerValue.replace(/\d+(\.\d+)?/g, '')}</span>
                          {isPercent && <span className="block-number-percentage">%</span>}
                        </a>) : (
                          <span className={`block-number ${!innerValue && 'block-number-noValue'}`}>{innerValue || formatMessage({ id: 'nodata' })}</span>
                      )}
                      {rate && key !== 'duplicated_lines_density' && <Rating rating={rate} />}
                      {key === 'coverage' && <Percentage data={Number(innerValue)} />}
                      {key === 'duplicated_lines_density' && <Rating rating={rate} size="18px" type="pie" />}
                      {hasReport && (
                        <Link
                          to={{
                            pathname: '/devops/reports/code-quality',
                            search: `${search}&from=ci`,
                            state: { appId: appServiceId, type: OBJECT_TYPE[objKey] },
                          }}
                        >
                          <Icon type="timeline" className="reports-icon" />
                        </Link>
                      )}
                    </div>
                  ))
                }
              </div>
            </div>
          ))}
        </div>
      ) : (
        <EmptyPage
          title={formatMessage({ id: 'codeQuality.empty.title' })}
          describe={formatMessage({ id: 'codeQuality.empty.content' })}
          access
        />
      )
    );
  }
  return (
    <div
      className="c7n-region c7n-codeQuality-wrapper"
    >
      {codeQuality.loading ? <Loading display /> : getDetail()}
    </div>
  );
}));
