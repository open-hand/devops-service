import { Modal } from 'choerodon-ui/pro';

export default function openWarnModal(refresh, formatMessage) {
  Modal.open({
    movable: false,
    closable: false,
    key: Modal.key(),
    title: formatMessage({ id: 'data.lost' }),
    children: formatMessage({ id: 'data.lost.warn' }),
    okCancel: false,
    onOk: refresh,
  });
}
