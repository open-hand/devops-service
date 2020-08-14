import React, { useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Choerodon } from '@choerodon/boot';
import { handlePromptError } from '../../../../../../utils';
import HeaderButtons from '../../../../../../components/header-buttons';
import DetailsModal from './details';
import ValueModalContent from './values/Config';
import UpgradeModalContent from './upgrade';
import { useResourceStore } from '../../../../stores';
import { useInstanceStore } from '../stores';

const detailKey = Modal.key();
const valuesKey = Modal.key();
const upgradeKey = Modal.key();
const redeployKey = Modal.key();

const IstModals = injectIntl(observer(() => {
  const {
    prefixCls,
    intlPrefix,
    intl: { formatMessage },
    resourceStore: {
      getSelectedMenu: {
        id,
        parentId,
      },
    },
    treeDs,
  } = useResourceStore();
  const {
    baseDs,
    casesDs,
    podsDs,
    istStore,
    detailsStore,
    tabs: {
      CASES_TAB,
      DETAILS_TAB,
      PODS_TAB,
    },
    checkIstExist,
    AppState: { currentMenuType: { id: projectId } },
  } = useInstanceStore();
  const modalStyle = useMemo(() => ({
    width: 'calc(100vw - 3.52rem)',
  }), []);

  function openValueModal() {
    const record = baseDs.current;
    if (!record) return false;

    const appServiceVersionId = record.get('commandVersionId');
    const appServiceId = record.get('appServiceId');
    istStore.loadValue(projectId, id, appServiceVersionId);

    const deployVo = {
      id,
      parentId,
      projectId,
      appServiceVersionId,
      appServiceId,
    };
    Modal.open({
      key: valuesKey,
      title: formatMessage({ id: `${intlPrefix}.modal.values` }),
      drawer: true,
      okText: formatMessage({ id: 'deployment' }),
      style: modalStyle,
      children: <ValueModalContent
        store={istStore}
        vo={deployVo}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        formatMessage={formatMessage}
        refresh={afterDeploy}
      />,
      afterClose: () => {
        istStore.setUpgradeValue({});
      },
    });
  }

  function openUpgradeModal() {
    const record = baseDs.current;
    if (!record) return false;

    const appServiceVersionId = record.get('appServiceVersionId');
    const appServiceId = record.get('appServiceId');
    const deployVo = {
      id,
      parentId,
      versionId: appServiceVersionId,
      appServiceId,
    };

    Modal.open({
      key: upgradeKey,
      title: formatMessage({ id: `${intlPrefix}.modal.modify` }),
      drawer: true,
      okText: formatMessage({ id: 'modify' }),
      style: modalStyle,
      children: <UpgradeModalContent
        store={istStore}
        vo={deployVo}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        refresh={afterDeploy}
      />,
      afterClose: () => {
        istStore.setUpgradeValue({});
      },
    });
  }

  function openDetailModal() {
    Modal.open({
      key: detailKey,
      title: formatMessage({ id: `${intlPrefix}.modal.detail` }),
      drawer: true,
      okCancel: false,
      okText: formatMessage({ id: 'close' }),
      style: { width: 380 },
      children: <DetailsModal
        record={baseDs.current}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        formatMessage={formatMessage}
      />,
    });
  }

  function getDs(key) {
    const dsMapping = {
      [CASES_TAB]: casesDs,
      [PODS_TAB]: podsDs,
    };
    return dsMapping[key];
  }

  function refresh() {
    checkIstExist().then((query) => {
      if (query) {
        treeDs.query();
        baseDs.query();
        const activeKey = istStore.getTabKey;
        if (activeKey === DETAILS_TAB) {
          detailsStore.loadResource(projectId, id);
        } else {
          const ds = getDs(activeKey);
          ds && ds.query();
        }
      }
    });
  }

  function openRedeploy() {
    const record = baseDs.current;
    if (!record) return;

    const versionName = record.get('commandVersion');

    Modal.open({
      key: redeployKey,
      title: formatMessage({ id: `${intlPrefix}.modal.redeploy` }),
      children: <FormattedMessage id={`${intlPrefix}.modal.redeploy.tips`} values={{ versionName }} />,
      onOk: redeploy,
    });
  }

  async function redeploy() {
    try {
      const result = await istStore.redeploy(projectId, id);
      if (handlePromptError(result, false)) {
        afterDeploy();
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
    }
  }

  function afterDeploy() {
    detailsStore.setTargetCount({});
    refresh();
  }

  function getHeader() {
    const [envId] = parentId.split('**');
    const envRecord = treeDs.find((record) => record.get('key') === envId);
    const connect = envRecord && envRecord.get('connect');
    const record = baseDs.current;
    const status = record ? record.get('status') : '';
    const btnDisabled = !connect || !status || (status !== 'failed' && status !== 'running');

    const buttons = [{
      name: formatMessage({ id: `${intlPrefix}.modal.values` }),
      icon: 'rate_review1',
      handler: openValueModal,
      display: true,
      permissions: ['choerodon.code.project.deploy.app-deployment.resource.ps.values'],
      group: 1,
      disabled: btnDisabled,
    }, {
      name: formatMessage({ id: `${intlPrefix}.modal.modify` }),
      icon: 'backup_line',
      handler: openUpgradeModal,
      permissions: ['choerodon.code.project.deploy.app-deployment.resource.ps.example'],
      display: true,
      group: 1,
      disabled: btnDisabled,
    }, {
      name: formatMessage({ id: `${intlPrefix}.modal.redeploy` }),
      icon: 'redeploy_line',
      handler: openRedeploy,
      permissions: ['choerodon.code.project.deploy.app-deployment.resource.ps.redeploy'],
      display: true,
      group: 1,
      disabled: btnDisabled,
    }, {
      name: formatMessage({ id: 'refresh' }),
      icon: 'refresh',
      handler: refresh,
      display: true,
      group: 2,
    }];

    return <HeaderButtons items={buttons} />;
  }

  return getHeader();
}));

export default IstModals;
