import { Search } from "lucide-react";
import { useEffect, useState } from "react";

interface SearchWrapperProps {
  setOpen: (open: boolean) => void;
  setMobileMenuOpen?: (open: boolean) => void;
}

const SearchWrapper = ({ setOpen, setMobileMenuOpen }: SearchWrapperProps) => {
  useEffect(() => {
    const down = (e: KeyboardEvent) => {
      if (e.key === "k" && (e.metaKey || e.ctrlKey)) {
        e.preventDefault();
        setOpen((open: boolean) => !open);
      }
    };
    document.addEventListener("keydown", down);
    return () => document.removeEventListener("keydown", down);
  }, []);

  return (
    <>
      <div className="block lg:hidden">
        <button
          onClick={() => {
            setOpen(true);
            setMobileMenuOpen?.(false);
          }}
          className="block text-left w-full px-8 py-6 text-lg font-black transition-all border-b border-gray-800 text-white hover:text-emerald-400"
        >
          <span>Search</span>
        </button>
      </div>

      <div className="hidden lg:block">
        <button
          onClick={() => setOpen(true)}
          className="px-4 py-2 rounded-full text-sm font-medium transition-all duration-300 text-gray-300 hover:text-white hover:bg-gray-800 flex items-center gap-2 border border-transparent hover:border-gray-700"
        >
          <Search size={20} strokeWidth={1} />
          <span>Search...</span>
          <kbd className="text-xs bg-gray-800 px-1 rounded">⌘K</kbd>
        </button>
      </div>
    </>
  );
};

export default SearchWrapper;
