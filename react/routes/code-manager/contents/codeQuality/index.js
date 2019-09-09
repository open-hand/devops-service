import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter, Link } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Page, Header, Content } from '@choerodon/master';
import { Select, Button, Tooltip, Icon, Card } from 'choerodon-ui';
import _ from 'lodash';
import Loading from '../../../../components/loading';
import DevPipelineStore from '../../stores/DevPipelineStore';
import Percentage from '../../../../components/percentage/Percentage';
import Rating from '../../../../components/rating/Rating';
import { QUALITY_LIST, OBJECT_TYPE } from './components/Constants';
import CodeQualityStore from './stores';
import handleMapStore from '../../main-view/store/handleMapStore';

import './index.less';
import '../../../main.less';

@injectIntl
@withRouter
@inject('AppState')
@observer
class CodeQuality extends Component {
  constructor(props) {
    super(props);
    handleMapStore.setCodeQuality({
      refresh: this.handleRefresh,
      select: this.handleSelect,
    });
    this.state = {

    };
  }

  componentDidMount() {
    const {
      AppState: { currentMenuType: { projectId } },
      location: { state },
    } = this.props;
    const { appId } = state || {};
  }

  handleRefresh = () => {
    const {
      AppState: { currentMenuType: { projectId } },
    } = this.props;
    CodeQualityStore.loadData(projectId, DevPipelineStore.getSelectApp);
  };

  /**
   * 通过下拉选择器选择应用时，获取应用id
   * @param id
   */
  handleSelect = (value) => {
    const {
      AppState: { currentMenuType: { projectId } },
    } = this.props;
    CodeQualityStore.loadData(projectId, value);
  };

  getDetail = () => {
    const {
      intl: { formatMessage },
      location: {
        search,
      },
    } = this.props;
    const { getSelectApp } = DevPipelineStore;
    const { getData } = CodeQualityStore;
    const { date, status, sonarContents } = getData || {};

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
            <span className="codeQuality-head-date">{formatMessage({ id: 'codeQuality.analysis' })}：{date.split('+')[0].replace(/T/g, ' ')}</span>
          </div>
          {_.map(quality, (value, objKey) => (
            <div className="c7n-codeQuality-detail" key={objKey}>
              <div className="codeQuality-detail-title"><FormattedMessage id={`codeQuality.detail.${objKey}`} /></div>
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
                            search,
                            state: { appId: getSelectApp, type: OBJECT_TYPE[objKey] },
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
        <div className="c7n-codeQuality-empty">
          <Card title={formatMessage({ id: 'codeQuality.empty.title' })}>
            <span className="codeQuality-empty-content">{formatMessage({ id: 'codeQuality.empty.content' })}</span>
            <a
              href={formatMessage({ id: 'codeQuality.link' })}
              target="_blank"
              rel="nofollow me noopener noreferrer"
              className="codeQuality-empty-link"
            >
              <span className="codeQuality-empty-more">{formatMessage({ id: 'learnmore' })}</span>
              <Icon type="open_in_new" />
            </a>
          </Card>
        </div>
      )
    );
  };

  render() {
    const {
      intl: { formatMessage },
      AppState: {
        currentMenuType: { name },
      },
      location: {
        state,
      },
    } = this.props;
    const {
      getLoading,
    } = CodeQualityStore;
    const backPath = state && state.backPath ? state.backPath : '';
    const { getAppData, getRecentApp, getSelectApp } = DevPipelineStore;
    const app = _.find(getAppData, ['id', getSelectApp]);
    const titleName = app ? app.name : name;
    return (
      <Page
        className="c7n-region c7n-codeQuality-wrapper"
        service={[
          'devops-service.app-service.getSonarQube',
        ]}
      >
        {getAppData && getAppData.length && getSelectApp ? <Fragment>
          <Content className="c7n-codeQuality-content">
            {getLoading ? <Loading display /> : this.getDetail()}
          </Content>
        </Fragment> : <Loading display={DevPipelineStore.getLoading} />}
      </Page>
    );
  }
}

export default CodeQuality;
