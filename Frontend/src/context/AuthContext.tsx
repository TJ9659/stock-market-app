import React, { createContext, useContext, useState, useEffect } from "react";
import api from "../services/api";
import type { User } from "../interfaces/interfaces";


interface AuthContextType {
  isAuthenticated: boolean;
  user: User | null;
  login: (formData: any) => Promise<void>;
  registerUser: (formData: any) => Promise<void>;
  updateUser: (formData: any) => Promise<void>;
  updatePassword: (formData: any) => Promise<void>;
  logout: () => void;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(
    !!localStorage.getItem("token"),
  );
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    const checkAuth = async () => {
      const token = localStorage.getItem("token");
      if (token) {
        try {
          const response = await api.get("/auth/me");
          setUser(response.data);
          setIsAuthenticated(true);
        } catch (error) {
          console.error("Token verification failed:", error);
          logout();
        }
      }
      setLoading(false);
    };
    checkAuth();
  }, []);

  const registerUser = async (formData: any) => {
    try {
      const response = await api.post("/auth/register", formData);
      const { token, user: userData } = response.data;

      localStorage.setItem("token", token);
      setUser(userData);
      setIsAuthenticated(true);
    } catch (error: any) {
      console.log(error);
      if (error.response && error.response.data) {
        const problemDetail = error.response.data;

        const errorMessage = problemDetail.detail;
        throw errorMessage || `Failed to register profile. Please try again.`;
      }
      throw (
        error.response?.data?.message ||
        "Failed to register profile. Please try again."
      );
    }
  };

  const login = async (formData: any) => {
    try {
      const response = await api.post("/auth/login", formData);
      const { token, user: userData } = response.data;

      localStorage.setItem("token", token);
      setUser(userData);
      setIsAuthenticated(true);
    } catch (error: any) {
      throw (
        error.response?.data?.message ||
        "Failed to update profile. Please try again."
      );
    }
  };

  const updateUser = async (formData: any) => {
    try {
      const response = await api.put("/users/me", formData);

      setUser(response.data);

      return response.data;
    } catch (error: any) {
      console.error("Failed to update user:", error);

      throw (
        error.response?.data?.message ||
        "Failed to update profile. Please try again."
      );
    }
  };

  const updatePassword = async (formData: any) => {
    try {
      const response = await api.put("/users/me/change-password", formData);

      return response.data;
    } catch (error: any) {
      console.error("Failed to update password:", error);
      if (error.response && error.response.data) {
        const problemDetail = error.response.data;

        const errorMessage = problemDetail.detail;
        throw errorMessage || `Failed to update password. Please try again.`;
      }
      throw (
        error.response?.data?.message ||
        "Failed to update password. Please try again."
      );
    }
  };

  const logout = () => {
    localStorage.removeItem("token");
    setUser(null);
    setIsAuthenticated(false);
  };

  return (
    <AuthContext.Provider
      value={{
        isAuthenticated,
        user,
        login,
        logout,
        loading,
        registerUser,
        updateUser,
        updatePassword,
      }}
    >
      {!loading && children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within AuthProvider");
  return context;
};
