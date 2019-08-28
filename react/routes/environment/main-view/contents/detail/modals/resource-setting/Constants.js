const EVENT = ['instance', 'ingress', 'service', 'certificate', 'configMap', 'secret'];
const METHOD_OPTIONS = ['sms', 'email', 'pm'];
const TARGET_OPTIONS = ['handler', 'owner', 'specifier'];

const TARGET_SPECIFIER = 'specifier';

const SORTER_MAP = {
  ascend: 'asc',
  descend: 'desc',
};

const HEIGHT = window.innerHeight
  || document.documentElement.clientHeight
  || document.body.clientHeight;


export {
  EVENT,
  METHOD_OPTIONS,
  TARGET_OPTIONS,
  TARGET_SPECIFIER,
  SORTER_MAP,
  HEIGHT,
};
