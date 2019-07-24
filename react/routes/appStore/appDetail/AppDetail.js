import React, { Component } from 'react/index';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { Button, Input, Icon, Card, Select } from 'choerodon-ui';
import { Content, Header, Page, Permission, stores } from '@choerodon/boot';
import { injectIntl, FormattedMessage } from 'react-intl';
import ReactMarkdown from 'react-markdown';
import _ from 'lodash';
import LoadingBar from '../../../components/loadingBar';
import './AppDetail.scss';
import '../../main.scss';
import '../../../components/MdEditor/preview.css';

const Option = Select.Option;

const { AppState } = stores;

@observer
class AppDetail extends Component {
  constructor(props) {
    super(props);
    this.state = {
      verId: '',
      versions: '',
      id: props.match.params.id,
    };
  }

  componentDidMount() {
    const { AppStoreStore } = this.props;
    AppStoreStore.setBackPath(false);
    this.loadAppData();
  }

  componentWillUnmount() {
    const { AppStoreStore } = this.props;
    AppStoreStore.setApp([]);
    AppStoreStore.setReadme(false);
  }

  /**
   * 刷新函数
   */
  reload = () => {
    this.loadAppData();
  };

  /**
   * 处理页面跳转
   * @param url 跳转地址
   */
  linkToChange = (url) => {
    const { history } = this.props;
    history.push(url);
  };

  /**
   * 选择版本
   * @param verId
   */
  handleChange = (verId, option) => {
    this.setState({
      verId,
      version: option.props.children || '',
    });
    this.loadReadmes(verId);
  };

  /**
   * 条件部署应用
   * @param app 应用详细数据
   */
  deployApp = ({ appId, appVersions }) => {
    const {
      AppStoreStore,
      history,
      location: {
        search,
      },
    } = this.props;
    const { version } = this.state;
    const selectedVersion = version || appVersions[0].version;

    AppStoreStore.setBackPath(true);

    history.push({
      pathname: '/devops/deployment-app',
      search,
      state: {
        appId,
        version: selectedVersion,
        prevPage: 'market',
      },
    });

  };

  /**
   * 加载单应用数据
   */
  loadAppData = () => {
    const { AppStoreStore } = this.props;
    const { id, verId } = this.state;
    const projectId = AppState.currentMenuType.id;
    AppStoreStore.loadAppStore(projectId, id).then((app) => {
      if (app) {
        const ver = app.appVersions ? _.slice(app.appVersions) : [];
        this.loadReadmes(verId || ver[0].id);
      }
    });
  };

  /**
   * 加载对应版本readme
   * @param verId
   */
  loadReadmes = (verId) => {
    const { AppStoreStore } = this.props;
    const { id } = this.state;
    const projectId = AppState.currentMenuType.id;
    AppStoreStore.loadReadme(projectId, id, verId);
  };

  render() {
    const { AppStoreStore, intl } = this.props;
    const projectName = AppState.currentMenuType.name;
    const projectId = AppState.currentMenuType.id;
    const organizationId = AppState.currentMenuType.organizationId;
    const type = AppState.currentMenuType.type;
    const app = AppStoreStore.getApp;
    const readme = AppStoreStore.getReadme || intl.formatMessage({ id: 'appstore.noMD' });
    const appVers = app.appVersions ? _.slice(app.appVersions) : [];
    const appVersion = _.map(appVers, d => <Option key={d.id}>{d.version}</Option>);
    const imgDom = app.imgUrl ? <div className="c7n-store-img" style={{ backgroundImage: `url(${Choerodon.fileServer(app.imgUrl)})` }} /> : <div className="c7n-store-img" />;

    return (
      <Page
        className="c7n-region"
        service={[
          'devops-service.application-market.queryApp',
          'devops-service.application-market.queryAppVersionReadme',
        ]}
      >
        <Header title={intl.formatMessage({ id: 'app.appDetail' })} backPath={`/devops/app-market?type=${type}&id=${projectId}&name=${projectName}&organizationId=${organizationId}`}>
          <Button
            funcType="flat"
            onClick={this.reload}
          >
            <i className="icon-refresh icon" />
            <FormattedMessage id="refresh" />
          </Button>
        </Header>
        {AppStoreStore.isLoading ? <LoadingBar display />
          : (<div className="c7n-store-app-content">
            <div className="c7n-store-detail-head">
              <div className="c7n-store-detail-left">
                <div className="c7n-store-img-wrap">
                  {imgDom}
                </div>
              </div>
              <div className="c7n-store-detail-right">
                <div className="c7n-store-name">{app.name}</div>
                <div className="c7n-store-contributor">
                  <FormattedMessage id="ist.ctr" />
                  {app.contributor}</div>
                <div className="c7n-store-des">{app.description}</div>
                <div>
                  <span className="c7n-store-circle">V</span>
                  <Select
                    size="large"
                    defaultValue={appVers.length ? appVers[0].version : ''}
                    className="c7n-store-select"
                    optionFilterProp="children"
                    filterOption={(input, option) => option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
                    onChange={this.handleChange}
                    filter
                    showSearch
                  >
                    {appVersion}
                  </Select>
                  <Permission
                    service={['devops-service.application-instance.deploy']}
                    organizationId={organizationId}
                    projectId={projectId}
                    type={type}
                  >
                    <Button
                      className="c7n-store-deploy"
                      type="primary"
                      funcType="raised"
                      onClick={this.deployApp.bind(this, app)}
                    >
                      <FormattedMessage id="appstore.deploy" />
                    </Button>
                  </Permission>
                </div>
              </div>
            </div>
            <div className="c7n-store-detail">
              <div className="c7n-store-detail-left">
                <div className="c7n-store-key"><FormattedMessage id="appstore.category" /></div>
                <div className="c7n-store-type">{app.category}</div>
                <div className="c7n-store-key"><FormattedMessage id="appstore.lastDate" /></div>
                <div className="c7n-store-time">{app.lastUpdatedDate || 'xx-xx-xx'}</div>
              </div>
              <div className="c7n-store-detail-right">
                <div className="c7n-store-detail-overview">
                  <h1>README</h1>
                  <div className="c7n-md-parse">
                    <ReactMarkdown
                      source={readme}
                      skipHtml={false}
                      escapeHtml={false}
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>)}
      </Page>
    );
  }
}

export default withRouter(injectIntl(AppDetail));
