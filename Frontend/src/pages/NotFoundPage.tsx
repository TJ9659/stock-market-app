import React from "react";
import { useNavigate } from "react-router";
import { Button } from "../components/ui/button";
import { TrendingDown, Home } from "lucide-react";

const NotFoundPage = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-[80vh] flex flex-col items-center justify-center text-center px-4">
      <div className="bg-emerald-500/10 p-6 rounded-full mb-6">
        <TrendingDown className="w-16 h-16 text-emerald-500" />
      </div>

      <h1 className="text-8xl font-black text-white mb-2">404</h1>
      <h2 className="text-2xl font-semibold text-zinc-400 mb-6">
        Market Closed: This page doesn't exist.
      </h2>
      
      <p className="max-w-md text-zinc-500 mb-8">
        The ticker or page you're looking for has been delisted or moved. 
        Check the URL or head back to the Home Page.
      </p>

      <div className="flex gap-4">
        <Button
          onClick={() => navigate(-1)}
          variant="outline"
          className="border-gray-800 bg-gray-800 hover:bg-gray-900 hover:text-white font-semibold text-gray-400 hover:cursor-pointer"
        >
          Go Back
        </Button>
        <Button
          onClick={() => navigate("/")}
          className="bg-emerald-500 hover:bg-emerald-400 text-white font-semibold hover:cursor-pointer"
        >
          <Home className="w-4 h-4 mr-2" />
          Back to Home Page
        </Button>
      </div>
    </div>
  );
};

export default NotFoundPage;