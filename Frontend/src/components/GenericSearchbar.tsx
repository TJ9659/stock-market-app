import { Button } from "../components/ui/button"
import { Input } from "../components/ui/input"
import { Field } from "../components/ui/field"

interface SearchbarProps {
  value: string;
  onChange: (value: string) => void;
  onSearch: () => void;
  placeholder?: string;
}

const GenericSearchbar = ({ value, onChange, onSearch, placeholder = "Search..." }: SearchbarProps) => {
  
  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      onSearch();
    }
  };

  return (
    <Field orientation="horizontal">
      <Input 
        className="bg-[#161616]" 
        type="search" 
        placeholder={placeholder}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        onKeyDown={handleKeyDown}
      />
      <Button 
        onClick={onSearch}
        type="button"
        className="bg-emerald-500 hover:bg-emerald-400 transition-colors"
      >
        Search
      </Button>
    </Field>
  )
}

export default GenericSearchbar;