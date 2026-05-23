import { useEffect, useState } from "react";
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
//   CommandSeparator,
//   CommandShortcut,
} from "../ui/command"
import api from "../../services/api";
import { useNavigate } from "react-router";

export function StockSearch({ setOpen }: { setOpen?: (open: boolean) => void }) {
  const navigate = useNavigate();
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [query, setQuery] = useState("");

  const handleSearch = async (searchTerm: string) => {
    if (searchTerm.length < 2) {
      setResults([]);
      return;
    }
    try {
      setLoading(true);
      const res = await api.get(`/stocks/search?query=${searchTerm}`);
      setResults(res.data);
    } catch (error) {
      console.error("Search failed", error);
    } finally {
      setLoading(false);
    }
  };

  const handleSelect = (symbol: string) => {
    navigate(`/stock/${symbol}`);
    setOpen?.(false);
  };

  useEffect(() => {
    const timer = setTimeout(() => {
      handleSearch(query);
    }, 300);
    return () => clearTimeout(timer);
  }, [query]);

  return (
    <Command shouldFilter={false} className="py-2 bg-[#0B0E11] overflow-visible">
      <CommandInput
        className="flex h-11 w-full bg-transparent py-3 text-sm outline-none border-none focus:ring-0 text-white placeholder:text-gray-500"
        placeholder="Search symbols or companies..."
        onValueChange={setQuery}
      />
      
      { query.length > 1 && <div className="relative bg-[#0B0E11] pt-2">
        <CommandList className="relative w-full bg-[#0B0E11] border border-gray-800 shadow-2xl z-50">
          {loading && <div className="p-4 text-sm text-gray-400">Searching...</div>}
          
          {!loading && query.length > 1 && results.length === 0 && (
            <CommandEmpty className="p-4 text-sm text-gray-500">No results found.</CommandEmpty>
          )}

          <CommandGroup>
            {results.map((stock: any) => (
              <CommandItem
                key={stock.symbol}
                value={stock.symbol} 
                onSelect={() => handleSelect(stock.symbol)}
                className="flex items-center justify-between p-3 cursor-pointer 
                        aria-selected:bg-gray-800 aria-selected:text-white 
                        hover:bg-gray-800 transition-colors"
              >
                <div className="flex justify-between w-full">
                  <span className="font-bold text-white">{stock.symbol}</span>
                  <span className="text-gray-400 text-xs truncate max-w-[200px]">
                    {stock.description}
                  </span>
                </div>
              </CommandItem>
            ))}
          </CommandGroup>
        </CommandList>
      </div> }
    </Command>
  );
}