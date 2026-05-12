import { useEffect, useState } from "react";
import {
  Combobox,
  ComboboxContent,
  ComboboxEmpty,
  ComboboxInput,
  ComboboxItem,
  ComboboxList,
} from "./ui/combobox";
import api from "../services/api";

export function StockSearch() {
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [query, setQuery] = useState("");

  const handleSearch = async (query: string) => {
    try {
      if (query.length < 2) return;
      setLoading(true);
      const res = await api.get(`/stocks/search?query=${query}`);
      setResults(res.data);
    } catch (error: any) {
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const timer = setTimeout(() => {
      handleSearch(query);
    }, 300); // debounce used to limit rates
    return () => clearTimeout(timer);
  }, [query]);

  return (
    <Combobox items={results}>
      <ComboboxInput
        className="bg-[#0B0E11] text-white border border-gray-800 rounded-full px-4 py-2 w-full"
        placeholder="Search symbols or companies..."
        onChange={(e) => setQuery(e.target.value)}
      />
      <ComboboxContent className="bg-[#0B0E11]">
        {loading && (
          <div className="p-2 text-sm text-gray-400">Searching...</div>
        )}
        {query.length > 1 && !loading && (
          <ComboboxEmpty>No results found.</ComboboxEmpty>
        )}
        {query.length > 0 && (
          <ComboboxList className="bg-[#0B0E11] max-h-96 overflow-y-scroll scrollbar-hide border border-gray-800">
            {(stock) => (
              <ComboboxItem
                key={stock.symbol}
                value={stock.symbol}
                className="border-b border-gray-800 hover:bg-gray-800 "

                //   onSelect={() => handleSelect(stock.symbol)}
              >
                <div className="flex justify-between w-full">
                  <span className="font-bold text-white">{stock.symbol}</span>
                  <span className="text-gray-400 text-xs truncate max-w-[150px]">
                    {stock.description}
                  </span>
                </div>
              </ComboboxItem>
            )}
          </ComboboxList>
        )}
      </ComboboxContent>
    </Combobox>
  );
}
