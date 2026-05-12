
import { StockSearch } from "./StockSearch";
import { useEffect, useState } from "react";
import { Search } from "lucide-react";
import { DialogDescription, DialogTitle } from "./ui/dialog";
import { CommandDialog } from "./ui/command";
import SearchWrapper from "./SearchWrapper";

interface GlobalSearchProps {
    setMobileMenuOpen: (open: boolean) => void
}

export const GlobalSearch = ({setMobileMenuOpen} : GlobalSearchProps) => {
  const [open, setOpen] = useState(false);
  return (
    <>
    <SearchWrapper setOpen={setOpen} setMobileMenuOpen={setMobileMenuOpen}/>
      <CommandDialog open={open} onOpenChange={setOpen}>
        <span className="sr-only">
          <DialogTitle>Stock Search</DialogTitle>
          <DialogDescription>
            Search for stock symbols and company names.
          </DialogDescription>
        </span>
        <StockSearch />
      </CommandDialog>
    </>
  );
}
