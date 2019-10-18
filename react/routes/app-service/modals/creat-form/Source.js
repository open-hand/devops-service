import React, { useEffect } from 'react';
import { Form, Select, TextField } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';

import './index.less';
import Tips from '../../../../components/new-tips';

const { Option, OptGroup } = Select;

export default injectIntl(observer((props) => {
  const { record, appServiceStore, projectId, intl: { formatMessage }, intlPrefix, prefixCls } = props;

  useEffect(() => {
    if (record.get('appServiceSource')) {
      appServiceStore.loadAppService(projectId, record.get('appServiceSource'));
    } else {
      appServiceStore.setAppService([]);
    }
    record.set('templateAppServiceId', null);
  }, [record.get('appServiceSource')]);

  useEffect(() => {
    async function loadVersion() {
      if (record.get('templateAppServiceId')) {
        const res = await appServiceStore.loadVersion(projectId, record.get('templateAppServiceId'));
        res && res[0] && record.set('templateAppServiceVersionId', res[0].id);
      } else {
        appServiceStore.setVersion([]);
      }
    }
    loadVersion();
    record.set('templateAppServiceVersionId', null);
  }, [record.get('templateAppServiceId')]);

  return (
    <div className={`${prefixCls}-create-wrap-template`}>
      <div className={`${prefixCls}-create-wrap-template-title`}>
        <Tips
          helpText={formatMessage({ id: `${intlPrefix}.template.tips` })}
          title={formatMessage({ id: `${intlPrefix}.template` })}
        />
      </div>
      <Form record={record} columns={3}>
        <Select name="appServiceSource">
          <Option value="normal_service">{formatMessage({ id: `${intlPrefix}.source.project` })}</Option>
          <Option value="share_service">{formatMessage({ id: `${intlPrefix}.source.organization` })}</Option>
        </Select>
        <Select
          name="templateAppServiceId"
          colSpan={2}
          searchable
          disabled={!record.get('appServiceSource')}
          notFoundContent={<FormattedMessage id={`${intlPrefix}.empty`} />}
        >
          {record.get('appServiceSource') === 'normal_service' ? (
            map(appServiceStore.getAppService[0] && appServiceStore.getAppService[0].appServiceList, ({ id, name }) => (
              <Option value={id}>{name}</Option>
            ))
          ) : (
            map(appServiceStore.getAppService, ({ id: groupId, name: groupName, appServiceList }) => (
              <OptGroup label={groupName} key={groupId}>
                {map(appServiceList, ({ id, name }) => (
                  <Option value={id}>{name}</Option>
                ))}
              </OptGroup>
            ))
          )}
        </Select>
        <Select name="templateAppServiceVersionId" colSpan={3} searchable clearButton={false} disabled={!record.get('templateAppServiceId')}>
          {map(appServiceStore.getVersion, ({ id, version }) => (
            <Option value={id}>{version}</Option>
          ))}
        </Select>
      </Form>
    </div>
  );
}));
