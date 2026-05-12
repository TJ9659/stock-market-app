// import axios from "axios";
import { useEffect, useState } from "react";
import { Link } from "react-router";
import TradingViewWidget from "../components/TradingViewWidget";
import {
  HEATMAP_WIDGET_CONFIG,
  MARKET_OVERVIEW_WIDGET_CONFIG,
  MARKET_QUOTES_WIDGET_CONFIG,
  TOP_STORIES_WIDGET_CONFIG,
} from "../lib/constants";
// import api from "../services/api";

const Home = () => {
  return (
    <div className="pb-10 px-6 max-w-[1600px] mx-auto mt-8">
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-10 items-start">
        {/* left column */}
        <div className="flex flex-col gap-10">
          <div className="flex flex-col gap-4">
            <h2 className="text-xl font-bold text-white tracking-tight">
              Market Overview
            </h2>
            <TradingViewWidget
              title="Market Overview"
              scriptUrl="embed-widget-market-overview.js"
              config={MARKET_OVERVIEW_WIDGET_CONFIG}
              height={500}
              width="100%"
            />
          </div>

          <div className="flex flex-col gap-4">
            <h2 className="text-xl font-bold text-white tracking-tight">
              Top Stories
            </h2>
            <TradingViewWidget
              scriptUrl="embed-widget-timeline.js"
              config={TOP_STORIES_WIDGET_CONFIG}
              width="100%"
              height={500}
            />
          </div>
        </div>

        {/* right column */}

        <div className="lg:col-span-2 flex flex-col gap-10">
          <div className="flex flex-col gap-4">
            <h2 className="text-xl font-bold text-white tracking-tight">
              Stock Heatmap
            </h2>
            <TradingViewWidget
              scriptUrl="embed-widget-stock-heatmap.js"
              config={HEATMAP_WIDGET_CONFIG}
              height={500}
              width="100%"
            />
          </div>

          <div className="flex flex-col gap-4">
            <h2 className="text-xl font-bold text-white tracking-tight">
              Market Quotes
            </h2>
            <TradingViewWidget
              scriptUrl="embed-widget-market-quotes.js"
              config={MARKET_QUOTES_WIDGET_CONFIG}
              height={500}
              width="100%"
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default Home;
