export function isNotRunning({ active, synchro }) {
  return !active || !synchro;
}
