import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Page, Header, Content } from '@choerodon/boot';
import { Select, Button, Tooltip, Icon } from 'choerodon-ui';
import _ from 'lodash';
import LoadingBar from '../../../../components/loadingBar/LoadingBar';
import DevPipelineStore from "../../../../stores/project/devPipeline";
import DepPipelineEmpty from "../../../../components/DepPipelineEmpty/DepPipelineEmpty";
import Percentage from "../../../../components/percentage/Percentage";
import Rating from "../../../../components/rating/Rating";

import './CodeQuality.scss';
import '../../../main.scss';

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
      CodeQualityStore,
      AppState: { currentMenuType: { projectId } },
      location: { state },
    } = this.props;
    const historyAppId = state || {};
    DevPipelineStore.queryAppData(projectId, "quality");
  }

  handleRefresh = () => {
    const {
      CodeQualityStore,
      AppState: { currentMenuType: { projectId } },
    } = this.props;
    // CodeQualityStore.loadData(projectId, DevPipelineStore.getSelectApp);
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
    // CodeQualityStore.loadData(projectId, value)
  };

  getDetail = () => {
    const {
      CodeQualityStore,
      intl: { formatMessage },
    } = this.props;
    // const { getData } = CodeQualityStore;
    const getData = {
      date: "2019年4月15日 下午21:46",
      status: "success",
      DTO: {
        reliability: [
          {
            icon: "bug_report",
            title: "Bugs：",
            hasReport: true,
            number: 12,
            rating: "B",
          },
          {
            icon: "unlock",
            title: formatMessage({ id: "codeQuality.vulnerabilities" }),
            hasReport: true,
            number: 21,
            rating: "C",
          },
          {
            icon: "bug_report",
            title: formatMessage({ id: "codeQuality.bugs.new" }),
            hasReport: false,
            number: 2,
            rating: "A",
          },
          {
            icon: "unlock",
            title: formatMessage({ id: "codeQuality.vulnerabilities.new" }),
            hasReport: false,
            number: 10,
            rating: "D",
          },
        ],
        maintainability: [
          {
            icon: "opacity",
            title: formatMessage({ id: "codeQuality.debt" }),
            hasReport: true,
            number: 36,
            rating: "B",
          },
          {
            icon: "group_work",
            title: formatMessage({ id: "codeQuality.code.smells" }),
            hasReport: false,
            number: 21,
            rating: "A",
          },
          {
            icon: "opacity",
            title: formatMessage({ id: "codeQuality.debt.new" }),
            hasReport: false,
            number: 6,
            rating: "B",
          },
          {
            icon: "group_work",
            title: formatMessage({ id: "codeQuality.code.smells.new" }),
            hasReport: false,
            number: 12,
            rating: "C",
          },
        ],
        coverage: [
          {
            icon: "fiber_smart_record",
            title: formatMessage({ id: "codeQuality.coverage" }),
            hasReport: true,
            number: 25,
            rating: null,
          },
          {
            icon: "adjust",
            title: formatMessage({ id: "codeQuality.unit.tests" }),
            hasReport: false,
            number: 0,
            rating: null,
          },
          {
            icon: "fiber_smart_record",
            title: formatMessage({ id: "codeQuality.coverage.new" }),
            hasReport: false,
            number: 12,
            rating: null,
          },
        ],
        duplications: [
          {
            icon: "adjust",
            title: formatMessage({ id: "codeQuality.duplications" }),
            hasReport: true,
            number: 21,
            rating: null,
          },
          {
            icon: "adjust",
            title: formatMessage({ id: "codeQuality.duplications.blocks" }),
            hasReport: false,
            number: 231,
            rating: null,
          },
          {
            icon: "adjust",
            title: formatMessage({ id: "codeQuality.duplications.new" }),
            hasReport: false,
            number: 12,
            rating: null,
          },
        ],
      },
    };
    const { date, status, DTO } = getData;
    return (
      _.isEmpty(getData) ? <FormattedMessage id="codeQuality.empty" /> : (
        <div className="c7n-codeQuality-content">
          <div className="c7n-codeQuality-content-head">
            <span className="codeQuality-head-title">{formatMessage({ id: "codeQuality.content.title" })}</span>
            <span className={`codeQuality-head-status codeQuality-head-status-${status}`}>{formatMessage({ id: `codeQuality.status.${status}` })}</span>
            <span className="codeQuality-head-date">{formatMessage({ id: "codeQuality.analysis"})}{date}</span>
          </div>
          {_.map(DTO, (value, key) => (
            <div className="c7n-codeQuality-detail" key={key}>
              <div className="codeQuality-detail-title"><FormattedMessage id={`codeQuality.detail.${key}`} /></div>
              <div className="codeQuality-detail-content">
                {
                  _.map(value, ({ icon, title, hasReport, number, rating }, index) => (
                    <div className="detail-content-block" key={title}>
                      <Icon type={icon} />
                      <span className="detail-content-block-title">{title}：</span>
                      <span className="detail-content-block-number">{number}</span>
                      <span className="detail-content-block-number-percentage">{(key === "coverage" || key === "duplications") && index !== 1 ? "%" : ""}</span>
                      {rating && <Rating rating={rating} />}
                      {key === "coverage" && index === 0 && <Percentage data={number} />}
                      {key === "duplications" && index === 0 && <div className="duplications-rating duplications-rating-A" />}
                      {hasReport && <Icon type="timeline" />}
                    </div>
                  ))
                }
              </div>
            </div>
          ))}
        </div>
      )
    );
  };

  render() {
    const {
      intl: { formatMessage },
      AppState: {
        currentMenuType: {
          projectId,
          type,
          name,
          organizationId,
        },
      },
      location: {
        search,
        state,
      },
      CodeQualityStore,
    } = this.props;
    const {
      getLoading,
    } = CodeQualityStore;

    const backPath = "";
    const appData = DevPipelineStore.getAppData;
    const appId = DevPipelineStore.getSelectApp;
    const titleName = _.find(appData, ['id', appId]) ? _.find(appData, ['id', appId]).name : name;
    return (
      <Page
        className="c7n-region c7n-codeQuality-wrapper"
        service={[
          'devops-service.application.listByActive',
        ]}
      >
        {appData && appData.length && appId ? <Fragment>
          <Header
            title={formatMessage({ id: 'codeQuality.head' })}
            backPath={backPath}
          >
            <Select
              filter
              className="c7n-header-select"
              dropdownClassName="c7n-header-select_drop"
              placeholder={formatMessage({ id: 'ist.noApp' })}
              value={DevPipelineStore.getSelectApp}
              disabled={appData.length === 0}
              filterOption={(input, option) => option.props.children.props.children.props.children
                .toLowerCase().indexOf(input.toLowerCase()) >= 0}
              onChange={this.handleSelect}
            >
              <OptGroup label={formatMessage({ id: 'recent' })} key="recent">
                {
                  _.map(DevPipelineStore.getRecentApp, app => (
                    <Option
                      key={`recent-${app.id}`}
                      value={app.id}
                      disabled={!app.permission}
                    >
                      <Tooltip title={app.code}><span className="c7n-ib-width_100">{app.name}</span></Tooltip>
                    </Option>))
                }
              </OptGroup>
              <OptGroup label={formatMessage({ id: 'deploy.app' })} key="app">
                {
                  _.map(appData, (app, index) => (
                    <Option
                      value={app.id}
                      key={index}
                      disabled={!app.permission}
                    >
                      <Tooltip title={app.code}><span className="c7n-ib-width_100">{app.name}</span></Tooltip>
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
