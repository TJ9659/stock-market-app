import { useNavigate, Link } from "react-router";
import { useAuth } from "../context/AuthContext";
import {
  LogOut,
  User,
  Settings,
  CreditCard,
  LayoutDashboard,
} from "lucide-react";
import { Button } from "../components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuPortal,
  DropdownMenuSeparator,
  DropdownMenuShortcut,
  DropdownMenuSub,
  DropdownMenuSubContent,
  DropdownMenuSubTrigger,
  DropdownMenuTrigger,
} from "../components/ui/dropdown-menu";
import { useEffect } from "react";

interface GlobalSearchProps {
  setMobileMenuOpen?: (open: boolean) => void;
}

export const MobileUserNav = ({ setMobileMenuOpen }: GlobalSearchProps) => {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    setMobileMenuOpen?.(false);
    logout();
    navigate("/login");
  };

  if (!isAuthenticated) {
    return (
      <div className="mt-auto p-8">
        <button
          onClick={() => navigate("/register")}
          className="w-full px-4 py-3 text-md font-bold text-black bg-emerald-500 rounded-2xl hover:bg-emerald-400 shadow-[0_0_20px_rgba(16,185,129,0.2)] transition-all active:scale-[0.98]"
        >
          Get Started
        </button>
      </div>
    );
  }

  const initials =
    `${user?.firstName?.charAt(0) || ""}${user?.lastName?.charAt(0) || ""}`.toUpperCase();

  return (
    <div className="mt-auto p-6 border-t border-gray-800/50">
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-500 text-black font-black text-xs shadow-lg shadow-emerald-500/10">
              {initials || <User size={18} />}
            </div>

            <div className="flex flex-col flex-1 overflow-hidden">
              <p className="text-sm font-bold text-white truncate">
                {user?.firstName} {user?.lastName}
              </p>
            </div>
          </div>
        </DropdownMenuTrigger>
        <DropdownMenuContent
          className="w-40 bg-[#0f0f0f] text-white"
          align="start"
        >
          <DropdownMenuGroup>
            <DropdownMenuLabel>My Account</DropdownMenuLabel>
            <DropdownMenuItem onClick={() => navigate("/profile")}>
              Profile
            </DropdownMenuItem>
          </DropdownMenuGroup>
          <DropdownMenuSeparator className="" />
          <DropdownMenuItem
            className="flex items-center gap-2"
            onClick={handleLogout}
          >
            <LogOut size={18} />
            <span>Logout</span>
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  );
};
