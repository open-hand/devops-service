import React, { Component, Fragment, useMemo } from 'react';
import { observer, inject } from 'mobx-react';
import { observer as observerLite } from 'mobx-react-lite';
import { withRouter, Link } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Header } from '@choerodon/master';
import { Button, Select, Tooltip } from 'choerodon-ui';
import { CopyToClipboard } from 'react-copy-to-clipboard';
import _ from 'lodash';
import DevPipelineStore from '../stores/DevPipelineStore';
import handleMapStore from '../main-view/store/handleMapStore';
import './index.less';


const { Option, OptGroup } = Select;


@injectIntl
@withRouter
@inject('AppState')
@observer
class CodeManagerToolBar extends Component {
  constructor(props) {
    super(props);
    this.state = {
      name: props.name,
    };
  }

  componentDidMount() {
    const {
      AppState: { currentMenuType: { projectId } },
      name,
    } = this.props;
    DevPipelineStore.queryAppData(projectId, name, this.handleRefresh, false);
  }

  /**
   * 点击复制代码成功回调
   * @returns {*|string}
   */
  handleCopy = () => Choerodon.prompt('复制成功');

  handleRefresh = () => {
    handleMapStore[this.state.name].refresh();
  };

  /**
   * @param isInit 表示是否需要刷新app数据
   */
  refreshApp = (isInit = true) => {
    const {
      AppState: { currentMenuType: { projectId } },
      name,
    } = this.props;
    DevPipelineStore.queryAppData(projectId, name, this.handleRefresh, true, this.changeLoding);
  }

  getSelfToolBar = () => {
    const obj = handleMapStore[this.state.name]
    && handleMapStore[this.state.name].getSelfToolBar
    && handleMapStore[this.state.name].getSelfToolBar();
    return obj || null;
  }


  render() {
    const {
      intl: { formatMessage },
    } = this.props;
    const appData = DevPipelineStore.getAppData;
    const appId = DevPipelineStore.getSelectApp;
    const currentApp = _.find(appData, ['id', appId]);
    const noRepoUrl = formatMessage({ id: 'repository.noUrl' });

    return <Header>
      {this.getSelfToolBar()}
      
      <CopyToClipboard
        text={(currentApp && currentApp.repoUrl) || noRepoUrl}
        onCopy={this.handleCopy}
      >
        <Tooltip title={<FormattedMessage id="repository.copyUrl" />} placement="bottom">
          <Button icon="content_copy" disabled={!(currentApp && currentApp.repoUrl)}>
            <FormattedMessage id="repository.copyUrl" />
          </Button>
        </Tooltip> 
      </CopyToClipboard>
      
      <Button
        onClick={this.refreshApp}
        icon="refresh"
      ><FormattedMessage id="refresh" /></Button>
    </Header>;
  }
}


export default CodeManagerToolBar;

export const SelectApp = injectIntl(inject('AppState')(observerLite((props) => {
  const handleSelect = (value, option) => {
    DevPipelineStore.setSelectApp(value);
    DevPipelineStore.setRecentApp(value);
    Object.keys(handleMapStore).forEach((key) => {
      if (key.indexOf('Code') !== -1) {
        handleMapStore[key]
        && handleMapStore[key].select
        && handleMapStore[key].select(value, option);
      }
    });
  };
  const {
    intl: { formatMessage },
  } = props;
  const { getAppData, getRecentApp, getSelectApp } = DevPipelineStore;
  return <Select
    filter
    filterOption={(input, option) => option.props.children.props.children.props.children
      .toLowerCase().indexOf(input.toLowerCase()) >= 0}
    placeholder={formatMessage({ id: 'ist.noApp' })}
    disabled={getAppData.length === 0}
    label={formatMessage({ id: 'c7ncd.deployment.app-service' })}
    className="c7n-code-managerment-select-app"
    value={getSelectApp}
    onChange={handleSelect}
  >
    <OptGroup label={formatMessage({ id: 'recent' })} key="recent">
      {
          _.map(getRecentApp, ({ id, code, name: opName }) => (
            <Option
              key={`recent-${id}`}
              value={id}
            >
              <Tooltip title={code}>
                <span className="c7n-ib-width_100">{opName}</span>
              </Tooltip>
            </Option>))
        }
    </OptGroup>
    <OptGroup label={formatMessage({ id: 'deploy.app' })} key="app">
      {
          _.map(getAppData, ({ id, code, name: opName }, index) => (
            <Option
              value={id}
              key={index}
            >
              <Tooltip title={code}>
                <span className="c7n-ib-width_100">{opName}</span>
              </Tooltip>
            </Option>))
        }
    </OptGroup>
  </Select>;
})));
