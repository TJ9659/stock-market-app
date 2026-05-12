import { Menu, Search, X } from "lucide-react";
import { useState } from "react";
import { NavLink } from "react-router"; // Standard for React Router
import { GlobalSearch } from "./GlobalSearch";

const links = [
  { name: "Markets", to: "/markets" },
  { name: "Watchlists", to: "/watchlists" },
];

const mobileLinkStyles = ({ isActive }: any) =>
  `block w-full px-8 py-6 text-lg font-black transition-all border-b border-gray-800 ${
    isActive ? "text-emerald-500" : "text-white hover:text-emerald-400"
  }`;

const Header = () => {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <div className="fixed top-0 w-full z-50 px-4 py-4">
      <header className="max-w-7xl mx-auto flex items-center justify-between h-16 px-6 border border-gray-800 bg-[#0B0E11]/80 backdrop-blur-md rounded-full shadow-2xl">
        <div className="flex items-center gap-8">
          <NavLink className="flex items-center gap-2 group" to="/">
            <div className="w-8 h-8 bg-emerald-500 rounded-lg flex items-center justify-center font-bold text-black text-xl shadow-[0_0_15px_rgba(16,185,129,0.4)]">
              M
            </div>
            <span className="text-xl font-bold tracking-tight text-white group-hover:text-emerald-400 transition-colors">
              MarketSense
            </span>
          </NavLink>

          <nav className="hidden lg:flex items-center gap-1">
            {links.map((link) => (
              <NavLink
                key={link.name}
                to={link.to}
                className={({ isActive }) =>
                  `px-4 py-2 rounded-full text-sm font-medium transition-all duration-300 ${
                    isActive
                      ? "text-emerald-400 bg-gray-800"
                      : "text-gray-300 hover:text-white hover:bg-gray-800"
                  }`
                }
              >
                {link.name}
              </NavLink>
            ))}

            <GlobalSearch />
          </nav>
        </div>

        <div className="flex items-center gap-4">
          <button className="hidden md:block px-6 py-2 text-sm font-bold text-black bg-emerald-500 rounded-full hover:bg-emerald-400 transition-all">
            Get Started
          </button>

          <button
            onClick={() => setIsOpen(true)}
            className="lg:hidden text-white p-2 hover:bg-gray-800 rounded-full transition-colors"
          >
            <Menu size={28} strokeWidth={1} />
          </button>
        </div>
      </header>

      <div
        className={`fixed inset-0 z-[60] transform ${
          isOpen ? "translate-x-0" : "translate-x-full"
        } transition-transform duration-500 ease-in-out`}
      >
        <div
          className="absolute inset-0 bg-black/60 backdrop-blur-sm"
          onClick={() => setIsOpen(false)}
        />

        <div className="absolute right-0 top-0 h-full w-[80%] max-w-sm bg-[#0B0E11] border-l border-gray-800 shadow-2xl flex flex-col">
          <div className="flex items-center justify-between p-6 border-b border-gray-800">
            <span className="text-xl font-bold text-emerald-500">Menu</span>
            <button
              onClick={() => setIsOpen(false)}
              className="p-2 text-white hover:bg-gray-800 rounded-full transition-colors"
            >
              <X size={30} strokeWidth={1} />
            </button>
          </div>

          <nav className="flex flex-col mt-4">
            {links.map((link) => (
              <NavLink
                key={link.name}
                to={link.to}
                onClick={() => setIsOpen(false)}
                className={mobileLinkStyles}
              >
                {link.name}
              </NavLink>
            ))}
            <GlobalSearch setMobileMenuOpen={setIsOpen}/>
          </nav>

          <div className="mt-auto p-8">
            <button className="w-full py-4 text-lg font-bold text-black bg-emerald-500 rounded-2xl hover:bg-emerald-400 shadow-[0_0_20px_rgba(16,185,129,0.2)]">
              Get Started
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Header;
