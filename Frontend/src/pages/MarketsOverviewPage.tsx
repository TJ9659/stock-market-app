import TradingViewWidget from "../components/TradingViewWidget";
import {
  HEATMAP_WIDGET_CONFIG,
  SYMBOL_INFO_WIDGET_CONFIG,
  TICKER_WIDGET_CONFIG,
} from "../lib/constants";

const MarketsOverview = () => {
  return (
    <div className="flex flex-col gap-8 p-6">
      <section className="w-full">
        <TradingViewWidget
          config={TICKER_WIDGET_CONFIG}
          scriptUrl="embed-widget-tickers.js"
          height={100}
        />
      </section>

      <section className="w-full">
        <TradingViewWidget
          config={HEATMAP_WIDGET_CONFIG}
          scriptUrl="embed-widget-stock-heatmap.js"
          height={500}
          width="100%"
        />
      </section>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        <div className="lg:col-span-4 flex flex-col gap-8"></div>

        <div className="lg:col-span-8 flex flex-col gap-8"></div>
      </div>
    </div>
  );
};

export default MarketsOverview;
