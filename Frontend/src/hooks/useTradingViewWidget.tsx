import { useEffect, useRef } from 'react';

interface UseTradingViewWidgetProps {
    scriptUrl: string;
    config: Record<string, unknown>;
}

const useTradingViewWidget = ({ scriptUrl, config }: UseTradingViewWidgetProps) => {
  const containerRef = useRef<HTMLDivElement>(null);

  // Compare configs by value, not object identity: callers often build the config inline
  // (e.g. CONFIG(symbol)), which creates a new object every render and would otherwise
  // tear down and re-inject the TradingView script on every state change.
  const configKey = JSON.stringify(config);

  useEffect(() => {
    const container = containerRef.current;
    if (!container) return;

    container.innerHTML = "";

    const script = document.createElement("script");
    script.src = `https://s3.tradingview.com/external-embedding/${scriptUrl}`;
    script.type = "text/javascript";
    script.async = true;
    script.innerHTML = configKey;

    container.appendChild(script);

    return () => {
      container.innerHTML = "";
    };
  }, [configKey, scriptUrl]);

  return containerRef;
}

export default useTradingViewWidget;
