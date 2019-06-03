const SORTER_MAP = {
  ascend: 'asc',
  descend: 'desc',
};

function getWindowHeight() {
  return window.innerHeight ||
    document.documentElement.clientHeight ||
    document.body.clientHeight;
}

const HEIGHT = window.innerHeight ||
  document.documentElement.clientHeight ||
  document.body.clientHeight;

export {
  SORTER_MAP,
  HEIGHT,
  getWindowHeight,
};
