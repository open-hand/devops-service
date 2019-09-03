export function isNotRunning({ active, synchro, connect }) {
  return !active || !synchro || !connect;
}
