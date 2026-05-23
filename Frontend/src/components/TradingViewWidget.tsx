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
  width = "100%",
  className,
  config,
}: TradingViewWidgetProps) => {
  const containerRef = useTradingViewWidget({ scriptUrl, config });

  return (
    <div 
      style={{ height: `${height}px`, width: width }} 
      className={`relative overflow-hidden ${className}`}
    >
      <div
        ref={containerRef}
        style={{ height: "100%", width: "100%" }}
        className="tradingview-widget-container"
      >
        <div className="tradingview-widget-container__widget" style={{ height: "100%", width: "100%" }}></div>
      </div>
    </div>
  );
};

export default memo(TradingViewWidget);
