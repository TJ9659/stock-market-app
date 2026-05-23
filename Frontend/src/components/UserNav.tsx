import { useNavigate, Link } from "react-router";
import { useAuth } from "../context/AuthContext";
import {
  LogOut,
  User,
  Settings,
  CreditCard,
  LayoutDashboard,
  ArrowDown,
  ChevronDown,
} from "lucide-react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "./ui/dropdown-menu";

export function UserNav() {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  if (!isAuthenticated) {
    return (
      <div className="mt-auto p-8">
        <button
          onClick={() => navigate("/register")}
          className="hidden lg:block w-full px-4 py-3 text-md font-bold text-black bg-emerald-500 rounded-2xl hover:bg-emerald-400 shadow-[0_0_20px_rgba(16,185,129,0.2)] transition-all active:scale-[0.98]"
        >
          Get Started
        </button>
      </div>
    );
  }

  const initials =
    `${user?.firstName?.charAt(0) || ""}${user?.lastName?.charAt(0) || ""}`.toUpperCase();

  return (
    <div className="hidden lg:block mt-auto p-6 border-t border-gray-800/50">
      <div className="flex items-center gap-3">
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <div className="group flex items-center gap-2 cursor-pointer">
              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-500 text-black font-black text-xs shadow-lg shadow-emerald-500/10">
                {initials || <User size={18} />}
              </div>

              <ChevronDown
                size={18}
                className="transition-transform duration-200 group-data-[state=open]:rotate-180"
              />
            </div>

            {/* <div className="flex flex-col flex-1 overflow-hidden">
              <p className="text-sm font-bold text-white truncate">
                {user?.firstName} {user?.lastName}
              </p>
            </div> */}
          </DropdownMenuTrigger>
          <DropdownMenuContent className="bg-[#0f0f0f] text-white">
            <DropdownMenuGroup>
              <DropdownMenuLabel>My Account</DropdownMenuLabel>
              <DropdownMenuItem onClick={() => navigate("/profile")}>
                Profile
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => navigate("/change-password")}>
                Change Password
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

        {/* <button
          onClick={handleLogout}
          className="p-2 text-gray-500 hover:text-red-400 hover:bg-red-400/10 rounded-lg transition-colors"
          title="Log out"
        >
          <LogOut size={18} />
        </button> */}
      </div>
    </div>
  );
}
