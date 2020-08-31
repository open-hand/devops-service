import React, { useEffect, useState, Fragment } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Header, Choerodon } from '@choerodon/boot';
import {
  Button, Select, Form, Menu, Dropdown, Icon, UrlField, TextField,
} from 'choerodon-ui/pro';
import { CopyToClipboard } from 'react-copy-to-clipboard';
import { Tooltip } from 'choerodon-ui';
import _ from 'lodash';
import handleMapStore from '../main-view/store/handleMapStore';
import { useCodeManagerStore } from '../stores';
import './index.less';

const { Option, OptGroup } = Select;

const CodeManagerToolBar = injectIntl(inject('AppState')(observer((props) => {
  const { appServiceDs, selectAppDs } = useCodeManagerStore();
  useEffect(() => {
    handleRefresh();
  }, [selectAppDs.current]);

  const { name, intl: { formatMessage } } = props;
  const currentApp = _.find(appServiceDs.toData(), ['id', selectAppDs.current.get('appServiceId')]);
  const noRepoUrl = formatMessage({ id: 'repository.noUrl' });
  const getSelfToolBar = () => {
    const obj = handleMapStore[name]
      && handleMapStore[name].getSelfToolBar
      && handleMapStore[name].getSelfToolBar();
    return obj || null;
  };
  /**
   * 点击复制代码成功回调
   * @returns {*|string}
   */
  const handleCopy = () => { Choerodon.prompt('复制成功'); };

  const handleRefresh = () => {
    handleMapStore[name] && handleMapStore[name].refresh();
  };

  const refreshApp = () => {
    appServiceDs.query().then((data) => {
      if (data && data.length && data.length > 0) {
        selectAppDs.current.set('appServiceId', selectAppDs.current.get('appServiceId') || data[0].id);
        handleRefresh();
      }
    });
  };
  return (
    <>
      <Header>
        {getSelfToolBar()}
        <Button
          onClick={refreshApp}
          icon="refresh"
        >
          {formatMessage({ id: 'refresh' })}
        </Button>
      </Header>
    </>
  );
})));

export default CodeManagerToolBar;

export const SelectApp = injectIntl(inject('AppState')(observer((props) => {
  const codeManagerStore = useCodeManagerStore();
  const { appServiceDs, selectAppDs, projectId } = codeManagerStore;
  const { intl: { formatMessage } } = props;
  const currentApp = _.find(appServiceDs.toData(), ['id', selectAppDs.current.get('appServiceId')]);
  const noRepoUrl = formatMessage({ id: 'repository.noUrl' });

  const handleCopy = () => { Choerodon.prompt('复制成功'); };
  const copyMenu = (
    <div className="c7ncd-copyMenu">
      <Form>
        <TextField
          disabled
          defaultValue={(currentApp && currentApp.sshRepositoryUrl) || noRepoUrl}
          label={formatMessage({ id: 'repository.SSHaddress' })}
          addonAfter={(
            <CopyToClipboard
              text={(currentApp && currentApp.sshRepositoryUrl) || noRepoUrl}
              onCopy={handleCopy}
            >
              <Icon type="content_copy" style={{ cursor: 'pointer' }} />
            </CopyToClipboard>
          )}
        />
        <TextField
          disabled
          defaultValue={(currentApp && currentApp.repoUrl) || noRepoUrl}
          label={formatMessage({ id: 'repository.HTTPSaddress' })}
          addonAfter={(
            <CopyToClipboard
              text={(currentApp && currentApp.repoUrl) || noRepoUrl}
              onCopy={handleCopy}
            >
              <Icon type="content_copy" style={{ cursor: 'pointer' }} />
            </CopyToClipboard>
          )}
        />
      </Form>
    </div>
  );

  function rendererAppServiceId({ value, text }) {
    const record = appServiceDs.find((appServiceRecord) => appServiceRecord.get('id') === value);
    try {
      return `${text}(${record.get('code')})`;
    } catch (e) {
      return text;
    }
  }

  function renderAppServiceOption({ value, text }) {
    const record = appServiceDs.find((appServiceRecord) => appServiceRecord.get('id') === value);
    if (record) {
      return (
        <Tooltip title={record.get('code')}>
          {rendererAppServiceId({ value, text })}
        </Tooltip>
      );
    }
    return '';
  }

  return (
    <div style={{ paddingLeft: 24, display: 'flex', alignItems: 'center' }}>
      <Form columns={2} style={{ maxWidth: '5.5rem' }}>
        <Select
          colSpan={1}
          className="c7ncd-cm-select"
          label={formatMessage({ id: 'c7ncd.deployment.app-service' })}
          dataSet={selectAppDs}
          notFoundContent={appServiceDs.length === 0 ? formatMessage({ id: 'ist.noApp' }) : '未找到应用服务'}
          searchable
          name="appServiceId"
          clearButton={false}
          disabled={appServiceDs.status !== 'ready' || appServiceDs.length === 0}
          optionRenderer={renderAppServiceOption}
          renderer={({ text }) => text}
        >
          {
          localStorage.getItem('recent-app') && (
          <OptGroup label={formatMessage({ id: 'deploy.app-recent' })} key="app-recent">
            {
              _.map(JSON.parse(localStorage.getItem('recent-app'))[projectId], ({ id, code, name: opName }, index) => (
                <Option
                  value={id}
                  key={index}
                >
                  {opName}
                </Option>
              ))
            }
          </OptGroup>
          )
        }

          <OptGroup label={formatMessage({ id: 'deploy.app' })} key="app">
            {
            _.map(appServiceDs.toData(), ({ id, code, name: opName }, index) => (
              <Option
                value={id}
                key={index}
              >
                {opName}
              </Option>
            ))
          }
          </OptGroup>

        </Select>
        <Dropdown
          trigger={['click', 'focus']}
          overlay={copyMenu}
          placement="bottomRight"
          colSpan={1}
        >
          <Button
            funcType="raised"
            disabled={!(currentApp && currentApp.repoUrl)}
            className="c7ncd-copyBtn"
          >
            <span className="c7ncd-copyBtn-span">
              {formatMessage({ id: 'repository.copyUrl' })}
              <Icon
                style={{ marginLeft: '.18rem' }}
                type="arrow_drop_down"
              />
            </span>
          </Button>
        </Dropdown>
      </Form>

    </div>
  );
})));
