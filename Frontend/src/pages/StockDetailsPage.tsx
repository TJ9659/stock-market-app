import { useParams, useNavigate } from "react-router";
import { useEffect, useState } from "react";
import TradingViewWidget from "../components/TradingViewWidget";
import {
  ADVANCED_REAL_TIME_CHART_WIDGET_CONFIG,
  FUNDAMENTAL_DATA_WIDGET_CONFIG,
  TECHNICAL_ANALYSIS_WIDGET_CONFIG,
} from "../lib/constants";
import { formatMarketCap, getTradingViewSymbol } from "../lib/formatter";
import type { CompanyProfile, StockQuote } from "../interfaces/interfaces";
import api from "../services/api";
import { Phone, Globe, Calendar, Briefcase, DollarSign } from "lucide-react";
import { toast } from "sonner";
import { useAuth } from "../context/AuthContext";
import StatRow from "../components/StatRow";

const StockDetailPage = () => {
  const { symbol } = useParams<{ symbol: string }>();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const [loading, setLoading] = useState(true);
  const [watchlistId, setWatchlistId] = useState(null);
  const [isAdded, setIsAdded] = useState(false);
  const [companyProfile, setCompanyProfile] = useState<CompanyProfile | null>(
    null,
  );
  const [quote, setQuote] = useState<StockQuote | null>(null);

  const checkStatus = async () => {
    try {
      const response = await api.get(`/watchlist/check?symbol=${symbol}`);

      if (response.data.tracked) {
        setIsAdded(true);
        setWatchlistId(response.data.id);
      } else {
        setIsAdded(false);
        setWatchlistId(null);
      }
    } catch (error) {
      console.error("Error checking watchlist status", error);
    }
  };

  const fetchData = async () => {
    if (!symbol) return;
    setLoading(true);
    try {
      const [profileRes, quoteRes] = await Promise.all([
        api.get(`/stocks/profile?symbol=${symbol}`),
        api.get(`/stocks/quote?symbol=${symbol}`),
      ]);
      setCompanyProfile(profileRes.data);
      setQuote(quoteRes.data);
    } catch (error) {
      console.error("Failed to fetch stock data", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
    if (isAuthenticated) {
      checkStatus();
    } else {
      setIsAdded(false);
      setWatchlistId(null);
    }
  }, [symbol, isAuthenticated]);

  const handleAddToWatchlist = async () => {
    if (!isAuthenticated) {
      navigate("/login");
      toast.info("Please login to our system to use this feature.")
      return;
    }

    if (!symbol) return;

    try {
      if (!isAdded) {
        const response = await api.post("/watchlist/add", {
          symbol: symbol,
          name: companyProfile?.name,
        });
        setWatchlistId(response.data.id);
        setIsAdded(true);
        toast.success(`${symbol} added to watchlist!`);
      } else {
        await api.delete(`/watchlist/${watchlistId}`);
        setWatchlistId(null);
        setIsAdded(false);
        toast.info(`${symbol} removed from watchlist`);
      }
    } catch (error) {
      console.error("Watchlist action failed", error);
      toast.error("Something went wrong");
    }
  };

  if (loading)
    return (
      <div className="min-h-screen flex items-center justify-center text-emerald-500 font-mono animate-pulse">
        Initializing Market Data...
      </div>
    );
  if (!companyProfile || !symbol)
    return (
      <div className="p-10 text-center text-gray-400">
        Stock symbol not found.
      </div>
    );

  const isPositive = (quote?.percentChange ?? 0) >= 0;
  const priceColor = isPositive ? "text-emerald-500" : "text-red-500";

  return (
    <div className="max-w-7xl mx-auto p-4 space-y-6">
      <header className="flex flex-col md:flex-row justify-between items-start md:items-end pb-6 border-b border-gray-800 gap-4">
        <div className="flex items-center gap-5">
          {companyProfile.logo && (
            <div className="bg-white p-2 rounded-2xl shadow-lg">
              <img
                src={companyProfile.logo}
                alt={companyProfile.name}
                className="w-14 h-14 object-contain"
              />
            </div>
          )}
          <div>
            <div className="flex items-center gap-3">
              <h1 className="text-5xl font-black uppercase tracking-tighter text-white">
                {companyProfile.ticker}
              </h1>
              <div className="flex flex-col">
                <span className="text-[10px] uppercase font-bold text-gray-500 tracking-widest">
                  Industry
                </span>
                <span className="text-emerald-500 text-xs font-bold">
                  {companyProfile.industry}
                </span>
              </div>
            </div>
            <p className="text-gray-400 text-lg font-medium">
              {companyProfile.name}
            </p>
          </div>
        </div>

        <div className="text-right">
          <div className="text-4xl font-mono font-bold text-white">
            {quote?.currentPrice?.toLocaleString(undefined, {
              minimumFractionDigits: 2,
              maximumFractionDigits: 2,
            })}
            <span className="text-sm">USD</span>
          </div>
          <div
            className={`text-sm font-bold flex items-center justify-end gap-1 ${priceColor}`}
          >
            {isPositive ? "▲" : "▼"}
            {quote?.change?.toFixed(2)} ({quote?.percentChange?.toFixed(2)}%)
            <span className="text-gray-500 text-[10px] ml-1 uppercase">
              Today
            </span>
          </div>
        </div>
      </header>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        <div className="lg:col-span-3 rounded-2xl overflow-hidden border border-gray-800 bg-[#161616] p-2 gap-2">
          <div className="flex gap-2 flex-col">
            <TradingViewWidget
              config={ADVANCED_REAL_TIME_CHART_WIDGET_CONFIG(
                getTradingViewSymbol(symbol),
              )}
              scriptUrl="embed-widget-advanced-chart.js"
              height={550}
              width="100%"
            />
            <div className="flex flex-col md:flex-row gap-1">
              <TradingViewWidget
                scriptUrl="embed-widget-technical-analysis.js"
                config={TECHNICAL_ANALYSIS_WIDGET_CONFIG(
                  getTradingViewSymbol(symbol),
                )}
                width="100%"
                height={400}
              />
              <TradingViewWidget
                scriptUrl="embed-widget-financials.js"
                config={FUNDAMENTAL_DATA_WIDGET_CONFIG(
                  getTradingViewSymbol(symbol),
                )}
                width="100%"
                height={400}
              />
            </div>
          </div>
        </div>

        <div className="lg:col-span-1 space-y-6">
          <button
            onClick={handleAddToWatchlist}
            className={`
    ${
      isAdded
        ? "bg-red-500 text-white hover:bg-red-400 shadow-red-500/10"
        : "bg-emerald-500 text-black hover:bg-emerald-400 shadow-emerald-500/10"
    } 
    font-black hover:-translate-y-0.5 active:translate-y-0 rounded-xl py-4 px-4 transition-all w-full shadow-xl uppercase tracking-wider hover:cursor-pointer
  `}
          >
            {isAdded ? "Remove from Watchlist" : "Add to Watchlist"}
          </button>

          <div className="p-6 rounded-2xl border border-gray-800 bg-[#1f1f1f] shadow-2xl">
            <h3 className="text-xs font-black uppercase text-gray-500 mb-6 tracking-widest">
              Market Fundamental
            </h3>
            <div className="space-y-5">
              <StatRow
                icon={<DollarSign size={14} />}
                label="Market Cap"
                value={formatMarketCap(companyProfile.marketCapitalization)}
              />
              <StatRow
                icon={<Briefcase size={14} />}
                label="Industry"
                value={companyProfile.industry}
              />
              <StatRow
                icon={<Calendar size={14} />}
                label="IPO Date"
                value={companyProfile.ipo}
              />
              <StatRow
                icon={<Globe size={14} />}
                label="Website"
                value={
                  <a
                    href={companyProfile.webUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="text-emerald-500 hover:text-emerald-400 font-bold transition-colors"
                  >
                    Official Site
                  </a>
                }
              />
              {companyProfile.phone && (
                <StatRow
                  icon={<Phone size={14} />}
                  label="Contact"
                  value={`+${companyProfile.phone}`}
                />
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};



export default StockDetailPage;
