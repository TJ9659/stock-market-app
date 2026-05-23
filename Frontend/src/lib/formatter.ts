export const formatDescription = (str : string) => {
  if (!str) return "";
  return str.length > 15 ? str.substring(0, 20) + "..." : str;
};

export const getTradingViewSymbol = (symbol: string) => {
  if (!symbol) return "";
  const cleanSymbol = symbol.toUpperCase();

  if (cleanSymbol.length === 5 && cleanSymbol.endsWith('F')) {
    return `OTC:${cleanSymbol}`;
  }

  const exchange = cleanSymbol.length <= 3 ? "NYSE" : "NASDAQ";
  return `${exchange}:${cleanSymbol}`;
};

  // helper to format 1415993.0 -> $1.42T
  export const formatMarketCap = (value: number | undefined) => {
    if (!value) return "N/A";
    if (value >= 1000000) return `$${(value / 1000000).toFixed(2)}T`;
    if (value >= 1000) return `$${(value / 1000).toFixed(2)}B`;
    return `$${value.toFixed(2)}M`;
  };
