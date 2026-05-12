import { useEffect, useRef } from 'react';

interface UseTradingViewWidgetProps {
    scriptUrl: string;
    config: Record<string, unknown>;
}

const useTradingViewWidget = ({ scriptUrl, config }: UseTradingViewWidgetProps) => {
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!containerRef.current) return;

    containerRef.current.innerHTML = ""; 

    const script = document.createElement("script");
    script.src = `https://s3.tradingview.com/external-embedding/${scriptUrl}`;
    script.type = "text/javascript";
    script.async = true;
    script.innerHTML = JSON.stringify(config);

    containerRef.current.appendChild(script);
  }, [config, scriptUrl]);

  return containerRef;
}

export default useTradingViewWidget;