import React, { createContext, useContext, useMemo, useEffect } from 'react';
import axios from 'axios';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import CreateDataSet from './branchCreateDataSet';
import issueNameDataSet from './issueNameDataSet';
import DevPipelineStore from '../../../../stores/DevPipelineStore';
import { handlePromptError } from '../../../../../../utils';

const Store = createContext();

export function useFormStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const {
      AppState: { currentMenuType: { id: projectId } },
      children,
      intlPrefix,
    } = props;
    const formDs = useMemo(() => new DataSet(CreateDataSet({ ...props }), [projectId]));
    const issueNameOptionDs = useMemo(() => new DataSet(issueNameDataSet({ projectId }), [projectId]));
    function searchData() {

    }
    function loadBranchData() {
      axios.post(`/devops/v1/projects/${projectId}/app_service/${DevPipelineStore.selectedApp}/git/page_branch_by_options`)
        .then((data) => {
          if (handlePromptError(data)) {

          }
        });
    }
    console.log(props);
    function handleDataSetChange({ record, name, value, oldValue }) {
      console.log('[dataset newValue]', value, '[oldValue]', oldValue, `[record.get('${name}')]`, record.get(name));
    }
    // 分支类型DataSet
    const branchTypeDS = new DataSet({
      fields: [
        { name: 'branchType', type: 'string', required: true, textField: 'text', label: '分支类型' },
      ],
      events: {
        update: handleDataSetChange,
      },
    });
    // 问题名称DataSet
    const issueNameDs = new DataSet({
      fields: [
        {
          name: 'issueName',
          type: 'string',
          textField: 'text',
          label: '问题名称',
          options: issueNameOptionDs,
        },
      ],
    });

    const value = {
      ...props,
      branchTypeDS,
      issueNameDs,
      formDs,
      intlPrefix,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  })
));
