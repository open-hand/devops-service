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
    const appServiceId = record.get('templateAppServiceId');
    record.set('templateAppServiceVersionId', null);
    record.getField('templateAppServiceVersionId').reset();
    if (appServiceId) {
      const field = record.getField('templateAppServiceVersionId');
      field.set('lookupAxiosConfig', {
        url: `/devops/v1/projects/${projectId}/app_service_versions/page_by_options?app_service_id=${appServiceId}&deploy_only=false&do_page=true&page=1&size=40`,
        method: 'post',
      });
      fetchLookup(field);
    }
  }, [record.get('templateAppServiceId')]);

  async function fetchLookup(field) {
    const data = await field.fetchLookup();
    if (data && data.length) {
      record.set('templateAppServiceVersionId', data[0].id);
    }
  }

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
        <Select
          name="templateAppServiceVersionId"
          colSpan={3}
          searchable
          searchMatcher="version"
          clearButton={false}
          disabled={!record.get('templateAppServiceId')}
        />
      </Form>
    </div>
  );
}));
