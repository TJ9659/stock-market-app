import {
  TrendingUp,
  TrendingDown,
  Trash2,
  ChevronLeft,
  ChevronRight,
  Eye,
  Lightbulb,
} from "lucide-react";
import { useEffect, useState } from "react";
import api from "../services/api";
import { toast } from "sonner";
import { useNavigate } from "react-router";
import { InsightsDetailDrawer } from "../components/ai/InsightsDetailDrawer";
import GenericSearchbar from "../components/GenericSearchbar";

interface WatchlistEntry {
  id: number;
  symbol: string;
  currentPrice: number;
  change: number;
  percentChange: number;
  tracked: boolean;
}

export default function WatchlistPage() {
  const navigate = useNavigate();
  const [watchlist, setWatchlist] = useState<WatchlistEntry[]>([]);
  const [selectedSymbol, setSelectedSymbol] = useState("");
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const pageSize = 10;
  const [searchQuery, setSearchQuery] = useState("");
  const fetchWatchlist = async (page: number, searchQuery?: string) => {
    setLoading(true);
    try {
      const response = await api.get("/watchlist", {
        params: {
          search: searchQuery || "",
          page: page,
          size: pageSize,
        },
      });

      setWatchlist(response.data.content);
      setTotalPages(response.data.totalPages);
      setTotalElements(response.data.totalElements);
    } catch (error) {
      console.error("Failed to fetch watchlist", error);
      toast.error("Could not load watchlist");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const timer = setTimeout(() => {
      fetchWatchlist(currentPage);
    }, 300);
    return () => clearTimeout(timer);
  }, [currentPage]);

  const handleSearchTrigger = () => {
    if (currentPage !== 0) {
      setCurrentPage(0);
    } else {
      fetchWatchlist(0, searchQuery);
    }
  };

  const handleRemove = async (id: number, symbol: string) => {
    try {
      await api.delete(`/api/watchlist/${id}`);
      toast.info(`${symbol} removed from watchlist`);

      if (watchlist.length === 1 && currentPage > 0) {
        setCurrentPage((prev) => prev - 1);
      } else {
        fetchWatchlist(currentPage);
      }
    } catch (error) {
      toast.error("Failed to remove asset");
    }
  };

  const handleView = (symbol: string) => {
    navigate(`/stock/${symbol}`);
  };

  const handleAIInsights = (symbol: string) => {
    setSelectedSymbol(symbol);
    setIsDrawerOpen(true);
  };

  return (
    <div className="p-8 text-white">
      <header className="flex justify-between items-end mb-8">
        <div>
          <h1 className="text-4xl font-black tracking-tighter italic uppercase">
            Watchlist
          </h1>
          <p className="text-gray-500 font-medium mt-1">
            Real-time tracking for your favorite assets.
          </p>
        </div>
      </header>

      {!loading && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
          <div className="bg-[#111111] border border-gray-900 p-6 rounded-2xl">
            <p className="text-[10px] font-black text-gray-500 uppercase tracking-widest mb-1">
              Total Assets
            </p>
            <p className="text-3xl font-bold">{totalElements}</p>
          </div>
        </div>
      )}

      <div className="mb-5 w-full max-w-2xl">
        <GenericSearchbar
          value={searchQuery}
          onChange={setSearchQuery}
          onSearch={() => handleSearchTrigger()}
        />
      </div>

      <div className="bg-[#111111] border border-gray-900 rounded-3xl overflow-auto mb-6">
        <table className="w-full text-left border-collapse ">
          <thead className="bg-[#161616] border-b border-gray-900">
            <tr>
              <th className="px-6 py-5 text-[10px] font-black uppercase text-gray-500 tracking-widest">
                Asset
              </th>
              <th className="px-6 py-5 text-[10px] font-black uppercase text-gray-500 tracking-widest text-right">
                Price
              </th>
              <th className="px-6 py-5 text-[10px] font-black uppercase text-gray-500 tracking-widest text-right">
                24h Change
              </th>
              <th className="px-6 py-5 text-[10px] font-black uppercase text-gray-500 tracking-widest text-right">
                Insights
              </th>
              <th className="px-6 py-5 text-[10px] font-black uppercase text-gray-500 tracking-widest text-right">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-900">
            {loading ? (
              <tr>
                <td
                  colSpan={5}
                  className="p-10 text-center text-emerald-500 font-mono animate-pulse"
                >
                  Updating Market Data...
                </td>
              </tr>
            ) : watchlist.length === 0 ? (
              <tr>
                <td
                  colSpan={5}
                  className="p-10 text-center text-gray-500 font-medium"
                >
                  Your watchlist is empty.
                </td>
              </tr>
            ) : (
              watchlist.map((item) => {
                const isPositive = (item.percentChange ?? 0) >= 0;
                return (
                  <tr
                    key={item.id}
                    className="hover:bg-white/[0.02] transition-colors group"
                  >
                    <td className="px-6 py-5">
                      <span className="font-bold text-white text-lg tracking-tight">
                        {item.symbol}
                      </span>
                    </td>
                    <td className="px-6 py-5 text-right font-mono font-bold text-gray-200">
                      {item.currentPrice?.toLocaleString(undefined, {
                        minimumFractionDigits: 2,
                        maximumFractionDigits: 2,
                      })}
                      <span className="text-xs">USD</span>
                    </td>
                    <td
                      className={`px-6 py-5 text-right font-bold ${isPositive ? "text-emerald-400" : "text-red-400"}`}
                    >
                      <div className="flex items-center justify-end gap-1">
                        {isPositive ? (
                          <TrendingUp size={14} />
                        ) : (
                          <TrendingDown size={14} />
                        )}
                        {item.percentChange?.toFixed(2)}%
                      </div>
                    </td>
                    <td className="px-6 py-5">
                      <div className="flex justify-end">
                        <button
                          onClick={() => handleAIInsights(item.symbol)}
                          className="font-bold flex items-center gap-2 text-white bg-emerald-600 hover:bg-emerald-500 hover:cursor-pointer transition-colors rounded-lg text-xs px-3 py-2 tracking-tight"
                        >
                          <Lightbulb size={14} />
                          <span>AI Overview</span>
                        </button>
                      </div>
                    </td>
                    <td className="px-6 py-5 text-right">
                      <div className="flex items-center justify-end gap-1">
                        <button
                          onClick={() => handleView(item.symbol)}
                          className="p-2 text-gray-500 hover:text-emerald-400 transition-colors hover:cursor-pointer"
                          title="View details"
                        >
                          <Eye size={18} />
                        </button>
                        <button
                          onClick={() => handleRemove(item.id, item.symbol)}
                          className="p-2 text-gray-500 hover:text-red-400 transition-colors hover:cursor-pointer"
                          title="Remove from watchlist"
                        >
                          <Trash2 size={18} />
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      {totalPages > 1 && (
        <div className="flex items-center justify-between px-2">
          <p className="text-[10px] text-gray-500 font-black uppercase tracking-widest">
            Page {currentPage + 1} of {totalPages}
          </p>
          <div className="flex gap-2">
            <button
              onClick={() => setCurrentPage((prev) => prev - 1)}
              disabled={currentPage === 0}
              className="p-2 rounded-lg bg-[#111111] border border-gray-800 disabled:opacity-20 hover:bg-gray-800 transition-all hover:cursor-pointer"
            >
              <ChevronLeft size={20} />
            </button>
            <button
              onClick={() => setCurrentPage((prev) => prev + 1)}
              disabled={currentPage >= totalPages - 1}
              className="p-2 rounded-lg bg-[#111111] border border-gray-800 disabled:opacity-20 hover:bg-gray-800 transition-all hover:cursor-pointer"
            >
              <ChevronRight size={20} />
            </button>
          </div>
        </div>
      )}

      <InsightsDetailDrawer
        open={isDrawerOpen}
        onOpenChange={setIsDrawerOpen}
        symbol={selectedSymbol}
      />
    </div>
  );
}
