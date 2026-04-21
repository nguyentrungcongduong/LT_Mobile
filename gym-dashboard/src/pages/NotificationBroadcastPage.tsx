import { useState } from 'react';
import { notificationService, type BroadcastTarget } from '@/features/notifications/services/notificationService';

export default function NotificationBroadcastPage() {
  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [targetGroup, setTargetGroup] = useState<BroadcastTarget>('ALL');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    if (!title || !body) {
      alert('Vui lòng nhập đầy đủ thông tin');
      return;
    }

    try {
      setLoading(true);

      await notificationService.broadcast({
        title,
        body,
        target_group: targetGroup,
      });

      alert('Gửi broadcast thành công 🚀');

      setTitle('');
      setBody('');
    } catch (err) {
      console.error(err);
      alert('Gửi thất bại ❌');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-6 max-w-xl">
      <h2 className="text-xl font-bold mb-4">📢 Broadcast Notification</h2>

      <div className="mb-3">
        <label>Title</label>
        <input
          className="w-full border p-2 rounded"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
        />
      </div>

      <div className="mb-3">
        <label>Body</label>
        <textarea
          className="w-full border p-2 rounded"
          rows={4}
          value={body}
          onChange={(e) => setBody(e.target.value)}
        />
      </div>

      <div className="mb-4">
        <label>Target Group</label>
        <select
          className="w-full border p-2 rounded"
          value={targetGroup}
          onChange={(e) => setTargetGroup(e.target.value as BroadcastTarget)}
        >
          <option value="ALL">All Users</option>
          <option value="ACTIVE_MEMBERS">Active Members</option>
          <option value="ALL_PT">All PT</option>
        </select>
      </div>

      <button
        onClick={handleSubmit}
        disabled={loading}
        className="bg-blue-500 text-white px-4 py-2 rounded"
      >
        {loading ? 'Sending...' : 'Send Broadcast'}
      </button>
    </div>
  );
}