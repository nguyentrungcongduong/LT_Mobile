// src/lib/broadcastAuth.ts
// Multi-tab auth sync via BroadcastChannel API
// When admin logs out in tab A → tabs B, C also clear session automatically

const CHANNEL_NAME = 'gymadmin_auth';

type AuthMessage = { type: 'LOGOUT' } | { type: 'LOGIN' };

let channel: BroadcastChannel | null = null;

const getChannel = (): BroadcastChannel => {
  if (!channel) {
    channel = new BroadcastChannel(CHANNEL_NAME);
  }
  return channel;
};

/** Phát tín hiệu LOGOUT sang tất cả tab khác */
export const broadcastLogout = (): void => {
  try {
    getChannel().postMessage({ type: 'LOGOUT' } satisfies AuthMessage);
  } catch {
    // BroadcastChannel không hỗ trợ (Safari cũ) → bỏ qua
  }
};

/** Phát tín hiệu LOGIN sang tất cả tab khác */
export const broadcastLogin = (): void => {
  try {
    getChannel().postMessage({ type: 'LOGIN' } satisfies AuthMessage);
  } catch {
    // Ignore
  }
};

/** Lắng nghe events từ tab khác */
export const listenAuthBroadcast = (
  onLogout: () => void,
  onLogin?: () => void
): (() => void) => {
  const ch = getChannel();

  const handler = (e: MessageEvent<AuthMessage>) => {
    if (e.data?.type === 'LOGOUT') onLogout();
    if (e.data?.type === 'LOGIN') onLogin?.();
  };

  ch.addEventListener('message', handler);

  // Return cleanup function
  return () => ch.removeEventListener('message', handler);
};
