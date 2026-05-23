import { BrowserRouter, Routes, Route } from "react-router";
// import { ScrollToTop } from "./components/UI/ScrollToTop";

import Home from "./pages/HomePage";
import Header from "./components/Header";
import MarketsOverview from "./pages/MarketsOverviewPage";
import StockDetailPage from "./pages/StockDetailsPage";
import { AuthProvider } from "./context/AuthContext";
import WatchlistPage from "./pages/WatchlistPage";
import ProtectedRoute from "./components/routes/ProtectedRoute";
import LoginPage from "./pages/auth/LoginPage";
import RegisterPage from "./pages/auth/RegisterPage";
import PublicRoute from "./components/routes/PublicRoute";
import { Toaster } from "./components/ui/sonner";
import { ScrollToTop } from "./components/ScrollToTop";
import ProfileEditPage from "./pages/user/ProfileEditPage";
import PasswordChangePage from "./pages/user/PasswordChangePage";
import NotFoundPage from "./pages/NotFoundPage";

function App() {
  return (
    <AuthProvider>
      <Toaster
          theme="dark" 
          position="top-right" 
          toastOptions={{
            style: { 
              background: '#0B0E11', 
              border: '1px solid #1f2937=',
              color: '#00bc7d' 
            },
          }}
        />
      <BrowserRouter>
        <ScrollToTop/>
        <div className="pb-10">
          <Header />
        </div>
        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 min-h-screen pt-16">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route element={<PublicRoute />}>
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
            </Route>

            <Route path="/markets" element={<MarketsOverview />} />
            <Route path="/stock/:symbol" element={<StockDetailPage />} />

            <Route element={<ProtectedRoute />}>
              <Route path="/watchlists" element={<WatchlistPage />} />
              <Route path="/profile" element={<ProfileEditPage/>} />
              <Route path="/change-password" element={<PasswordChangePage/>} />
            </Route>

            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </main>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
