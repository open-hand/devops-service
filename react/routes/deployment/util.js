export function getPodsInfo(record) {
  const name = record.get('code');
  const podRunningCount = record.get('podRunningCount');
  const podCount = record.get('podCount');
  const podUnlinkCount = podCount - podRunningCount;

  return {
    name,
    podCount,
    podRunningCount,
    podUnlinkCount,
  };
}

export function getEnvInfo(record) {
  const name = record.get('name');
  const connect = record.get('connect');
  const synchronize = record.get('synchronize');

  return {
    name,
    connect,
    synchronize,
  };
}
