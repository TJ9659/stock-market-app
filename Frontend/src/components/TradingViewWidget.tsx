import React, { memo } from "react";
import useTradingViewWidget from "../hooks/useTradingViewWidget";

interface TradingViewWidgetProps {
  scriptUrl: string;
  config: Record<string, unknown>;
  height?: number;
  width?: string;
  className?: string;
  title?: string;
}

const TradingViewWidget = ({
  scriptUrl,
  height = 600,
  width="100%",
  className,
  config,
  title,
}: TradingViewWidgetProps) => {
  const containerRef = useTradingViewWidget({ scriptUrl, config });

  return (
    <div>
      <div
        ref={containerRef}
        style={{ height: `${height}px`, width: `${width}` }}
        className={`tradingview-widget-container w-full rounded-xl overflow-hidden border border-gray-800 shadow-2xl ${className}`}
      ></div>
    </div>
  );
};

export default memo(TradingViewWidget);
