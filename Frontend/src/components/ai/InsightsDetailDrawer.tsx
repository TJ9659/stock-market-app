import { useEffect, useState } from "react";
import { Button } from "../ui/button";
import {
  Drawer,
  DrawerClose,
  DrawerContent,
  DrawerDescription,
  DrawerFooter,
  DrawerHeader,
  DrawerTitle,
} from "../ui/drawer";
import api from "../../services/api";
import type { MarketInsight } from "../../interfaces/interfaces";

interface InsightsDetailDrawerProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  symbol: string | null;
}

export function InsightsDetailDrawer({
  open,
  onOpenChange,
  symbol,
}: InsightsDetailDrawerProps) {
  const [insight, setInsight] = useState<MarketInsight | null>(null);
  const [loading, setLoading] = useState(false);

  const fetchInsight = async () => {
    setLoading(true);
    setInsight(null);

    try {
      const response = await api.get(`/insight/${symbol}`);

      if (!response.data) {
        throw new Error(`Server error: ${response.status}`);
      }
      setInsight(response.data);
    } catch (error) {
      console.error("Failed to fetch market insights:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (open && symbol) {
      fetchInsight();
    }
  }, [open, symbol]);

  return (
    <Drawer open={open} onOpenChange={onOpenChange}>
      <DrawerContent className="bg-[#0f0f0f] border-gray-800 text-white">
        <div className="mx-auto w-full max-w-sm">
          <DrawerHeader>
            <DrawerTitle className="text-2xl text-white uppercase tracking-tighter">
              {symbol} AI Analysis
            </DrawerTitle>
            <DrawerDescription className="text-gray-400">
              Generated via Latest Gemini Model
            </DrawerDescription>
            <DrawerDescription className="text-gray-400 text-xs">
              (Insights may be inaccurate, please use with caution.)
            </DrawerDescription>
          </DrawerHeader>

          <div className="p-6 flex flex-col items-center">
            {loading ? (
              <div className="animate-pulse text-gray-500">
                Analyzing market data...
              </div>
            ) : insight ? (
              <>
                <div
                  className={`text-4xl font-black tracking-tighter ${
                    insight.sentiment === "BULLISH"
                      ? "text-emerald-400"
                      : insight.sentiment === "BEARISH"
                        ? "text-red-400"
                        : "text-amber-400"
                  }`}
                >
                  {insight.sentiment}
                </div>

                <div className="mt-2 text-xs font-mono text-gray-500 uppercase">
                  Confidence: {(insight.confidence * 100).toFixed(0)}%
                </div>

                <p className="text-sm text-gray-300 mt-4 text-center leading-relaxed">
                  <span className="text-white font-bold">Catalyst:</span>{" "}
                  {insight.catalyst}
                </p>

                <ul className="mt-6 w-full space-y-2">
                  {insight.keyPoints.map((point, i) => (
                    <li key={i} className="text-xs text-gray-400 flex gap-2">
                      <span className="text-emerald-500">•</span> {point}
                    </li>
                  ))}
                </ul>
              </>
            ) : (
              <div className="text-red-400">Unable to load insights.</div>
            )}
          </div>

          <DrawerFooter className="mb-4">
            <DrawerClose asChild>
              <Button className="border-gray-800 hover:bg-gray-900 bg-gray-800 text-white hover:cursor-pointer transition-colors">
                Dismiss
              </Button>
            </DrawerClose>
          </DrawerFooter>
        </div>
      </DrawerContent>
    </Drawer>
  );
}
