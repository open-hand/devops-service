import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter, Link } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Header } from '@choerodon/boot';
import { Button, Select, Tooltip } from 'choerodon-ui';
import { CopyToClipboard } from 'react-copy-to-clipboard';
import _ from 'lodash';
import DevPipelineStore from '../../devPipeline';
import CodeQualityStore from '../../codeQuality/stores';
import handleMapStore from '../main-view/store/handleMapStore';
import './index.less';
import branch from '../../branch';


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
      location: { state },
    } = this.props;
    
    const { appId } = state || {};
    let type = '';
    switch (this.state.name) {
      case 'CodeQuality':
        type = 'quality';
        break;
      case 'CodeManagerBranch':
        type = 'branch';
        break;
      default:
        type = '';
    }
    DevPipelineStore.queryAppData(projectId, type, appId);
  }

  /**
   * 通过下拉选择器选择应用时，获取应用id
   * @param id
   */
  handleSelect = (value, option) => {
    DevPipelineStore.setSelectApp(value);
    DevPipelineStore.setRecentApp(value);
    handleMapStore[this.state.name].select(value, option);
  };

  
  /**
   * 点击复制代码成功回调
   * @returns {*|string}
   */
  handleCopy = () => Choerodon.prompt('复制成功');

  handleRefresh = () => {
    handleMapStore[this.state.name].refresh();
  };

  render() {
    const {
      intl: { formatMessage },
      location: {
        search,
      },
    } = this.props;
    const appData = DevPipelineStore.getAppData;
    const appId = DevPipelineStore.getSelectApp;
    const { getAppData, getRecentApp, getSelectApp } = DevPipelineStore;
    const app = _.find(getAppData, ['id', getSelectApp]);
    const currentApp = _.find(appData, ['id', appId]);
    const noRepoUrl = formatMessage({ id: 'repository.noUrl' });

    return <Header>
      <Select
        filter
        className="c7n-header-select c7n-header-select-noborder"
        dropdownClassName="c7n-header-select_drop"
        placeholder={formatMessage({ id: 'ist.noApp' })}
        value={getSelectApp}
        disabled={getAppData.length === 0}
        filterOption={(input, option) => option.props.children.props.children.props.children
          .toLowerCase().indexOf(input.toLowerCase()) >= 0}
        onChange={(value, option) => this.handleSelect(value, option)}
      >
        <OptGroup label={formatMessage({ id: 'recent' })} key="recent">
          {
                  _.map(getRecentApp, ({ id, permission, code, name: opName }) => (
                    <Option
                      key={`recent-${id}`}
                      value={id}
                      disabled={!permission}
                    >
                      <Tooltip title={code}>
                        <span className="c7n-ib-width_100">{opName}</span>
                      </Tooltip>
                    </Option>))
                }
        </OptGroup>
        <OptGroup label={formatMessage({ id: 'deploy.app' })} key="app">
          {
                  _.map(getAppData, ({ id, code, name: opName, permission }, index) => (
                    <Option
                      value={id}
                      key={index}
                      disabled={!permission}
                    >
                      <Tooltip title={code}>
                        <span className="c7n-ib-width_100">{opName}</span>
                      </Tooltip>
                    </Option>))
                }
        </OptGroup>
      </Select>
      {currentApp && currentApp.repoUrl
        ? <Tooltip title={<FormattedMessage id="repository.copyUrl" />} placement="bottom">
          <CopyToClipboard
            text={currentApp.repoUrl || noRepoUrl}
            onCopy={this.handleCopy}
          >
            <Button icon="content_copy">
              <FormattedMessage id="repository.copyUrl" />  
            </Button>
          </CopyToClipboard>
        </Tooltip> : null}
      <Button
        onClick={this.handleRefresh}
        icon="refresh"
      ><FormattedMessage id="refresh" /></Button>
    </Header>;
  }
}

export default CodeManagerToolBar;
