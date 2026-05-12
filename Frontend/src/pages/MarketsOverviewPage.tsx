import TradingViewWidget from "../components/TradingViewWidget";
import { SYMBOL_INFO_WIDGET_CONFIG, TICKER_WIDGET_CONFIG } from "../lib/constants";

const MarketsOverview = () => {
  return (
    <div className="flex flex-col gap-8 p-6">
      <section className="w-full">
        <TradingViewWidget config={TICKER_WIDGET_CONFIG} scriptUrl="embed-widget-tickers.js" height={100} />
        {/* <TradingViewWidget config={SYMBOL_INFO_WIDGET_CONFIG("NVDA")} scriptUrl="embed-widget-symbol-info.js" height={400} /> */}
      </section>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* left */}
        <div className="lg:col-span-4 flex flex-col gap-8">

        </div>

        {/* right */}
        <div className="lg:col-span-8 flex flex-col gap-8">

        </div>
      </div>
    </div>
  );
};

export default MarketsOverview;