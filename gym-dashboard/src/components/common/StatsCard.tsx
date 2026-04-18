// src/components/common/StatsCard.tsx
import React from 'react';
import { Card, Skeleton, Statistic } from 'antd';
import { ArrowUpOutlined, ArrowDownOutlined } from '@ant-design/icons';

interface StatsCardProps {
  title: string;
  value: number | string;
  prefix?: React.ReactNode;
  suffix?: string;
  /** % thay đổi so với kỳ trước (dương = tăng, âm = giảm) */
  trend?: number;
  trendLabel?: string;
  loading?: boolean;
  color?: 'blue' | 'green' | 'orange' | 'purple';
}

const COLOR_MAP: Record<
  NonNullable<StatsCardProps['color']>,
  { bg: string; text: string }
> = {
  blue:   { bg: '#eff6ff', text: '#1677ff' },
  green:  { bg: '#f0fdf4', text: '#16a34a' },
  orange: { bg: '#fff7ed', text: '#ea580c' },
  purple: { bg: '#faf5ff', text: '#7c3aed' },
};

const StatsCard: React.FC<StatsCardProps> = ({
  title,
  value,
  prefix,
  suffix,
  trend,
  trendLabel = 'so với tháng trước',
  loading = false,
  color = 'blue',
}) => {
  const { bg, text } = COLOR_MAP[color];
  const isUp = (trend ?? 0) >= 0;

  return (
    <Card
      className="rounded-xl shadow-sm hover:shadow-md transition-all duration-200"
      styles={{ body: { padding: '20px' } }}
    >
      <Skeleton loading={loading} active paragraph={{ rows: 2 }}>
        <div className="flex items-start justify-between gap-3">
          {/* Left: text data */}
          <div className="min-w-0">
            <p className="text-sm text-gray-500 mb-1 truncate">{title}</p>
            <Statistic
              value={value}
              suffix={suffix}
              valueStyle={{ fontSize: '1.65rem', fontWeight: 700, color: '#111827', lineHeight: 1.2 }}
            />
            {trend !== undefined && (
              <div className="flex items-center gap-1 mt-2 flex-wrap">
                {isUp ? (
                  <ArrowUpOutlined className="text-green-500 text-xs" />
                ) : (
                  <ArrowDownOutlined className="text-red-500 text-xs" />
                )}
                <span
                  className="text-xs font-semibold"
                  style={{ color: isUp ? '#16a34a' : '#dc2626' }}
                >
                  {Math.abs(trend)}%
                </span>
                <span className="text-xs text-gray-400">{trendLabel}</span>
              </div>
            )}
          </div>

          {/* Right: icon badge */}
          <div
            className="w-12 h-12 rounded-xl flex items-center justify-center text-xl flex-shrink-0"
            style={{ background: bg, color: text }}
          >
            {prefix}
          </div>
        </div>
      </Skeleton>
    </Card>
  );
};

export default StatsCard;
