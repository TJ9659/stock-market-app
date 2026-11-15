export const formatDescription = (str : string) => {
  if (!str) return "";
  return str.length > 15 ? str.substring(0, 20) + "..." : str;
};

// Maps Finnhub's exchange names (e.g. "NASDAQ NMS - GLOBAL MARKET",
// "NEW YORK STOCK EXCHANGE, INC.") to TradingView exchange prefixes.
const toTradingViewExchange = (exchange?: string): string | null => {
  if (!exchange) return null;
  const name = exchange.toUpperCase();

  if (name.includes("NASDAQ")) return "NASDAQ";
  if (name.includes("ARCA")) return "AMEX"; // TradingView lists NYSE Arca under AMEX
  if (name.includes("AMERICAN") || name.includes("NYSE MKT")) return "AMEX";
  if (name.includes("NEW YORK STOCK EXCHANGE") || name.startsWith("NYSE")) return "NYSE";
  if (name.includes("OTC")) return "OTC";
  return null;
};

export const getTradingViewSymbol = (symbol: string, exchange?: string) => {
  if (!symbol) return "";
  const cleanSymbol = symbol.toUpperCase();

  const tvExchange = toTradingViewExchange(exchange);
  if (tvExchange) return `${tvExchange}:${cleanSymbol}`;

  // Foreign ordinaries traded over the counter (e.g. NSRGF) are 5 letters ending in F.
  if (cleanSymbol.length === 5 && cleanSymbol.endsWith("F")) {
    return `OTC:${cleanSymbol}`;
  }

  // Unknown exchange (ETFs, ADRs whose profile reports a foreign home exchange, ...):
  // a bare ticker lets TradingView resolve the primary US listing itself.
  return cleanSymbol;
};

  // helper to format 1415993.0 -> $1.42T
  export const formatMarketCap = (value: number | undefined) => {
    if (!value) return "N/A";
    if (value >= 1000000) return `$${(value / 1000000).toFixed(2)}T`;
    if (value >= 1000) return `$${(value / 1000).toFixed(2)}B`;
    return `$${value.toFixed(2)}M`;
  };
