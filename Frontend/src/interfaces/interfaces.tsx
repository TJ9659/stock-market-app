export interface User {
  firstName: string;
  lastName: string;
  email: string;
  role: string;
}

export interface CompanyProfile {
  name: string;
  ticker: string;
  logo: string;
  phone: string;
  industry: string;
  currency: string;
  marketCapitalization: number;
  ipo: string;
  webUrl: string;
}

export interface StockQuote {
  currentPrice: number;
  change: number;
  percentChange: number;
  high: number;
  low: number;
  open: number;
  previousClose: number;
  timestamp: number;
}

export interface MarketInsight {
  symbol: string;
  sentiment: "BULLISH" | "BEARISH" | "NEUTRAL";
  catalyst: string;
  confidence: number;
  keyPoints: string[];
}