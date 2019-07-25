import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter, Link } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Page, Header, Content } from '@choerodon/boot';
import { Select, Button, Tooltip, Icon, Card } from 'choerodon-ui';
import _ from 'lodash';
import LoadingBar from '../../../components/loadingBar/LoadingBar';
import DevPipelineStore from "../../../stores/project/devPipeline";
import DepPipelineEmpty from "../../../components/DepPipelineEmpty/DepPipelineEmpty";
import Percentage from "../../../components/percentage/Percentage";
import Rating from "../../../components/rating/Rating";
import { QUALITY_LIST, OBJECT_TYPE } from "../components/Constants";

import './CodeQuality.scss';
import '../../main.scss';

const { Option, OptGroup} = Select;

@injectIntl
@withRouter
@inject('AppState')
@observer
class CodeQuality extends Component {
  constructor(props) {
    super(props);
    this.state = {
    };
  }

  componentDidMount() {
    const {
      AppState: { currentMenuType: { projectId } },
      location: { state },
    } = this.props;
    const { appId } = state || {};
    DevPipelineStore.queryAppData(projectId, "quality", appId);
  }

  handleRefresh = () => {
    const {
      CodeQualityStore,
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
      CodeQualityStore,
      AppState: { currentMenuType: { projectId } },
    } = this.props;
    DevPipelineStore.setSelectApp(value);
    DevPipelineStore.setRecentApp(value);
    CodeQualityStore.loadData(projectId, value)
  };

  getDetail = () => {
    const {
      CodeQualityStore,
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
    _.map(QUALITY_LIST, item => {
      const data = _.find(sonarContents, ({ key }) => item.key === key) || {};
      qualityList.push(Object.assign({}, item, data));
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
            <span className="codeQuality-head-title">{formatMessage({ id: "codeQuality.content.title" })}</span>
            <span className={`codeQuality-head-status codeQuality-head-status-${status}`}>{formatMessage({ id: `codeQuality.status.${status}` })}</span>
            <span className="codeQuality-head-date">{formatMessage({ id: "codeQuality.analysis"})}：{date.split('+')[0].replace(/T/g, ' ')}</span>
          </div>
          {_.map(quality, (value, objKey) => (
            <div className="c7n-codeQuality-detail" key={objKey}>
              <div className="codeQuality-detail-title"><FormattedMessage id={`codeQuality.detail.${objKey}`} /></div>
              <div className="codeQuality-detail-content">
                {
                  _.map(value, ({ icon, key, hasReport, isPercent, value, rate, url }) => (
                    <div className="detail-content-block" key={key}>
                      <Icon type={icon} />
                      <span className="detail-content-block-title">{formatMessage({ id: `codeQuality.${key}`})}：</span>
                      {url ? (
                        <a href={url} target="_blank" rel="nofollow me noopener noreferrer">
                          <span className="block-number-link">{value.match(/\d+(\.\d+)?/g)}</span>
                          <span className="block-number-percentage">{value.replace(/\d+(\.\d+)?/g, '')}</span>
                          {isPercent && <span className="block-number-percentage">%</span>}
                        </a>) : (
                        <span className={`block-number ${!value && "block-number-noValue"}`}>{value || formatMessage({ id: "nodata" })}</span>
                      )}
                      {rate && key !== "duplicated_lines_density" && <Rating rating={rate} />}
                      {key === "coverage" && <Percentage data={Number(value)} />}
                      {key === "duplicated_lines_density" && <Rating rating={rate} size="18px" type="pie" />}
                      {hasReport && (
                        <Link
                          to={{
                            pathname: "/devops/reports/code-quality",
                            search,
                            state: { appId: getSelectApp, type: OBJECT_TYPE[objKey]},
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
          <Card title={formatMessage({ id: "codeQuality.empty.title"})}>
            <span className="codeQuality-empty-content">{formatMessage({ id: "codeQuality.empty.content"})}</span>
            <a
              href={formatMessage({ id: 'codeQuality.link' })}
              target="_blank"
              className="codeQuality-empty-link"
            >
              <span className="codeQuality-empty-more" >{formatMessage({ id: "learnmore" })}</span>
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
      CodeQualityStore,
    } = this.props;
    const {
      getLoading,
    } = CodeQualityStore;
    const backPath = state && state.backPath ? state.backPath : "";
    const { getAppData, getRecentApp, getSelectApp } = DevPipelineStore;
    const app = _.find(getAppData, ['id', getSelectApp]);
    const titleName =  app? app.name : name;
    return (
      <Page
        className="c7n-region c7n-codeQuality-wrapper"
        service={[
          "devops-service.application.listByActive",
          "devops-service.application.getSonarQube",
        ]}
      >
        {getAppData && getAppData.length && getSelectApp ? <Fragment>
          <Header
            title={formatMessage({ id: 'codeQuality.head' })}
            backPath={backPath}
          >
            <Select
              filter
              className="c7n-header-select"
              dropdownClassName="c7n-header-select_drop"
              placeholder={formatMessage({ id: 'ist.noApp' })}
              value={getSelectApp}
              disabled={getAppData.length === 0}
              filterOption={(input, option) => option.props.children.props.children.props.children
                .toLowerCase().indexOf(input.toLowerCase()) >= 0}
              onChange={this.handleSelect}
            >
              <OptGroup label={formatMessage({ id: 'recent' })} key="recent">
                {
                  _.map(getRecentApp, ({ id, permission, code, name }) => (
                    <Option
                      key={`recent-${id}`}
                      value={id}
                      disabled={!permission}
                    >
                      <Tooltip title={code}>
                        <span className="c7n-ib-width_100">{name}</span>
                      </Tooltip>
                    </Option>))
                }
              </OptGroup>
              <OptGroup label={formatMessage({ id: 'deploy.app' })} key="app">
                {
                  _.map(getAppData, ({ id, code, name, permission }, index) => (
                    <Option
                      value={id}
                      key={index}
                      disabled={!permission}
                    >
                      <Tooltip title={code}>
                        <span className="c7n-ib-width_100">{name}</span>
                      </Tooltip>
                    </Option>))
                }
              </OptGroup>
            </Select>
            <Button
              onClick={this.handleRefresh}
              icon="refresh"
            >
              <FormattedMessage id="refresh" />
            </Button>
          </Header>
          <Content code="codeQuality" values={{ name: titleName }}>
            {getLoading ? <LoadingBar display /> : this.getDetail()}
          </Content>
        </Fragment> : <DepPipelineEmpty title={formatMessage({ id: "codeQuality.head" })} type="app" />}
      </Page>
    );
  }
}

export default CodeQuality;
