import React, { Component, Fragment } from 'react';
import { observer } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Page, Header, Content, stores } from '@choerodon/boot';
import { Tag, Card, Select, Button, Tooltip, Spin } from 'choerodon-ui';
import _ from 'lodash';
import './CodeQuality.scss';
import ChartSwitch from '../Component/ChartSwitch';
import LoadingBar from '../../../../components/loadingBar/LoadingBar';

const { AppState } = stores;
const RATING = { '1.0': 'A', '2.0': 'B', '3.0': 'C', '4.0': 'D', '5.0': 'E' };

@observer
class CodeQuality extends Component {
  constructor(props, context) {
    super(props, context);
    this.state = {
    };
  }

  componentDidMount() {
    // const { ServiceId } = this.state;
    // const { AppState, ServiceStore } = this.props;
    // const menuType = AppState.currentMenuType;
    // const proId = menuType.id;
    // ServiceStore.loadSonar(proId, ServiceId).then((data) => {
    //   this.setState({
    //     sonarData: data,
    //   });
    // }).catch(error => handleProptError(error));
  }

  levelType = (num) => {
    if (num > 0 && num < 1000) {
      return 'XS';
    } else if (num >= 1000 && num < 10000) {
      return 'S';
    } else if (num >= 10000 && num < 100000) {
      return 'M';
    } else if (num >= 100000) {
      return 'L';
    }
  };

  repeat = (num) => {
    let value = ['', 0];
    if (num === 0) {
      value = ['#1BC123', 0];
    } else if (num > 0 && num < 10) {
      value = ['#4D90FE', 6];
    } else if (num >= 10 && num < 20) {
      value = ['#FFB100', 8];
    } else if (num >= 20) {
      value = ['#F44336', 9];
    }
    return value;
  };

  render() {
    const { intl: { formatMessage }, history, ReportsStore } = this.props;
    const { dateType } = this.state;
    const { id, name, type, organizationId } = AppState.currentMenuType;
    // const { sonarData } = this.state;
    const loadingBar = (
      <div style={{ display: 'inherit', margin: '200px auto', textAlign: 'center' }}>
        <Spin />
      </div>
    );
    const quality = ['Bugs', '漏洞', '坏味道'];
    const ragting = ['1.0', '4.0', '3.0'];
    const data = [12, 4, 125, 50, 0.5, 10000];
    // if (Object.keys(sonarData).length && sonarData !== 'loading') {
    //   qualityGateDetails = JSON.parse(sonarData.metrics.quality_gate_details).conditions.filter(c => c.level !== 'OK');
    // }
    const sonarData = { status: 'success', analysedAt: '2018年10月18日', metrics: { alert_status: 'OK' } };
    return (
      <Page
        className="c7n-region c7n-ciPipeline"
        service={[
          'devops-service.application.listByActive',
          'devops-service.devops-gitlab-pipeline.listPipelineTime',
          'devops-service.devops-gitlab-pipeline.pagePipeline',
        ]}
      >
        <Header
          title={formatMessage({ id: 'report.build-duration.head' })}
          backPath={`/devops/reports?type=${type}&id=${id}&name=${name}&organizationId=${organizationId}`}
        >
          <ChartSwitch
            history={history}
            current="build-duration"
          />
          <Button
            icon="refresh"
            onClick={this.handleRefresh}
          >
            <FormattedMessage id="refresh" />
          </Button>
        </Header>
        <Content code="report.build-duration" values={{ name }} className="c7n-codeQuality-content">
          <Fragment>
            {sonarData === 'loading' ? loadingBar : <div>
              {Object.keys(sonarData).length ? <div>
                <div className="c7n-warp-card">
                  <div className="c7n-app-title">
                    <i className="icon icon-project" />
                    <div className="c7n-fs15 c7n-mr12">综合状态：</div>
                    <div className={`c7n-app-status c7n-status-${sonarData.status}`}>{sonarData.status || '--'}</div>
                    <div className="c7n-analysis">最后一次分析：{sonarData.analysedAt || '--'}</div>
                  </div>
                  <div className="c7n-quality-content">
                    {
                      _.map(quality, (value, index) => <Fragment>
                        <div className="c7n-quality-rating smaller-card">
                          <div className="c7n-quality-number">
                            <span>{data[index]}</span>
                            <span className={`c7n-rating-small rating-${RATING[ragting[index]]}`}>{RATING[ragting[index]]}</span>
                          </div>
                          <div className="c7n-quality-message">
                            <i className="icon icon-bug_report" />
                            <div className="c7n-quality-label">{value}</div>
                          </div>
                        </div>
                        <div className="c7n-card-border" />
                      </Fragment>)
                    }
                    <div className="c7n-quality-rating smaller-card">
                      <div className="c7n-quality-number">
                        <svg className="c7n-transition-rotate">
                          <circle cx="50%" cy="50%" r="13px" fill="none" stroke="#F44336" strokeWidth="4" strokeLinecap="round" />
                          <circle cx="50%" cy="50%" r="13px" fill="none" stroke="#1BC123" strokeWidth="4" strokeDasharray={`${(2 * Math.PI * 13 * data[3]) / 100}, 1000`} />
                        </svg>
                        <span>{data[3]}</span>
                        <span className="c7n-gray-12">%</span>
                      </div>
                      <div className="c7n-message-text">覆盖率</div>
                    </div>
                    <div className="c7n-card-border" />
                    <div className="c7n-quality-rating smaller-card">
                      <div className="c7n-quality-number">
                        <svg className="c7n-transition-rotate">
                          <circle cx="50%" cy="50%" r="13px" fill="none" stroke={this.repeat(data[4])[0]} strokeWidth="3" strokeLinecap="round" />
                          <circle cx="50%" cy="50%" r={this.repeat(data[4])[1]} fill={this.repeat(data[4])[0]} />
                        </svg>
                        <span>{data[4]}</span>
                        <span className="c7n-gray-12">%</span>
                      </div>
                      <div className="c7n-message-text">重复</div>
                    </div>
                    <div className="c7n-card-border" />
                    <div className="c7n-quality-rating">
                      <div>
                        <span>{data[5]}</span>
                        <div className="c7n-rating-small size-rating">{this.levelType(data[5])}</div>
                      </div>
                      <div className="c7n-mt12">Java, XML</div>
                    </div>
                  </div>
                </div>
              </div> : <p style={{ marginTop: 8 }}>暂无数据，请集成后查看</p>}
            </div>}
          </Fragment>
        </Content>
      </Page>
    );
  }
}

export default withRouter(injectIntl(CodeQuality));
